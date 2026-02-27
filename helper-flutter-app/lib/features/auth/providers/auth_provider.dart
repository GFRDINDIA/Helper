import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/storage/secure_storage.dart';
import '../data/auth_repository.dart';
import '../data/models/auth_models.dart';

class AuthState {
  final bool isLoggedIn;
  final String? userId;
  final String? role;
  final String? email;

  const AuthState({
    this.isLoggedIn = false,
    this.userId,
    this.role,
    this.email,
  });

  AuthState copyWith({
    bool? isLoggedIn,
    String? userId,
    String? role,
    String? email,
  }) =>
      AuthState(
        isLoggedIn: isLoggedIn ?? this.isLoggedIn,
        userId: userId ?? this.userId,
        role: role ?? this.role,
        email: email ?? this.email,
      );
}

final authRepositoryProvider = Provider<AuthRepository>((_) => AuthRepository());

class AuthNotifier extends AsyncNotifier<AuthState> {
  @override
  Future<AuthState> build() async {
    final hasToken = await SecureStorage.hasToken();
    if (!hasToken) return const AuthState(isLoggedIn: false);

    final role = await SecureStorage.getUserRole();
    final userId = await SecureStorage.getUserId();
    final email = await SecureStorage.getUserEmail();

    return AuthState(
      isLoggedIn: true,
      role: role,
      userId: userId,
      email: email,
    );
  }

  Future<void> register({
    required String fullName,
    required String email,
    required String phone,
    required String password,
    required String role,
  }) async {
    final repo = ref.read(authRepositoryProvider);
    await repo.register(RegisterRequest(
      fullName: fullName,
      email: email,
      phone: phone,
      password: password,
      role: role,
    ));
    // After register, OTP is sent — don't update auth state yet
  }

  Future<void> login(String email, String password) async {
    state = const AsyncLoading();
    final repo = ref.read(authRepositoryProvider);
    try {
      final response = await repo.login(LoginRequest(
        email: email,
        password: password,
      ));

      await SecureStorage.saveTokens(
        accessToken: response.accessToken,
        refreshToken: response.refreshToken,
      );
      await SecureStorage.saveUserInfo(
        userId: response.user.id,
        role: response.user.role,
        email: response.user.email,
      );

      state = AsyncData(AuthState(
        isLoggedIn: true,
        userId: response.user.id,
        role: response.user.role,
        email: response.user.email,
      ));
    } catch (e, st) {
      state = AsyncError(e, st);
    }
  }

  Future<void> verifyOtp(String email, String otp) async {
    final repo = ref.read(authRepositoryProvider);
    await repo.verifyEmail(email, otp);
    // OTP verified — user can now login
  }

  Future<void> logout() async {
    final repo = ref.read(authRepositoryProvider);
    await repo.logout();
    await SecureStorage.clearAll();
    state = const AsyncData(AuthState(isLoggedIn: false));
  }
}

final authStateProvider =
    AsyncNotifierProvider<AuthNotifier, AuthState>(AuthNotifier.new);
