import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:geolocator/geolocator.dart';
import '../providers/worker_provider.dart';
import '../../customer/data/models/task_models.dart';
import '../../auth/providers/auth_provider.dart';
import '../../../shared/theme/app_theme.dart';
import '../../../shared/widgets/loading_overlay.dart';
import '../../../core/constants/app_constants.dart';
import '../data/worker_repository.dart';
import '../../../core/network/api_exception.dart';

class WorkerHomeScreen extends ConsumerStatefulWidget {
  const WorkerHomeScreen({super.key});

  @override
  ConsumerState<WorkerHomeScreen> createState() => _WorkerHomeScreenState();
}

class _WorkerHomeScreenState extends ConsumerState<WorkerHomeScreen>
    with SingleTickerProviderStateMixin {
  late TabController _tabController;
  Position? _position;

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 2, vsync: this);
    _fetchLocation();
  }

  Future<void> _fetchLocation() async {
    try {
      await Geolocator.requestPermission();
      final pos = await Geolocator.getCurrentPosition();
      if (mounted) setState(() => _position = pos);
    } catch (_) {}
  }

  @override
  void dispose() {
    _tabController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final authState = ref.watch(authStateProvider).valueOrNull;

    return Scaffold(
      appBar: AppBar(
        title: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text('Helper',
                style: TextStyle(fontWeight: FontWeight.bold)),
            Text(
              'Worker Dashboard',
              style: TextStyle(fontSize: 12, color: Colors.grey.shade600),
            ),
          ],
        ),
        actions: [
          IconButton(
            icon: const Icon(Icons.person_outline),
            onPressed: () => context.push('/worker/profile'),
          ),
          IconButton(
            icon: const Icon(Icons.article_outlined),
            onPressed: () => context.push('/worker/kyc'),
            tooltip: 'KYC Documents',
          ),
          IconButton(
            icon: const Icon(Icons.logout),
            onPressed: () async {
              await ref.read(authStateProvider.notifier).logout();
              if (context.mounted) context.go('/login');
            },
          ),
        ],
        bottom: TabBar(
          controller: _tabController,
          labelColor: AppTheme.primary,
          unselectedLabelColor: Colors.grey,
          indicatorColor: AppTheme.primary,
          tabs: const [
            Tab(
                icon: Icon(Icons.work_outline_rounded),
                text: 'Available Tasks'),
            Tab(icon: Icon(Icons.gavel_rounded), text: 'My Bids'),
          ],
        ),
      ),
      body: TabBarView(
        controller: _tabController,
        children: [
          _AvailableTasksTab(position: _position),
          _MyBidsTab(),
        ],
      ),
    );
  }
}

class _AvailableTasksTab extends ConsumerWidget {
  final Position? position;

  const _AvailableTasksTab({this.position});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    if (position == null) {
      return Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(Icons.location_off, size: 48, color: Colors.grey.shade400),
            const SizedBox(height: 16),
            Text('Enable location to see available tasks',
                style: TextStyle(color: Colors.grey.shade600)),
          ],
        ),
      );
    }

    final tasksAsync = ref.watch(availableTasksProvider(AvailableTasksParams(
      lat: position!.latitude,
      lng: position!.longitude,
    )));

    return tasksAsync.when(
      data: (tasks) => tasks.isEmpty
          ? Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Icon(Icons.search_off, size: 64, color: Colors.grey.shade300),
                  const SizedBox(height: 16),
                  Text('No tasks available nearby',
                      style: TextStyle(color: Colors.grey.shade500)),
                ],
              ),
            )
          : RefreshIndicator(
              onRefresh: () => ref.refresh(availableTasksProvider(
                AvailableTasksParams(
                    lat: position!.latitude, lng: position!.longitude),
              ).future),
              child: ListView.builder(
                padding: const EdgeInsets.all(16),
                itemCount: tasks.length,
                itemBuilder: (_, i) => _AvailableTaskCard(task: tasks[i]),
              ),
            ),
      loading: () => const LoadingOverlay(),
      error: (e, _) => Center(child: Text('Error: $e')),
    );
  }
}

