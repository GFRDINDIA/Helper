class ApiException implements Exception {
  final String message;
  final String? errorCode;
  final int? statusCode;

  const ApiException({
    required this.message,
    this.errorCode,
    this.statusCode,
  });

  @override
  String toString() => message;

  factory ApiException.fromDioError(dynamic error) {
    if (error?.response != null) {
      final data = error.response?.data;
      final statusCode = error.response?.statusCode as int?;

      if (data is Map<String, dynamic>) {
        final msg = data['message'] as String? ??
            data['error'] as String? ??
            'Request failed';
        final code = data['errorCode'] as String?;
        return ApiException(
            message: msg, errorCode: code, statusCode: statusCode);
      }
      return ApiException(
          message: 'Server error ($statusCode)', statusCode: statusCode);
    }
    return const ApiException(message: 'Network error. Check your connection.');
  }
}
