import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../providers/customer_provider.dart';
import '../data/customer_repository.dart';
import '../data/models/task_models.dart';
import '../../../shared/theme/app_theme.dart';
import '../../../shared/widgets/loading_overlay.dart';
import '../../../core/constants/app_constants.dart';
import '../../../core/network/api_exception.dart';

class TaskDetailScreen extends ConsumerStatefulWidget {
  final String taskId;

  const TaskDetailScreen({super.key, required this.taskId});

  @override
  ConsumerState<TaskDetailScreen> createState() => _TaskDetailScreenState();
}

class _TaskDetailScreenState extends ConsumerState<TaskDetailScreen> {
  bool _isActing = false;

  Future<void> _updateStatus(String status) async {
    setState(() => _isActing = true);
    try {
      await CustomerRepository().updateTaskStatus(widget.taskId, status);
      ref.invalidate(taskDetailProvider(widget.taskId));
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Task status updated to $status'),
            backgroundColor: Colors.green),
      );
    } on ApiException catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(context)
          .showSnackBar(SnackBar(content: Text(e.message)));
    } finally {
      if (mounted) setState(() => _isActing = false);
    }
  }

  Future<void> _acceptBid(String bidId) async {
    setState(() => _isActing = true);
    try {
      await CustomerRepository().acceptBid(bidId);
      ref.invalidate(taskDetailProvider(widget.taskId));
      ref.invalidate(taskBidsProvider(widget.taskId));
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
            content: Text('Bid accepted'), backgroundColor: Colors.green),
      );
    } on ApiException catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(context)
          .showSnackBar(SnackBar(content: Text(e.message)));
    } finally {
      if (mounted) setState(() => _isActing = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final taskAsync = ref.watch(taskDetailProvider(widget.taskId));

    return Scaffold(
      appBar: AppBar(title: const Text('Task Details')),
      body: taskAsync.when(
        data: (task) => _buildBody(task),
        loading: () => const LoadingOverlay(),
        error: (e, _) => Center(child: Text('Error: $e')),
      ),
    );
  }

  Widget _buildBody(Task task) {
    return RefreshIndicator(
      onRefresh: () =>
          ref.refresh(taskDetailProvider(widget.taskId).future),
      child: SingleChildScrollView(
        physics: const AlwaysScrollableScrollPhysics(),
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Header
            Row(
              children: [
                Text(kDomainIcons[task.domain] ?? '🛠️',
                    style: const TextStyle(fontSize: 28)),
                const SizedBox(width: 12),
                Expanded(
                  child: Text(task.title,
                      style: const TextStyle(
                          fontSize: 20, fontWeight: FontWeight.bold)),
                ),
                _StatusBadge(status: task.status),
              ],
            ),
            const SizedBox(height: 16),
            Text(task.description,
                style: TextStyle(color: Colors.grey.shade700, height: 1.6)),
            const Divider(height: 32),
            _InfoRow(
                icon: Icons.location_on_outlined, text: task.location),
            const SizedBox(height: 8),
            _InfoRow(
                icon: Icons.currency_rupee,
                text: task.finalPrice != null
                    ? 'Final: ₹${task.finalPrice!.toStringAsFixed(0)}'
                    : task.displayBudget),
            const SizedBox(height: 8),
            _InfoRow(
                icon: Icons.sell_outlined,
                text: task.pricingModel == 'FIXED'
                    ? 'Fixed Price'
                    : 'Open Bidding'),
            const Divider(height: 32),
            // Action buttons based on status
            ..._buildActions(task),
            const SizedBox(height: 24),
            // Bids section (for BIDDING tasks in OPEN status)
            if (task.pricingModel == 'BIDDING' &&
                ['OPEN', 'POSTED'].contains(task.status))
              _BidsList(taskId: task.id, onAccept: _acceptBid),
          ],
        ),
      ),
    );
  }

  List<Widget> _buildActions(Task task) {
    final buttons = <Widget>[];

    if (task.status == 'OPEN' && task.pricingModel == 'FIXED') {
      buttons.add(_ActionButton(
        label: 'Accept & Start',
        icon: Icons.check_circle_outline,
        color: Colors.green,
        isLoading: _isActing,
        onPressed: () => _updateStatus('ACCEPTED'),
      ));
    }

    if (task.status == 'IN_PROGRESS') {
      buttons.add(_ActionButton(
        label: 'Mark Completed',
        icon: Icons.task_alt_outlined,
        color: Colors.blue,
        isLoading: _isActing,
        onPressed: () => _updateStatus('COMPLETED'),
      ));
    }

    if (task.status == 'COMPLETED') {
      buttons.add(ElevatedButton.icon(
        onPressed: () => context.push('/payment/${task.id}'),
        icon: const Icon(Icons.payment, color: Colors.white),
        label: const Text('Pay Now',
            style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold)),
        style: ElevatedButton.styleFrom(
          backgroundColor: AppTheme.primary,
          minimumSize: const Size(double.infinity, 48),
          shape:
              RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
        ),
      ));
    }

    if (['OPEN', 'POSTED'].contains(task.status)) {
      buttons.add(const SizedBox(height: 8));
      buttons.add(_ActionButton(
        label: 'Cancel Task',
        icon: Icons.cancel_outlined,
        color: Colors.red,
        isLoading: _isActing,
        onPressed: () => _updateStatus('CANCELLED'),
      ));
    }

    return buttons;
  }
}

