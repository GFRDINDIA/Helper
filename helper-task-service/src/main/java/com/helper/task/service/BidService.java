package com.helper.task.service;

import com.helper.task.dto.request.CreateBidRequest;
import com.helper.task.dto.response.BidResponse;
import com.helper.task.entity.Bid;
import com.helper.task.entity.Task;
import com.helper.task.enums.*;
import com.helper.task.exception.TaskExceptions;
import com.helper.task.repository.BidRepository;
import com.helper.task.repository.TaskRepository;
import com.helper.task.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BidService {

    private final BidRepository bidRepository;
    private final TaskRepository taskRepository;

    @Value("${app.task.max-bids-per-task:20}")
    private int maxBidsPerTask;

    /**
     * Submit a bid on a task (WORKER only, BIDDING model only)
     */
    @Transactional
    public BidResponse createBid(UUID taskId, CreateBidRequest request, AuthenticatedUser user) {
        if (!user.isWorker()) {
            throw new TaskExceptions.UnauthorizedTaskAccessException("Only workers can submit bids");
        }

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskExceptions.TaskNotFoundException("Task not found: " + taskId));

        // Validate task is open for bidding
        if (task.getStatus() != TaskStatus.OPEN) {
            throw new TaskExceptions.InvalidTaskStateException("Task is not open for bidding. Status: " + task.getStatus());
        }

        if (task.getPricingModel() != PricingModel.BIDDING) {
            throw new TaskExceptions.InvalidBidException("This task uses fixed pricing, not bidding");
        }

        // Worker can't bid on own task
        if (user.getUserId().equals(task.getCustomerId())) {
            throw new TaskExceptions.InvalidBidException("You cannot bid on your own task");
        }

        // Check duplicate bid
        bidRepository.findByTask_TaskIdAndWorkerId(taskId, user.getUserId()).ifPresent(b -> {
            throw new TaskExceptions.DuplicateBidException("You have already submitted a bid on this task");
        });

        // Check bid limit
        long currentBids = bidRepository.countByTask_TaskId(taskId);
        if (currentBids >= maxBidsPerTask) {
            throw new TaskExceptions.BidLimitExceededException(
                    "Maximum bids (" + maxBidsPerTask + ") reached for this task");
        }

        Bid bid = Bid.builder()
                .task(task)
                .workerId(user.getUserId())
                .proposedPrice(request.getProposedPrice())
                .message(request.getMessage())
                .status(BidStatus.PENDING)
                .build();

        bid = bidRepository.save(bid);

        log.info("Bid created: {} on task: {} by worker: {} price: {}",
                bid.getBidId(), taskId, user.getUserId(), request.getProposedPrice());

        return mapToResponse(bid);
    }

    /**
     * Get all bids for a task (task owner or admin only)
     */
    public List<BidResponse> getBidsForTask(UUID taskId, AuthenticatedUser user) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskExceptions.TaskNotFoundException("Task not found: " + taskId));

        // Only task owner, admin, or bidding workers can see bids
        if (!user.isAdmin() && !user.getUserId().equals(task.getCustomerId())) {
            // Workers can only see their own bid
            return bidRepository.findByTask_TaskIdAndWorkerId(taskId, user.getUserId())
                    .map(b -> List.of(mapToResponse(b)))
                    .orElse(List.of());
        }

        return bidRepository.findByTask_TaskIdOrderByCreatedAtDesc(taskId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    /**
     * Accept a bid (task owner only)
     * Sets the worker, final price, and moves task to ACCEPTED
     */
    @Transactional
    public BidResponse acceptBid(UUID bidId, AuthenticatedUser user) {
        Bid bid = bidRepository.findById(bidId)
                .orElseThrow(() -> new TaskExceptions.BidNotFoundException("Bid not found: " + bidId));

        Task task = bid.getTask();

        // Only task owner can accept bids
        if (!user.getUserId().equals(task.getCustomerId()) && !user.isAdmin()) {
            throw new TaskExceptions.UnauthorizedTaskAccessException("Only the task owner can accept bids");
        }

        // Task must be OPEN
        if (task.getStatus() != TaskStatus.OPEN) {
            throw new TaskExceptions.InvalidTaskStateException("Task is no longer open. Status: " + task.getStatus());
        }

        // Bid must be PENDING
        if (bid.getStatus() != BidStatus.PENDING) {
            throw new TaskExceptions.InvalidBidException("Bid is not in PENDING status: " + bid.getStatus());
        }

        // Accept the bid
        bid.setStatus(BidStatus.ACCEPTED);
        bid.setRespondedAt(LocalDateTime.now());
        bidRepository.save(bid);

        // Update task
        task.setAssignedWorkerId(bid.getWorkerId());
        task.setFinalPrice(bid.getProposedPrice());
        task.setStatus(TaskStatus.ACCEPTED);
        taskRepository.save(task);

        // Reject all other pending bids
        List<Bid> otherBids = bidRepository.findByTask_TaskIdAndStatus(task.getTaskId(), BidStatus.PENDING);
        otherBids.forEach(b -> {
            b.setStatus(BidStatus.REJECTED);
            b.setRespondedAt(LocalDateTime.now());
        });
        bidRepository.saveAll(otherBids);

        log.info("Bid {} accepted for task {}. Worker: {} Price: {}",
                bidId, task.getTaskId(), bid.getWorkerId(), bid.getProposedPrice());

        return mapToResponse(bid);
    }

    /**
     * Reject a bid (task owner only)
     */
    @Transactional
    public BidResponse rejectBid(UUID bidId, AuthenticatedUser user) {
        Bid bid = bidRepository.findById(bidId)
                .orElseThrow(() -> new TaskExceptions.BidNotFoundException("Bid not found: " + bidId));

        Task task = bid.getTask();

        if (!user.getUserId().equals(task.getCustomerId()) && !user.isAdmin()) {
            throw new TaskExceptions.UnauthorizedTaskAccessException("Only the task owner can reject bids");
        }

        if (bid.getStatus() != BidStatus.PENDING) {
            throw new TaskExceptions.InvalidBidException("Bid is not in PENDING status");
        }

        bid.setStatus(BidStatus.REJECTED);
        bid.setRespondedAt(LocalDateTime.now());
        bid = bidRepository.save(bid);

        log.info("Bid {} rejected for task {}", bidId, task.getTaskId());

        return mapToResponse(bid);
    }

    /**
     * Withdraw a bid (bidding worker only)
     */
    @Transactional
    public BidResponse withdrawBid(UUID bidId, AuthenticatedUser user) {
        Bid bid = bidRepository.findById(bidId)
                .orElseThrow(() -> new TaskExceptions.BidNotFoundException("Bid not found: " + bidId));

        if (!user.getUserId().equals(bid.getWorkerId())) {
            throw new TaskExceptions.UnauthorizedTaskAccessException("You can only withdraw your own bids");
        }

        if (bid.getStatus() != BidStatus.PENDING) {
            throw new TaskExceptions.InvalidBidException("Only PENDING bids can be withdrawn");
        }

        bid.setStatus(BidStatus.WITHDRAWN);
        bid.setRespondedAt(LocalDateTime.now());
        bid = bidRepository.save(bid);

        log.info("Bid {} withdrawn by worker {}", bidId, user.getUserId());

        return mapToResponse(bid);
    }

    /**
     * Get bids submitted by a worker
     */
    public List<BidResponse> getMyBids(AuthenticatedUser user) {
        if (!user.isWorker()) {
            throw new TaskExceptions.UnauthorizedTaskAccessException("Only workers have bids");
        }
        return bidRepository.findByWorkerIdOrderByCreatedAtDesc(user.getUserId())
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    private BidResponse mapToResponse(Bid bid) {
        return BidResponse.builder()
                .bidId(bid.getBidId())
                .taskId(bid.getTask().getTaskId())
                .workerId(bid.getWorkerId())
                .proposedPrice(bid.getProposedPrice())
                .message(bid.getMessage())
                .status(bid.getStatus())
                .createdAt(bid.getCreatedAt())
                .respondedAt(bid.getRespondedAt())
                .build();
    }
}
