import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../data/customer_repository.dart';
import '../data/models/task_models.dart';
import '../../auth/providers/auth_provider.dart';

final customerRepositoryProvider =
    Provider<CustomerRepository>((_) => CustomerRepository());

// My tasks list
final myTasksProvider =
    FutureProvider.family<List<Task>, String>((ref, status) async {
  final repo = ref.read(customerRepositoryProvider);
  final userId = ref.read(authStateProvider).valueOrNull?.userId;
  return repo.getMyTasks(customerId: userId, status: status);
});

// Single task
final taskDetailProvider =
    FutureProvider.family<Task, String>((ref, taskId) async {
  final repo = ref.read(customerRepositoryProvider);
  return repo.getTask(taskId);
});

// Bids for a task
final taskBidsProvider =
    FutureProvider.family<List<Bid>, String>((ref, taskId) async {
  final repo = ref.read(customerRepositoryProvider);
  return repo.getBidsForTask(taskId);
});

// Nearby workers
class NearbyWorkersParams {
  final double lat;
  final double lng;
  final double radiusKm;
  final String? domain;

  const NearbyWorkersParams({
    required this.lat,
    required this.lng,
    this.radiusKm = 10,
    this.domain,
  });
}

final nearbyWorkersProvider =
    FutureProvider.family<List<NearbyWorker>, NearbyWorkersParams>(
        (ref, params) async {
  final repo = ref.read(customerRepositoryProvider);
  return repo.getNearbyWorkers(
    lat: params.lat,
    lng: params.lng,
    radiusKm: params.radiusKm,
    domain: params.domain,
  );
});
