import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../features/auth/providers/auth_provider.dart';
import '../../features/auth/screens/splash_screen.dart';
import '../../features/auth/screens/onboarding_screen.dart';
import '../../features/auth/screens/login_screen.dart';
import '../../features/auth/screens/register_screen.dart';
import '../../features/auth/screens/otp_screen.dart';
import '../../features/customer/screens/customer_home_screen.dart';
import '../../features/customer/screens/post_task_screen.dart';
import '../../features/customer/screens/task_detail_screen.dart';
import '../../features/customer/screens/worker_profile_screen.dart';
import '../../features/worker/screens/worker_home_screen.dart';
import '../../features/worker/screens/bid_screen.dart';
import '../../features/profile/screens/customer_profile_screen.dart';
import '../../features/profile/screens/worker_profile_edit_screen.dart';
import '../../features/profile/screens/kyc_upload_screen.dart';
import '../../features/payment/screens/payment_screen.dart';
import '../../features/payment/screens/transaction_history_screen.dart';
import '../../features/rating/screens/rating_screen.dart';

final routerProvider = Provider<GoRouter>((ref) {
  final authState = ref.watch(authStateProvider);

  return GoRouter(
    initialLocation: '/splash',
    redirect: (context, state) {
      final isLoading = authState.isLoading;
      final isLoggedIn = authState.valueOrNull?.isLoggedIn ?? false;
      final role = authState.valueOrNull?.role;
      final path = state.uri.path;

      if (isLoading) return null;

      final publicRoutes = ['/splash', '/onboarding', '/login', '/register', '/otp'];
      final isPublic = publicRoutes.any((r) => path.startsWith(r));

      if (!isLoggedIn && !isPublic) return '/login';
      if (isLoggedIn && (path == '/login' || path == '/register' || path == '/onboarding')) {
        return role == 'WORKER' ? '/worker' : '/customer';
      }

      return null;
    },
    routes: [
      GoRoute(path: '/splash', builder: (_, __) => const SplashScreen()),
      GoRoute(path: '/onboarding', builder: (_, __) => const OnboardingScreen()),
      GoRoute(path: '/login', builder: (_, __) => const LoginScreen()),
      GoRoute(path: '/register', builder: (_, __) => const RegisterScreen()),
      GoRoute(
        path: '/otp',
        builder: (_, state) {
          final email = state.extra as String? ?? '';
          return OtpScreen(email: email);
        },
      ),
      // Customer routes
      GoRoute(
        path: '/customer',
        builder: (_, __) => const CustomerHomeScreen(),
        routes: [
          GoRoute(
            path: 'post-task',
            builder: (_, __) => const PostTaskScreen(),
          ),
          GoRoute(
            path: 'task/:taskId',
            builder: (_, state) =>
                TaskDetailScreen(taskId: state.pathParameters['taskId']!),
          ),
          GoRoute(
            path: 'worker/:workerId',
            builder: (_, state) =>
                WorkerProfileScreen(workerId: state.pathParameters['workerId']!),
          ),
          GoRoute(
            path: 'profile',
            builder: (_, __) => const CustomerProfileScreen(),
          ),
        ],
      ),
      // Worker routes
      GoRoute(
        path: '/worker',
        builder: (_, __) => const WorkerHomeScreen(),
        routes: [
          GoRoute(
            path: 'bid/:taskId',
            builder: (_, state) =>
                BidScreen(taskId: state.pathParameters['taskId']!),
          ),
          GoRoute(
            path: 'profile',
            builder: (_, __) => const WorkerProfileEditScreen(),
          ),
          GoRoute(
            path: 'kyc',
            builder: (_, __) => const KycUploadScreen(),
          ),
        ],
      ),
      // Shared routes
      GoRoute(
        path: '/payment/:taskId',
        builder: (_, state) =>
            PaymentScreen(taskId: state.pathParameters['taskId']!),
      ),
      GoRoute(
        path: '/transactions',
        builder: (_, __) => const TransactionHistoryScreen(),
      ),
      GoRoute(
        path: '/rating/:taskId',
        builder: (_, state) =>
            RatingScreen(taskId: state.pathParameters['taskId']!),
      ),
      GoRoute(
        path: '/kyc',
        builder: (_, __) => const KycUploadScreen(),
      ),
    ],
    errorBuilder: (_, state) => Scaffold(
      body: Center(child: Text('Page not found: ${state.error}')),
    ),
  );
});
