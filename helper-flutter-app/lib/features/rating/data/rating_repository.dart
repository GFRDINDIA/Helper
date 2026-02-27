import '../../../core/network/dio_client.dart';
import '../../../core/constants/api_constants.dart';

class RatingRepository {
  final _dio = DioClient.instance;

  Future<void> submitRating({
    required String taskId,
    required String rateeId,
    required String type, // CUSTOMER_TO_WORKER | WORKER_TO_CUSTOMER
    required int score,
    String? feedback,
  }) async {
    try {
      await _dio.post(ApiEndpoints.ratings, data: {
        'taskId': taskId,
        'rateeId': rateeId,
        'type': type,
        'score': score,
        if (feedback != null && feedback.isNotEmpty) 'feedback': feedback,
      });
    } catch (e) {
      throw DioClient.parseError(e);
    }
  }

  Future<List<Map<String, dynamic>>> getWorkerRatings(String workerId) async {
    try {
      final r = await _dio.get(ApiEndpoints.ratingsByWorker(workerId));
      final data = r.data['data'] ?? r.data;
      final list = data is List ? data : [];
      return list.map((e) => e as Map<String, dynamic>).toList();
    } catch (e) {
      throw DioClient.parseError(e);
    }
  }

  Future<List<Map<String, dynamic>>> getCustomerRatings(
      String customerId) async {
    try {
      final r = await _dio.get(ApiEndpoints.ratingsByCustomer(customerId));
      final data = r.data['data'] ?? r.data;
      final list = data is List ? data : [];
      return list.map((e) => e as Map<String, dynamic>).toList();
    } catch (e) {
      throw DioClient.parseError(e);
    }
  }
}
