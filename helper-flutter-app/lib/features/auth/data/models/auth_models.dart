class RegisterRequest {
  final String fullName;
  final String email;
  final String phone;
  final String password;
  final String role; // CUSTOMER | WORKER

  const RegisterRequest({
    required this.fullName,
    required this.email,
    required this.phone,
    required this.password,
    required this.role,
  });

  Map<String, dynamic> toJson() => {
        'fullName': fullName,
        'email': email,
        'phone': phone,
        'password': password,
        'role': role,
      };
}

class LoginRequest {
  final String email;
  final String password;

  const LoginRequest({required this.email, required this.password});

  Map<String, dynamic> toJson() => {'email': email, 'password': password};
}

class AuthUser {
  final String id;
  final String fullName;
  final String email;
  final String phone;
  final String role;
  final bool emailVerified;
  final String verificationStatus;

  const AuthUser({
    required this.id,
    required this.fullName,
    required this.email,
    required this.phone,
    required this.role,
    required this.emailVerified,
    required this.verificationStatus,
  });

  factory AuthUser.fromJson(Map<String, dynamic> json) => AuthUser(
        id: json['id']?.toString() ?? '',
        fullName: json['fullName'] as String? ?? '',
        email: json['email'] as String? ?? '',
        phone: json['phone'] as String? ?? '',
        role: json['role'] as String? ?? '',
        emailVerified: json['emailVerified'] as bool? ?? false,
        verificationStatus: json['verificationStatus'] as String? ?? '',
      );
}

class LoginResponse {
  final String accessToken;
  final String refreshToken;
  final AuthUser user;

  const LoginResponse({
    required this.accessToken,
    required this.refreshToken,
    required this.user,
  });

  factory LoginResponse.fromJson(Map<String, dynamic> json) {
    final data = json['data'] as Map<String, dynamic>? ?? json;
    return LoginResponse(
      accessToken: data['accessToken'] as String? ?? '',
      refreshToken: data['refreshToken'] as String? ?? '',
      user: AuthUser.fromJson(data['user'] as Map<String, dynamic>? ?? {}),
    );
  }
}