class _AvailableTaskCard extends ConsumerStatefulWidget {
  final Task task;

  const _AvailableTaskCard({required this.task});

  @override
  ConsumerState<_AvailableTaskCard> createState() => _AvailableTaskCardState();
}

class _AvailableTaskCardState extends ConsumerState<_AvailableTaskCard> {
  bool _isStarting = false;

  Future<void> _startTask() async {
    setState(() => _isStarting = true);
    try {
      await WorkerRepository()
          .updateTaskStatus(widget.task.id, 'IN_PROGRESS');
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
            content: Text('Task started'), backgroundColor: Colors.green),
      );
    } on ApiException catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(context)
          .showSnackBar(SnackBar(content: Text(e.message)));
    } finally {
      if (mounted) setState(() => _isStarting = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final task = widget.task;
    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Text(kDomainIcons[task.domain] ?? '🛠️',
                    style: const TextStyle(fontSize: 22)),
                const SizedBox(width: 8),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(task.title,
                          style: const TextStyle(
                              fontWeight: FontWeight.bold, fontSize: 15)),
                      Text(
                        kDomainLabels[task.domain] ?? task.domain,
                        style: TextStyle(
                            color: Colors.grey.shade500, fontSize: 12),
                      ),
                    ],
                  ),
                ),
                Column(
                  crossAxisAlignment: CrossAxisAlignment.end,
                  children: [
                    Text(
                      task.displayBudget,
                      style: TextStyle(
                          color: AppTheme.primary,
                          fontWeight: FontWeight.bold,
                          fontSize: 15),
                    ),
                    Text(
                      task.pricingModel,
                      style:
                          TextStyle(color: Colors.grey.shade500, fontSize: 11),
                    ),
                  ],
                ),
              ],
            ),
            const SizedBox(height: 8),
            Text(task.description,
                maxLines: 2,
                overflow: TextOverflow.ellipsis,
                style: TextStyle(color: Colors.grey.shade600, fontSize: 13)),
            const SizedBox(height: 8),
            Row(
              children: [
                Icon(Icons.location_on_outlined,
                    size: 14, color: Colors.grey.shade500),
                const SizedBox(width: 4),
                Expanded(
                  child: Text(task.location,
                      style: TextStyle(
                          color: Colors.grey.shade500, fontSize: 12)),
                ),
              ],
            ),
            const SizedBox(height: 12),
            if (task.pricingModel == 'BIDDING')
              SizedBox(
                width: double.infinity,
                child: ElevatedButton(
                  onPressed: () =>
                      context.push('/worker/bid/${task.id}'),
                  style: ElevatedButton.styleFrom(
                    backgroundColor: AppTheme.primary,
                    foregroundColor: Colors.white,
                    shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(10)),
                  ),
                  child: const Text('Submit Bid'),
                ),
              )
            else
              SizedBox(
                width: double.infinity,
                child: ElevatedButton(
                  onPressed: _isStarting ? null : _startTask,
                  style: ElevatedButton.styleFrom(
                    backgroundColor: Colors.green,
                    foregroundColor: Colors.white,
                    shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(10)),
                  ),
                  child: _isStarting
                      ? const SizedBox(
                          width: 20,
                          height: 20,
                          child: CircularProgressIndicator(
                              strokeWidth: 2, color: Colors.white))
                      : const Text('Accept & Start'),
                ),
              ),
          ],
        ),
      ),
    );
  }
}

class _MyBidsTab extends ConsumerWidget {
  const _MyBidsTab();

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final bidsAsync = ref.watch(myBidsProvider);

