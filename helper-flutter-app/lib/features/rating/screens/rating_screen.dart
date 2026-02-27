import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../data/rating_repository.dart';
import '../../auth/providers/auth_provider.dart';
import '../../customer/providers/customer_provider.dart';
import '../../../shared/theme/app_theme.dart';
import '../../../shared/widgets/helper_button.dart';
import '../../../core/network/api_exception.dart';

class RatingScreen extends ConsumerStatefulWidget {
  final String taskId;

  const RatingScreen({super.key, required this.taskId});

  @override
  ConsumerState<RatingScreen> createState() => _RatingScreenState();
}

class _RatingScreenState extends ConsumerState<RatingScreen> {
  int _score = 5;
  final _feedbackCtrl = TextEditingController();
  bool _isLoading = false;
  String? _errorMessage;

  @override
  void dispose() {
    _feedbackCtrl.dispose();
    super.dispose();
  }

  Future<void> _submit() async {
    setState(() {
      _isLoading = true;
      _errorMessage = null;
    });

    try {
      final taskAsync = ref.read(taskDetailProvider(widget.taskId));
      final task = taskAsync.valueOrNull;
      if (task == null) throw Exception('Task not loaded');

      final authState = ref.read(authStateProvider).valueOrNull;
      final role = authState?.role ?? '';

      String rateeId;
      String ratingType;

      if (role == 'CUSTOMER') {
        rateeId = task.workerId ?? '';
        ratingType = 'CUSTOMER_TO_WORKER';
      } else {
        rateeId = task.customerId ?? '';
        ratingType = 'WORKER_TO_CUSTOMER';
      }

      await RatingRepository().submitRating(
        taskId: widget.taskId,
        rateeId: rateeId,
        type: ratingType,
        score: _score,
        feedback: _feedbackCtrl.text.trim(),
      );

      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
            content: Text('Rating submitted. Thank you.'),
            backgroundColor: Colors.green),
      );

      // Go back to home
      context.go(role == 'WORKER' ? '/worker' : '/customer');
    } on ApiException catch (e) {
      setState(() => _errorMessage = e.message);
    } catch (e) {
      setState(() => _errorMessage = e.toString());
    } finally {
      if (mounted) setState(() => _isLoading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final role = ref.watch(authStateProvider).valueOrNull?.role ?? '';
    final ratingTarget =
        role == 'CUSTOMER' ? 'the Worker' : 'the Customer';

    return Scaffold(
      appBar: AppBar(title: const Text('Rate Your Experience')),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(24),
        child: Column(
          children: [
            const SizedBox(height: 24),
            Container(
              width: 100,
              height: 100,
              decoration: BoxDecoration(
                color: AppTheme.primary.withOpacity(0.1),
                shape: BoxShape.circle,
              ),
              child: Icon(
                Icons.star_rounded,
                size: 56,
                color: AppTheme.primary,
              ),
            ),
            const SizedBox(height: 24),
            Text(
              'How was $ratingTarget?',
              style: const TextStyle(
                  fontSize: 22, fontWeight: FontWeight.bold),
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 8),
            Text(
              'Your feedback helps build trust in the community.',
              style: TextStyle(color: Colors.grey.shade600),
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 40),
            // Star rating
            Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: List.generate(5, (i) {
                final starScore = i + 1;
                return GestureDetector(
                  onTap: () => setState(() => _score = starScore),
                  child: AnimatedContainer(
                    duration: const Duration(milliseconds: 150),
                    padding: const EdgeInsets.symmetric(horizontal: 6),
                    child: Icon(
                      starScore <= _score ? Icons.star_rounded : Icons.star_border_rounded,
                      size: 48,
                      color: starScore <= _score
                          ? Colors.amber.shade500
                          : Colors.grey.shade300,
                    ),
                  ),
                );
              }),
            ),
            const SizedBox(height: 12),
            Text(
              _scoreLabel(_score),
              style: TextStyle(
                  color: AppTheme.primary,
                  fontWeight: FontWeight.bold,
                  fontSize: 16),
            ),
            const SizedBox(height: 32),
            if (_errorMessage != null)
              Container(
                padding: const EdgeInsets.all(12),
                margin: const EdgeInsets.only(bottom: 16),
                decoration: BoxDecoration(
                  color: Colors.red.shade50,
                  borderRadius: BorderRadius.circular(8),
                  border: Border.all(color: Colors.red.shade200),
                ),
                child: Text(_errorMessage!,
                    style: TextStyle(color: Colors.red.shade700)),
              ),
            TextFormField(
              controller: _feedbackCtrl,
              maxLines: 4,
              decoration: InputDecoration(
                hintText: 'Share your experience (optional)...',
                border: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(12)),
                enabledBorder: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(12),
                  borderSide: BorderSide(color: Colors.grey.shade300),
                ),
                focusedBorder: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(12),
                  borderSide: BorderSide(color: AppTheme.primary, width: 2),
                ),
                filled: true,
                fillColor: Colors.grey.shade50,
              ),
            ),
            const SizedBox(height: 32),
            HelperButton(
              label: 'Submit Rating',
              isLoading: _isLoading,
              onPressed: _submit,
            ),
            const SizedBox(height: 16),
            TextButton(
              onPressed: () =>
                  context.go(role == 'WORKER' ? '/worker' : '/customer'),
              child: Text('Skip',
                  style: TextStyle(color: Colors.grey.shade500)),
            ),
          ],
        ),
      ),
    );
  }

  String _scoreLabel(int score) {
    switch (score) {
      case 1:
        return 'Poor';
      case 2:
        return 'Fair';
      case 3:
        return 'Good';
      case 4:
        return 'Very Good';
      case 5:
        return 'Excellent';
      default:
        return '';
    }
  }
}
