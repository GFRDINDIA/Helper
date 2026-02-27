import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../data/payment_repository.dart';
import '../../auth/providers/auth_provider.dart';
import '../../../shared/theme/app_theme.dart';

final _paymentHistoryProvider = FutureProvider<List<PaymentInfo>>((ref) async {
  final userId = ref.read(authStateProvider).valueOrNull?.userId ?? '';
  return PaymentRepository().getHistory(userId);
});

class TransactionHistoryScreen extends ConsumerWidget {
  const TransactionHistoryScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final historyAsync = ref.watch(_paymentHistoryProvider);

    return Scaffold(
      appBar: AppBar(title: const Text('Transaction History')),
      body: historyAsync.when(
        data: (history) => history.isEmpty
            ? Center(
                child: Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    Icon(Icons.receipt_long_outlined,
                        size: 64, color: Colors.grey.shade300),
                    const SizedBox(height: 16),
                    Text('No transactions yet',
                        style: TextStyle(color: Colors.grey.shade500)),
                  ],
                ),
              )
            : ListView.builder(
                padding: const EdgeInsets.all(16),
                itemCount: history.length,
                itemBuilder: (_, i) => _TransactionCard(payment: history[i]),
              ),
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (e, _) => Center(child: Text('Error: $e')),
      ),
    );
  }
}

class _TransactionCard extends StatelessWidget {
  final PaymentInfo payment;

  const _TransactionCard({required this.payment});

  @override
  Widget build(BuildContext context) {
    final isCompleted = payment.status == 'PAYMENT_DONE' ||
        payment.status == 'COMPLETED';

    return Card(
      margin: const EdgeInsets.only(bottom: 10),
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
      child: Padding(
        padding: const EdgeInsets.all(14),
        child: Row(
          children: [
            Container(
              width: 48,
              height: 48,
              decoration: BoxDecoration(
                color: isCompleted
                    ? Colors.green.shade50
                    : Colors.orange.shade50,
                shape: BoxShape.circle,
              ),
              child: Icon(
                isCompleted
                    ? Icons.check_circle_outline
                    : Icons.pending_outlined,
                color: isCompleted ? Colors.green : Colors.orange,
              ),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'Task #${payment.taskId?.substring(0, 8) ?? "—"}',
                    style: const TextStyle(fontWeight: FontWeight.w600),
                  ),
                  const SizedBox(height: 2),
                  Text(
                    payment.method ?? 'Pending payment',
                    style: TextStyle(
                        color: Colors.grey.shade500, fontSize: 12),
                  ),
                ],
              ),
            ),
            Column(
              crossAxisAlignment: CrossAxisAlignment.end,
              children: [
                Text(
                  '₹${payment.totalAmount.toStringAsFixed(2)}',
                  style: TextStyle(
                      fontWeight: FontWeight.bold,
                      color: AppTheme.primary,
                      fontSize: 15),
                ),
                Text(
                  payment.status,
                  style: TextStyle(
                      color: isCompleted ? Colors.green : Colors.orange,
                      fontSize: 11),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}
