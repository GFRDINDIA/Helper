import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:geolocator/geolocator.dart';
import '../providers/customer_provider.dart';
import '../data/models/task_models.dart';
import '../../auth/providers/auth_provider.dart';
import '../../../shared/theme/app_theme.dart';
import '../../../shared/widgets/loading_overlay.dart';
import '../../../core/constants/app_constants.dart';

class CustomerHomeScreen extends ConsumerStatefulWidget {
  const CustomerHomeScreen({super.key});

  @override
  ConsumerState<CustomerHomeScreen> createState() => _CustomerHomeScreenState();
}

class _CustomerHomeScreenState extends ConsumerState<CustomerHomeScreen>
    with SingleTickerProviderStateMixin {
  late TabController _tabController;
  String _taskFilter = 'ALL';
  Position? _position;

  final _filters = ['ALL', 'OPEN', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'];

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 2, vsync: this);
    _fetchLocation();
  }

  Future<void> _fetchLocation() async {
    try {
      final permission = await Geolocator.requestPermission();
      if (permission == LocationPermission.denied) return;
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
            const Text('Helper', style: TextStyle(fontWeight: FontWeight.bold)),
            Text(
              'Hi, ${authState?.email?.split('@').first ?? "there"}',
              style: TextStyle(fontSize: 12, color: Colors.grey.shade600),
            ),
          ],
        ),
        actions: [
          IconButton(
            icon: const Icon(Icons.person_outline),
            onPressed: () => context.push('/customer/profile'),
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
            Tab(icon: Icon(Icons.assignment_outlined), text: 'My Tasks'),
            Tab(icon: Icon(Icons.search_rounded), text: 'Nearby Workers'),
          ],
        ),
      ),
      body: TabBarView(
        controller: _tabController,
        children: [
          _MyTasksTab(filter: _taskFilter, onFilterChange: (f) {
            setState(() => _taskFilter = f);
          }),
          _NearbyWorkersTab(position: _position),
        ],
      ),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: () => context.push('/customer/post-task'),
        backgroundColor: AppTheme.primary,
        icon: const Icon(Icons.add, color: Colors.white),
        label: const Text('Post Task',
            style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold)),
      ),
    );
  }
}

class _MyTasksTab extends ConsumerWidget {
  final String filter;
  final ValueChanged<String> onFilterChange;

  const _MyTasksTab({required this.filter, required this.onFilterChange});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final tasksAsync = ref.watch(myTasksProvider(filter));
    final filters = ['ALL', 'OPEN', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'];

    return Column(
      children: [
        // Filter chips
        SizedBox(
          height: 56,
          child: ListView.builder(
            scrollDirection: Axis.horizontal,
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
            itemCount: filters.length,
            itemBuilder: (_, i) {
              final f = filters[i];
              final selected = f == filter;
              return GestureDetector(
                onTap: () => onFilterChange(f),
                child: AnimatedContainer(
                  duration: const Duration(milliseconds: 200),
                  margin: const EdgeInsets.only(right: 8),
                  padding:
                      const EdgeInsets.symmetric(horizontal: 16, vertical: 6),
                  decoration: BoxDecoration(
                    color: selected ? AppTheme.primary : Colors.white,
                    border: Border.all(
                        color: selected ? AppTheme.primary : Colors.grey.shade300),
                    borderRadius: BorderRadius.circular(20),
                  ),
                  child: Text(
                    f.replaceAll('_', ' '),
                    style: TextStyle(
                      color: selected ? Colors.white : Colors.grey.shade700,
                      fontWeight:
                          selected ? FontWeight.bold : FontWeight.normal,
                      fontSize: 13,
                    ),
                  ),
                ),
              );
            },
          ),
        ),
        Expanded(
          child: tasksAsync.when(
            data: (tasks) => tasks.isEmpty
                ? _EmptyState(
                    icon: Icons.assignment_outlined,
                    message: 'No tasks yet.\nTap + to post your first task.',
                  )
                : RefreshIndicator(
                    onRefresh: () =>
                        ref.refresh(myTasksProvider(filter).future),
                    child: ListView.builder(
                      padding: const EdgeInsets.all(16),
                      itemCount: tasks.length,
                      itemBuilder: (_, i) => _TaskCard(task: tasks[i]),
                    ),
                  ),
            loading: () => const LoadingOverlay(),
            error: (e, _) => Center(child: Text('Error: $e')),
          ),
        ),
      ],
    );
  }
}

class _TaskCard extends StatelessWidget {
  final Task task;

  const _TaskCard({required this.task});

