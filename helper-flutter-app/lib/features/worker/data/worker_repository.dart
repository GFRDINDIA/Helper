import '../../../core/network/dio_client.dart';
import '../../../core/constants/api_constants.dart';
import '../../customer/data/models/task_models.dart';
import 'models/worker_models.dart';

class WorkerRepository {
  final _dio = DioClient.instance;

  Future<Map<String, dynamic>> getProfile() async {
    try {
      final r = await _dio.get(ApiEndpoints.workerMe);
      return (r.data['data'] ?? r.data) as Map<String, dynamic>;
    } catch (e) {
      throw DioClient.parseError(e);
    }
  }

  Future<void> updateProfile(WorkerProfileRequest request) async {
    try {
      await _dio.put(ApiEndpoints.workerProfile, data: request.toJson());
    } catch (e) {
      throw DioClient.parseError(e);
    }
  }

  Future<List<Task>> getAvailableTasks({
    required double lat,
    required double lng,
    String? domain,
  }) async {
    try {
      final params = <String, dynamic>{'lat': lat, 'lng': lng};
      if (domain != null) params['domain'] = domain;
      final r = await _dio.get(ApiEndpoints.tasksAvailable,
          queryParameters: params);
      final data = r.data['data'] ?? r.data;
      final list = data is List
          ? data
          : (data['content'] as List<dynamic>? ?? []);
      return list
          .map((e) => Task.fromJson(e as Map<String, dynamic>))
          .toList();
    } catch (e) {
      throw DioClient.parseError(e);
    }
  }

  Future<Bid> submitBid(SubmitBidRequest request) async {
    try {
      final r = await _dio.post(ApiEndpoints.bids, data: request.toJson());
      final data = r.data['data'] ?? r.data;
      return Bid.fromJson(data as Map<String, dynamic>);
    } catch (e) {
      throw DioClient.parseError(e);
    }
  }

  Future<List<Bid>> getMyBids(String workerId) async {
    try {
      final r = await _dio.get(ApiEndpoints.bidsByWorker(workerId));
      final data = r.data['data'] ?? r.data;
      final list = data is List ? data : [];
      return list
          .map((e) => Bid.fromJson(e as Map<String, dynamic>))
          .toList();
    } catch (e) {
      throw DioClient.parseError(e);
    }
  }

  Future<void> withdrawBid(String bidId) async {
    try {
      await _dio.put(ApiEndpoints.withdrawBid(bidId));
    } catch (e) {
      throw DioClient.parseError(e);
    }
  }

  Future<void> updateTaskStatus(String taskId, String status) async {
    try {
      await _dio.put(ApiEndpoints.taskStatus(taskId), data: {'status': status});
    } catch (e) {
      throw DioClient.parseError(e);
    }
  }

  Future<Task> getTask(String taskId) async {
    try {
      final r = await _dio.get(ApiEndpoints.taskById(taskId));
      final data = r.data['data'] ?? r.data;
      return Task.fromJson(data as Map<String, dynamic>);
    } catch (e) {
      throw DioClient.parseError(e);
    }
  }
}