class _BidsList extends ConsumerWidget {
  final String taskId;
  final Future<void> Function(String) onAccept;

  const _BidsList({required this.taskId, required this.onAccept});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final bidsAsync = ref.watch(taskBidsProvider(taskId));

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Text('Bids Received',
            style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
        const SizedBox(height: 12),
        bidsAsync.when(
          data: (bids) => bids.isEmpty
              ? Text('No bids yet',
                  style: TextStyle(color: Colors.grey.shade500))
              : Column(
                  children: bids
                      .map((b) => _BidCard(bid: b, onAccept: onAccept))
                      .toList(),
                ),
          loading: () =>
              const Center(child: CircularProgressIndicator()),
          error: (e, _) => Text('Error loading bids: $e'),
        ),
      ],
    );
  }
}

class _BidCard extends StatelessWidget {
  final Bid bid;
  final Future<void> Function(String) onAccept;

  const _BidCard({required this.bid, required this.onAccept});

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: const EdgeInsets.only(bottom: 8),
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
      child: Padding(
        padding: const EdgeInsets.all(12),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                CircleAvatar(
                  radius: 18,
                  backgroundColor: AppTheme.primary.withOpacity(0.1),
                  child: Text(
                    (bid.workerName ?? 'W')[0].toUpperCase(),
                    style: TextStyle(color: AppTheme.primary),
                  ),
                ),
                const SizedBox(width: 8),
                Expanded(
                  child: Text(bid.workerName ?? 'Worker',
                      style:
                          const TextStyle(fontWeight: FontWeight.w600)),
                ),
                Text('₹${bid.proposedAmount.toStringAsFixed(0)}',
                    style: TextStyle(
                        color: AppTheme.primary,
                        fontWeight: FontWeight.bold,
                        fontSize: 16)),
              ],
            ),
            if (bid.message != null && bid.message!.isNotEmpty) ...[
              const SizedBox(height: 8),
              Text(bid.message!,
                  style: TextStyle(
                      color: Colors.grey.shade600, fontSize: 13)),
            ],
            if (bid.estimatedHours != null)
              Padding(
                padding: const EdgeInsets.only(top: 4),
                child: Text('Est. ${bid.estimatedHours}h',
                    style: TextStyle(
                        color: Colors.grey.shade500, fontSize: 12)),
              ),
            if (bid.status == 'PENDING') ...[
              const SizedBox(height: 10),
              SizedBox(
                width: double.infinity,
                child: ElevatedButton(
                  onPressed: () => onAccept(bid.id),
                  style: ElevatedButton.styleFrom(
                    backgroundColor: Colors.green,
                    foregroundColor: Colors.white,
                    shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(8)),
                  ),
                  child: const Text('Accept This Bid'),
                ),
              ),
            ],
          ],
        ),
      ),
    );
  }
}

class _StatusBadge extends StatelessWidget {
  final String status;

  const _StatusBadge({required this.status});

  @override
  Widget build(BuildContext context) {
    final color = _color(status);
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 5),
      decoration: BoxDecoration(
        color: color.withOpacity(0.1),
        borderRadius: BorderRadius.circular(8),
        border: Border.all(color: color.withOpacity(0.3)),
      ),
      child: Text(
        status.replaceAll('_', ' '),
        style: TextStyle(
            color: color, fontSize: 12, fontWeight: FontWeight.bold),
      ),
    );
  }

  Color _color(String s) {
    switch (s) {
      case 'OPEN':
        return Colors.blue;
      case 'IN_PROGRESS':
        return Colors.orange;
      case 'COMPLETED':
        return Colors.green;
      case 'CANCELLED':
        return Colors.red;
      default:
        return Colors.grey;
    }
  }
}

class _InfoRow extends StatelessWidget {
  final IconData icon;
  final String text;

  const _InfoRow({required this.icon, required this.text});

  @override
  Widget build(BuildContext context) => Row(
        children: [
          Icon(icon, size: 18, color: Colors.grey.shade500),
          const SizedBox(width: 8),
          Expanded(
              child: Text(text,
                  style: TextStyle(color: Colors.grey.shade700))),
        ],
      );
}

class _ActionButton extends StatelessWidget {
  final String label;
  final IconData icon;
  final Color color;
  final bool isLoading;
  final VoidCallback onPressed;

  const _ActionButton({
    required this.label,
    required this.icon,
    required this.color,
    required this.isLoading,
    required this.onPressed,
  });

  @override
  Widget build(BuildContext context) => ElevatedButton.icon(
        onPressed: isLoading ? null : onPressed,
        icon: isLoading
            ? const SizedBox(
                width: 18,
                height: 18,
                child: CircularProgressIndicator(
                    strokeWidth: 2, color: Colors.white))
            : Icon(icon, color: Colors.white),
        label: Text(label,
            style: const TextStyle(
                color: Colors.white, fontWeight: FontWeight.bold)),
        style: ElevatedButton.styleFrom(
          backgroundColor: color,
          minimumSize: const Size(double.infinity, 48),
          shape:
              RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
        ),
      );
}
