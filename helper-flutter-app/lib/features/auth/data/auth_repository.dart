import '../../../core/network/dio_client.dart';
import '../../../core/network/api_exception.dart';
import '../../../core/constants/api_constants.dart';
import 'models/auth_models.dart';

class AuthRepository {
  final _dio = DioClient.instance;

  Future<void> register(RegisterRequest request) async {
    try {
      await _dio.post(ApiEndpoints.register, data: request.toJson());
    } catch (e) {
      throw DioClient.parseError(e);
    }
  }

  Future<LoginResponse> login(LoginRequest request) async {
    try {
      final response = await _dio.post(
        ApiEndpoints.login,
        data: request.toJson(),
      );
      return LoginResponse.fromJson(response.data as Map<String, dynamic>);
    } catch (e) {
      throw DioClient.parseError(e);
    }
  }

  Future<void> verifyEmail(String email, String otp) async {
    try {
      await _dio.post(ApiEndpoints.verifyEmail, data: {'email': email, 'otp': otp});
    } catch (e) {
      throw DioClient.parseError(e);
    }
  }

  Future<void> resendOtp(String email) async {
    try {
      await _dio.post(ApiEndpoints.resendOtp, data: {'email': email});
    } catch (e) {
      throw DioClient.parseError(e);
    }
  }

  Future<void> logout() async {
    try {
      await _dio.post(ApiEndpoints.logout);
    } catch (_) {}
  }
}
