import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../data/worker_repository.dart';
import '../../customer/data/models/task_models.dart';
import '../../auth/providers/auth_provider.dart';

final workerRepositoryProvider =
    Provider<WorkerRepository>((_) => WorkerRepository());

final workerProfileProvider =
    FutureProvider<Map<String, dynamic>>((ref) async {
  final repo = ref.read(workerRepositoryProvider);
  return repo.getProfile();
});

class AvailableTasksParams {
  final double lat;
  final double lng;
  final String? domain;

  const AvailableTasksParams(
      {required this.lat, required this.lng, this.domain});
}

final availableTasksProvider =
    FutureProvider.family<List<Task>, AvailableTasksParams>(
        (ref, params) async {
  final repo = ref.read(workerRepositoryProvider);
  return repo.getAvailableTasks(
      lat: params.lat, lng: params.lng, domain: params.domain);
});

final myBidsProvider = FutureProvider<List<Bid>>((ref) async {
  final repo = ref.read(workerRepositoryProvider);
  final workerId = ref.read(authStateProvider).valueOrNull?.userId ?? '';
  return repo.getMyBids(workerId);
});
