import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import '../constants/app_constants.dart';

class SecureStorage {
  static const _storage = FlutterSecureStorage(
    aOptions: AndroidOptions(encryptedSharedPreferences: true),
  );

  static Future<void> saveTokens({
    required String accessToken,
    required String refreshToken,
  }) async {
    await Future.wait([
      _storage.write(key: kAccessToken, value: accessToken),
      _storage.write(key: kRefreshToken, value: refreshToken),
    ]);
  }

  static Future<void> saveUserInfo({
    required String userId,
    required String role,
    required String email,
  }) async {
    await Future.wait([
      _storage.write(key: kUserId, value: userId),
      _storage.write(key: kUserRole, value: role),
      _storage.write(key: kUserEmail, value: email),
    ]);
  }

  static Future<String?> getAccessToken() =>
      _storage.read(key: kAccessToken);

  static Future<String?> getRefreshToken() =>
      _storage.read(key: kRefreshToken);

  static Future<String?> getUserRole() =>
      _storage.read(key: kUserRole);

  static Future<String?> getUserId() =>
      _storage.read(key: kUserId);

  static Future<String?> getUserEmail() =>
      _storage.read(key: kUserEmail);

  static Future<bool> hasToken() async {
    final token = await getAccessToken();
    return token != null && token.isNotEmpty;
  }

  static Future<void> clearAll() => _storage.deleteAll();
}
