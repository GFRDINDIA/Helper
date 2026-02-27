import '../../../core/network/dio_client.dart';
import '../../../core/constants/api_constants.dart';
import 'models/task_models.dart';

class CustomerRepository {
  final _dio = DioClient.instance;

  Future<Map<String, dynamic>> getProfile() async {
    try {
      final r = await _dio.get(ApiEndpoints.customerMe);
      return (r.data['data'] ?? r.data) as Map<String, dynamic>;
    } catch (e) {
      throw DioClient.parseError(e);
    }
  }

  Future<void> updateProfile(Map<String, dynamic> data) async {
    try {
      await _dio.put(ApiEndpoints.customerProfile, data: data);
    } catch (e) {
      throw DioClient.parseError(e);
    }
  }

  Future<Task> createTask(CreateTaskRequest request) async {
    try {
      final r = await _dio.post(ApiEndpoints.tasks, data: request.toJson());
      final data = r.data['data'] ?? r.data;
      return Task.fromJson(data as Map<String, dynamic>);
    } catch (e) {
      throw DioClient.parseError(e);
    }
  }

  Future<List<Task>> getMyTasks({String? customerId, String? status}) async {
    try {
      final params = <String, dynamic>{};
      if (customerId != null) params['customerId'] = customerId;
      if (status != null && status != 'ALL') params['status'] = status;

      final r = await _dio.get(ApiEndpoints.tasks, queryParameters: params);
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

  Future<Task> getTask(String taskId) async {
    try {
      final r = await _dio.get(ApiEndpoints.taskById(taskId));
      final data = r.data['data'] ?? r.data;
      return Task.fromJson(data as Map<String, dynamic>);
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

  Future<List<Bid>> getBidsForTask(String taskId) async {
    try {
      final r = await _dio.get(ApiEndpoints.bidsByTask(taskId));
      final data = r.data['data'] ?? r.data;
      final list = data is List ? data : [];
      return list
          .map((e) => Bid.fromJson(e as Map<String, dynamic>))
          .toList();
    } catch (e) {
      throw DioClient.parseError(e);
    }
  }

  Future<void> acceptBid(String bidId) async {
    try {
      await _dio.put(ApiEndpoints.acceptBid(bidId));
    } catch (e) {
      throw DioClient.parseError(e);
    }
  }

  Future<List<NearbyWorker>> getNearbyWorkers({
    required double lat,
    required double lng,
    double radiusKm = 10,
    String? domain,
  }) async {
    try {
      final params = <String, dynamic>{
        'lat': lat,
        'lng': lng,
        'radiusKm': radiusKm,
      };
      if (domain != null) params['domain'] = domain;

      final r = await _dio.get(ApiEndpoints.workersNearby,
          queryParameters: params);
      final data = r.data['data'] ?? r.data;
      final list = data is List ? data : [];
      return list
          .map((e) => NearbyWorker.fromJson(e as Map<String, dynamic>))
          .toList();
    } catch (e) {
      throw DioClient.parseError(e);
    }
  }
}