    return bidsAsync.when(
      data: (bids) => bids.isEmpty
          ? Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Icon(Icons.gavel, size: 64, color: Colors.grey.shade300),
                  const SizedBox(height: 16),
                  Text('You have not submitted any bids yet',
                      style: TextStyle(color: Colors.grey.shade500)),
                ],
              ),
            )
          : RefreshIndicator(
              onRefresh: () => ref.refresh(myBidsProvider.future),
              child: ListView.builder(
                padding: const EdgeInsets.all(16),
                itemCount: bids.length,
                itemBuilder: (_, i) => _MyBidCard(bid: bids[i]),
              ),
            ),
      loading: () => const LoadingOverlay(),
      error: (e, _) => Center(child: Text('Error: $e')),
    );
  }
}

class _MyBidCard extends ConsumerStatefulWidget {
  final Bid bid;

  const _MyBidCard({required this.bid});

  @override
  ConsumerState<_MyBidCard> createState() => _MyBidCardState();
}

class _MyBidCardState extends ConsumerState<_MyBidCard> {
  bool _isWithdrawing = false;

  Future<void> _withdraw() async {
    setState(() => _isWithdrawing = true);
    try {
      await WorkerRepository().withdrawBid(widget.bid.id);
      ref.invalidate(myBidsProvider);
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Bid withdrawn')),
      );
    } on ApiException catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(context)
          .showSnackBar(SnackBar(content: Text(e.message)));
    } finally {
      if (mounted) setState(() => _isWithdrawing = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final bid = widget.bid;
    final statusColor = bid.status == 'ACCEPTED'
        ? Colors.green
        : bid.status == 'REJECTED'
            ? Colors.red
            : bid.status == 'WITHDRAWN'
                ? Colors.grey
                : Colors.blue;

    return Card(
      margin: const EdgeInsets.only(bottom: 10),
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
      child: Padding(
        padding: const EdgeInsets.all(14),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Expanded(
                  child: Text(
                    'Task #${bid.taskId.length > 8 ? bid.taskId.substring(0, 8) : bid.taskId}…',
                    style: const TextStyle(fontWeight: FontWeight.w600),
                  ),
                ),
                Container(
                  padding: const EdgeInsets.symmetric(
                      horizontal: 8, vertical: 4),
                  decoration: BoxDecoration(
                    color: statusColor.withOpacity(0.1),
                    borderRadius: BorderRadius.circular(6),
                  ),
                  child: Text(
                    bid.status,
                    style: TextStyle(
                        color: statusColor,
                        fontSize: 11,
                        fontWeight: FontWeight.bold),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 8),
            Row(
              children: [
                Text(
                  '₹${bid.proposedAmount.toStringAsFixed(0)}',
                  style: TextStyle(
                      color: AppTheme.primary,
                      fontWeight: FontWeight.bold,
                      fontSize: 16),
                ),
                if (bid.estimatedHours != null) ...[
                  const SizedBox(width: 8),
                  Text('• ${bid.estimatedHours}h',
                      style: TextStyle(color: Colors.grey.shade500)),
                ],
              ],
            ),
            if (bid.message != null && bid.message!.isNotEmpty) ...[
              const SizedBox(height: 6),
              Text(bid.message!,
                  style: TextStyle(
                      color: Colors.grey.shade600, fontSize: 13)),
            ],
            if (bid.status == 'ACCEPTED') ...[
              const SizedBox(height: 10),
              ElevatedButton.icon(
                onPressed: () =>
                    context.push('/customer/task/${bid.taskId}'),
                icon: const Icon(Icons.open_in_new, size: 16),
                label: const Text('View Task'),
                style: ElevatedButton.styleFrom(
                  backgroundColor: Colors.green,
                  foregroundColor: Colors.white,
                ),
              ),
            ],
            if (bid.status == 'PENDING') ...[
              const SizedBox(height: 10),
              TextButton(
                onPressed: _isWithdrawing ? null : _withdraw,
                style: TextButton.styleFrom(foregroundColor: Colors.red),
                child: _isWithdrawing
                    ? const CircularProgressIndicator()
                    : const Text('Withdraw Bid'),
              ),
            ],
          ],
        ),
      ),
    );
  }
}
