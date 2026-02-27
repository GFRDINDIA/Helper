import '../../../core/network/dio_client.dart';
import '../../../core/constants/api_constants.dart';

class PaymentInfo {
  final String id;
  final double amount;
  final double commission;
  final double tax;
  final double tip;
  final double totalAmount;
  final String status;
  final String? method;
  final String? taskId;
  final String? customerId;
  final String? workerId;

  const PaymentInfo({
    required this.id,
    required this.amount,
    required this.commission,
    required this.tax,
    required this.tip,
    required this.totalAmount,
    required this.status,
    this.method,
    this.taskId,
    this.customerId,
    this.workerId,
  });

  factory PaymentInfo.fromJson(Map<String, dynamic> json) => PaymentInfo(
        id: json['id']?.toString() ?? '',
        amount: (json['amount'] as num?)?.toDouble() ?? 0,
        commission: (json['commission'] as num?)?.toDouble() ?? 0,
        tax: (json['tax'] as num?)?.toDouble() ?? 0,
        tip: (json['tip'] as num?)?.toDouble() ?? 0,
        totalAmount: (json['totalAmount'] as num?)?.toDouble() ??
            (json['amount'] as num?)?.toDouble() ??
            0,
        status: json['status'] as String? ?? '',
        method: json['method'] as String?,
        taskId: json['taskId']?.toString(),
        customerId: json['customerId']?.toString(),
        workerId: json['workerId']?.toString(),
      );
}

class PaymentRepository {
  final _dio = DioClient.instance;

  Future<PaymentInfo> initiatePayment({
    required String customerId,
    required String workerId,
    required double finalPrice,
  }) async {
    try {
      final r = await _dio.post(
        ApiEndpoints.paymentsInitiate,
        queryParameters: {
          'customerId': customerId,
          'workerId': workerId,
          'finalPrice': finalPrice,
        },
      );
      final data = r.data['data'] ?? r.data;
      return PaymentInfo.fromJson(data as Map<String, dynamic>);
    } catch (e) {
      throw DioClient.parseError(e);
    }
  }

  Future<void> confirmPayment(String paymentId, String method) async {
    try {
      await _dio.post(ApiEndpoints.paymentPay(paymentId),
          data: {'method': method});
    } catch (e) {
      throw DioClient.parseError(e);
    }
  }

  Future<void> confirmCash(String paymentId) async {
    try {
      await _dio.post(ApiEndpoints.paymentConfirmCash(paymentId));
    } catch (e) {
      throw DioClient.parseError(e);
    }
  }

  Future<void> addTip(String paymentId, double amount) async {
    try {
      await _dio.post(ApiEndpoints.paymentTip(paymentId),
          data: {'amount': amount});
    } catch (e) {
      throw DioClient.parseError(e);
    }
  }

  Future<List<PaymentInfo>> getHistory(String userId) async {
    try {
      final r = await _dio
          .get(ApiEndpoints.paymentHistory, queryParameters: {'userId': userId});
      final data = r.data['data'] ?? r.data;
      final list = data is List ? data : [];
      return list
          .map((e) => PaymentInfo.fromJson(e as Map<String, dynamic>))
          .toList();
    } catch (e) {
      throw DioClient.parseError(e);
    }
  }
}
