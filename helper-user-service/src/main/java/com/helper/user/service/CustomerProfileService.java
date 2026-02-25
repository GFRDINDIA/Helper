package com.helper.user.service;

import com.helper.user.dto.request.AddressRequest;
import com.helper.user.dto.request.CustomerProfileRequest;
import com.helper.user.dto.response.CustomerProfileResponse;
import com.helper.user.dto.response.CustomerProfileResponse.AddressResponse;
import com.helper.user.dto.response.CustomerProfileResponse.PaymentMethodResponse;
import com.helper.user.entity.CustomerAddress;
import com.helper.user.entity.CustomerProfile;
import com.helper.user.exception.UserExceptions;
import com.helper.user.repository.CustomerProfileRepository;
import com.helper.user.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerProfileService {

    private final CustomerProfileRepository customerRepo;

    @Transactional
    public CustomerProfileResponse createOrUpdateProfile(CustomerProfileRequest request, AuthenticatedUser user) {
        if (!user.isCustomer()) {
            throw new UserExceptions.UnauthorizedAccessException("Only customers can manage customer profiles");
        }

        CustomerProfile profile = customerRepo.findById(user.getUserId())
                .orElse(CustomerProfile.builder().customerId(user.getUserId()).build());

        if (request.getProfileImageUrl() != null) profile.setProfileImageUrl(request.getProfileImageUrl());

        profile = customerRepo.save(profile);
        log.info("Customer profile created/updated: {}", user.getUserId());
        return mapToResponse(profile);
    }

    public CustomerProfileResponse getProfile(UUID customerId) {
        CustomerProfile profile = customerRepo.findById(customerId)
                .orElseThrow(() -> new UserExceptions.ProfileNotFoundException("Customer profile not found: " + customerId));
        return mapToResponse(profile);
    }

    // ===== ADDRESSES =====

    @Transactional
    public CustomerProfileResponse addAddress(AddressRequest request, AuthenticatedUser user) {
        CustomerProfile profile = getProfileOrCreate(user);

        // If this is set as default, unset others
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            profile.getAddresses().forEach(a -> a.setIsDefault(false));
        }

        CustomerAddress address = CustomerAddress.builder()
                .customerProfile(profile)
                .label(request.getLabel())
                .addressLine1(request.getAddressLine1())
                .addressLine2(request.getAddressLine2())
                .city(request.getCity())
                .state(request.getState())
                .pinCode(request.getPinCode())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .isDefault(request.getIsDefault() != null ? request.getIsDefault() : profile.getAddresses().isEmpty())
                .build();

        profile.getAddresses().add(address);
        profile = customerRepo.save(profile);
        log.info("Address added for customer: {}", user.getUserId());
        return mapToResponse(profile);
    }

    @Transactional
    public CustomerProfileResponse removeAddress(UUID addressId, AuthenticatedUser user) {
        CustomerProfile profile = getProfileOrThrow(user.getUserId());
        profile.getAddresses().removeIf(a -> a.getAddressId().equals(addressId));
        profile = customerRepo.save(profile);
        return mapToResponse(profile);
    }

    // ===== HELPERS =====

    private CustomerProfile getProfileOrThrow(UUID customerId) {
        return customerRepo.findById(customerId)
                .orElseThrow(() -> new UserExceptions.ProfileNotFoundException("Customer profile not found"));
    }

    private CustomerProfile getProfileOrCreate(AuthenticatedUser user) {
        return customerRepo.findById(user.getUserId())
                .orElseGet(() -> customerRepo.save(
                        CustomerProfile.builder().customerId(user.getUserId()).build()));
    }

    private CustomerProfileResponse mapToResponse(CustomerProfile p) {
        List<AddressResponse> addrs = p.getAddresses().stream().map(a -> AddressResponse.builder()
                .addressId(a.getAddressId()).label(a.getLabel())
                .addressLine1(a.getAddressLine1()).addressLine2(a.getAddressLine2())
                .city(a.getCity()).state(a.getState()).pinCode(a.getPinCode())
                .latitude(a.getLatitude()).longitude(a.getLongitude())
                .isDefault(a.getIsDefault()).build()
        ).collect(Collectors.toList());

        List<PaymentMethodResponse> methods = p.getPaymentMethods().stream().map(m -> PaymentMethodResponse.builder()
                .methodId(m.getMethodId()).methodType(m.getMethodType().name())
                .label(m.getLabel()).maskedIdentifier(m.getMaskedIdentifier())
                .isDefault(m.getIsDefault()).build()
        ).collect(Collectors.toList());

        return CustomerProfileResponse.builder()
                .customerId(p.getCustomerId()).profileImageUrl(p.getProfileImageUrl())
                .averageRating(p.getAverageRating()).totalRatings(p.getTotalRatings())
                .totalTasksPosted(p.getTotalTasksPosted())
                .addresses(addrs).paymentMethods(methods)
                .createdAt(p.getCreatedAt()).build();
    }
}