  @override
  Widget build(BuildContext context) {
    final statusColor = _statusColor(task.status);
    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
      child: InkWell(
        borderRadius: BorderRadius.circular(12),
        onTap: () => context.push('/customer/task/${task.id}'),
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                children: [
                  Text(
                    kDomainIcons[task.domain] ?? '🛠️',
                    style: const TextStyle(fontSize: 20),
                  ),
                  const SizedBox(width: 8),
                  Expanded(
                    child: Text(
                      task.title,
                      style: const TextStyle(
                          fontWeight: FontWeight.bold, fontSize: 15),
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                    ),
                  ),
                  Container(
                    padding: const EdgeInsets.symmetric(
                        horizontal: 8, vertical: 4),
                    decoration: BoxDecoration(
                      color: statusColor.withOpacity(0.1),
                      borderRadius: BorderRadius.circular(8),
                    ),
                    child: Text(
                      task.status.replaceAll('_', ' '),
                      style: TextStyle(
                          color: statusColor,
                          fontSize: 11,
                          fontWeight: FontWeight.bold),
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 8),
              Text(
                task.description,
                maxLines: 2,
                overflow: TextOverflow.ellipsis,
                style: TextStyle(color: Colors.grey.shade600, fontSize: 13),
              ),
              const SizedBox(height: 8),
              Row(
                children: [
                  Icon(Icons.location_on_outlined,
                      size: 14, color: Colors.grey.shade500),
                  const SizedBox(width: 4),
                  Text(task.location,
                      style: TextStyle(
                          color: Colors.grey.shade500, fontSize: 12)),
                  const Spacer(),
                  Text(
                    task.displayBudget,
                    style: TextStyle(
                        color: AppTheme.primary,
                        fontWeight: FontWeight.bold,
                        fontSize: 14),
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }

  Color _statusColor(String status) {
    switch (status) {
      case 'OPEN':
        return Colors.blue;
      case 'IN_PROGRESS':
        return Colors.orange;
      case 'COMPLETED':
        return Colors.green;
      case 'CANCELLED':
        return Colors.red;
      case 'PAYMENT_DONE':
        return Colors.teal;
      case 'CLOSED':
        return Colors.grey;
      default:
        return Colors.grey;
    }
  }
}

class _NearbyWorkersTab extends ConsumerWidget {
  final Position? position;

  const _NearbyWorkersTab({this.position});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    if (position == null) {
      return Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(Icons.location_off, size: 48, color: Colors.grey.shade400),
            const SizedBox(height: 16),
            Text('Enable location to see nearby workers',
                style: TextStyle(color: Colors.grey.shade600)),
          ],
        ),
      );
    }

    final workersAsync = ref.watch(nearbyWorkersProvider(NearbyWorkersParams(
      lat: position!.latitude,
      lng: position!.longitude,
    )));

    return workersAsync.when(
      data: (workers) => workers.isEmpty
          ? _EmptyState(
              icon: Icons.engineering_outlined,
              message: 'No workers found nearby.',
            )
          : RefreshIndicator(
              onRefresh: () => ref.refresh(nearbyWorkersProvider(
                NearbyWorkersParams(
                    lat: position!.latitude, lng: position!.longitude),
              ).future),
              child: ListView.builder(
                padding: const EdgeInsets.all(16),
                itemCount: workers.length,
                itemBuilder: (_, i) => _WorkerCard(worker: workers[i]),
              ),
            ),
      loading: () => const LoadingOverlay(),
      error: (e, _) => Center(child: Text('Error: $e')),
    );
  }
}

class _WorkerCard extends StatelessWidget {
  final NearbyWorker worker;

  const _WorkerCard({required this.worker});

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
      child: InkWell(
        borderRadius: BorderRadius.circular(12),
        onTap: () => context.push('/customer/worker/${worker.workerId}'),
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Row(
            children: [
              CircleAvatar(
                radius: 28,
                backgroundColor: AppTheme.primary.withOpacity(0.1),
                child: Text(
                  worker.fullName.isNotEmpty
                      ? worker.fullName[0].toUpperCase()
                      : '?',
                  style: TextStyle(
                      fontSize: 22,
                      fontWeight: FontWeight.bold,
                      color: AppTheme.primary),
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(worker.fullName,
                        style: const TextStyle(fontWeight: FontWeight.bold)),
                    const SizedBox(height: 4),
                    Row(
                      children: [
                        Icon(Icons.star, size: 14, color: Colors.amber.shade600),
                        const SizedBox(width: 2),
                        Text(
                          worker.averageRating?.toStringAsFixed(1) ?? 'New',
                          style: TextStyle(
                              fontSize: 12, color: Colors.grey.shade600),
                        ),
                        const SizedBox(width: 8),
                        if (worker.distanceKm != null)
                          Text(
                            '${worker.distanceKm!.toStringAsFixed(1)} km away',
                            style: TextStyle(
                                fontSize: 12, color: Colors.grey.shade500),
                          ),
                      ],
                    ),
                    const SizedBox(height: 6),
                    Wrap(
                      spacing: 4,
                      children: worker.domains.take(3).map((d) {
                        return Container(
                          padding: const EdgeInsets.symmetric(
                              horizontal: 6, vertical: 2),
                          decoration: BoxDecoration(
                            color: Colors.blue.shade50,
                            borderRadius: BorderRadius.circular(4),
                          ),
                          child: Text(
                            kDomainLabels[d] ?? d,
                            style: TextStyle(
                                fontSize: 10, color: Colors.blue.shade700),
                          ),
                        );
                      }).toList(),
                    ),
                  ],
                ),
              ),
              const Icon(Icons.chevron_right),
            ],
          ),
        ),
      ),
    );
  }
}

class _EmptyState extends StatelessWidget {
  final IconData icon;
  final String message;

  const _EmptyState({required this.icon, required this.message});

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Icon(icon, size: 64, color: Colors.grey.shade300),
          const SizedBox(height: 16),
          Text(
            message,
            textAlign: TextAlign.center,
            style: TextStyle(color: Colors.grey.shade500, height: 1.5),
          ),
        ],
      ),
    );
  }
}
