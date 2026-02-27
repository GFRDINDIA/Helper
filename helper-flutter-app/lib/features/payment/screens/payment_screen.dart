import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../data/payment_repository.dart';
import '../../auth/providers/auth_provider.dart';
import '../../customer/providers/customer_provider.dart';
import '../../../shared/theme/app_theme.dart';
import '../../../shared/widgets/helper_button.dart';
import '../../../core/network/api_exception.dart';

class PaymentScreen extends ConsumerStatefulWidget {
  final String taskId;

  const PaymentScreen({super.key, required this.taskId});

  @override
  ConsumerState<PaymentScreen> createState() => _PaymentScreenState();
}

class _PaymentScreenState extends ConsumerState<PaymentScreen> {
  String _selectedMethod = 'CASH';
  final _tipCtrl = TextEditingController();
  bool _isLoading = false;
  bool _isInitiating = true;
  String? _errorMessage;
  PaymentInfo? _payment;

  final _methods = [
    {'value': 'CASH', 'label': 'Cash', 'icon': Icons.money},
    {'value': 'UPI', 'label': 'UPI', 'icon': Icons.qr_code},
    {'value': 'CARD', 'label': 'Card', 'icon': Icons.credit_card},
    {'value': 'WALLET', 'label': 'Wallet', 'icon': Icons.account_balance_wallet},
  ];

  @override
  void initState() {
    super.initState();
    _initiatePayment();
  }

  Future<void> _initiatePayment() async {
    try {
      final task = await CustomerRepository().getTask(widget.taskId);
      final customerId =
          ref.read(authStateProvider).valueOrNull?.userId ?? '';
      final workerId = task.workerId ?? '';
      final finalPrice = task.finalPrice ?? task.budget ?? 0;

      final payment = await PaymentRepository().initiatePayment(
        customerId: customerId,
        workerId: workerId,
        finalPrice: finalPrice,
      );

      if (mounted) setState(() => _payment = payment);
    } catch (e) {
      if (mounted) setState(() => _errorMessage = e.toString());
    } finally {
      if (mounted) setState(() => _isInitiating = false);
    }
  }

  Future<void> _pay() async {
    if (_payment == null) return;
    setState(() {
      _isLoading = true;
      _errorMessage = null;
    });

    try {
      final repo = PaymentRepository();

      // Add tip if entered
      final tipAmount = double.tryParse(_tipCtrl.text);
      if (tipAmount != null && tipAmount > 0) {
        await repo.addTip(_payment!.id, tipAmount);
      }

      // Process payment
      if (_selectedMethod == 'CASH') {
        await repo.confirmCash(_payment!.id);
      } else {
        await repo.confirmPayment(_payment!.id, _selectedMethod);
      }

      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
            content: Text('Payment successful'),
            backgroundColor: Colors.green),
      );
      // Go to rating screen
      context.go('/rating/${widget.taskId}');
    } on ApiException catch (e) {
      setState(() => _errorMessage = e.message);
    } finally {
      if (mounted) setState(() => _isLoading = false);
    }
  }

  @override
  void dispose() {
    _tipCtrl.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Payment')),
      body: _isInitiating
          ? const Center(child: CircularProgressIndicator())
          : _payment == null
              ? Center(
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      const Icon(Icons.error_outline,
                          size: 48, color: Colors.red),
                      const SizedBox(height: 16),
                      Text(_errorMessage ?? 'Failed to load payment info'),
                    ],
                  ),
                )
              : SingleChildScrollView(
                  padding: const EdgeInsets.all(24),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      // Payment summary
                      Container(
                        padding: const EdgeInsets.all(20),
                        decoration: BoxDecoration(
                          color: AppTheme.primary.withOpacity(0.05),
                          borderRadius: BorderRadius.circular(16),
                          border: Border.all(
                              color: AppTheme.primary.withOpacity(0.2)),
                        ),
                        child: Column(
                          children: [
                            _SummaryRow('Service Amount',
                                _payment!.amount),
                            _SummaryRow('Platform Fee (2%)',
                                _payment!.commission),
                            _SummaryRow('Tax (18% of fee)',
                                _payment!.tax),
                            const Divider(height: 20),
                            _SummaryRow('Total',
                                _payment!.amount + _payment!.commission + _payment!.tax,
                                isTotal: true),
                          ],
                        ),
                      ),
                      const SizedBox(height: 24),
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
                              style:
                                  TextStyle(color: Colors.red.shade700)),
                        ),
                      const Text('Payment Method',
                          style: TextStyle(
                              fontWeight: FontWeight.bold, fontSize: 16)),
                      const SizedBox(height: 12),
                      GridView.builder(
                        shrinkWrap: true,
                        physics: const NeverScrollableScrollPhysics(),
                        gridDelegate:
                            const SliverGridDelegateWithFixedCrossAxisCount(
                          crossAxisCount: 2,
                          crossAxisSpacing: 12,
                          mainAxisSpacing: 12,
                          childAspectRatio: 2.5,
                        ),
                        itemCount: _methods.length,
                        itemBuilder: (_, i) {
                          final m = _methods[i];
                          final selected =
                              m['value'] == _selectedMethod;
                          return GestureDetector(
                            onTap: () => setState(
                                () => _selectedMethod =
                                    m['value'] as String),
                            child: AnimatedContainer(
                              duration: const Duration(milliseconds: 200),
                              padding: const EdgeInsets.symmetric(
                                  horizontal: 12),
                              decoration: BoxDecoration(
                                color: selected
                                    ? AppTheme.primary
                                    : Colors.white,
                                border: Border.all(
                                  color: selected
                                      ? AppTheme.primary
                                      : Colors.grey.shade300,
                                  width: 2,
                                ),
                                borderRadius: BorderRadius.circular(10),
                              ),
                              child: Row(
                                mainAxisAlignment:
                                    MainAxisAlignment.center,
                                children: [
                                  Icon(
                                    m['icon'] as IconData,
                                    color: selected
                                        ? Colors.white
                                        : Colors.grey.shade600,
                                    size: 20,
                                  ),
                                  const SizedBox(width: 8),
                                  Text(
                                    m['label'] as String,
                                    style: TextStyle(
                                      color: selected
                                          ? Colors.white
                                          : Colors.grey.shade700,
                                      fontWeight: selected
                                          ? FontWeight.bold
                                          : FontWeight.normal,
                                    ),
                                  ),
                                ],
                              ),
                            ),
                          );
                        },
                      ),
                      const SizedBox(height: 24),
                      const Text('Add a Tip (optional)',
                          style: TextStyle(
                              fontWeight: FontWeight.w600, fontSize: 15)),
                      const SizedBox(height: 8),
                      TextFormField(
                        controller: _tipCtrl,
                        keyboardType: TextInputType.number,
                        decoration: InputDecoration(
                          hintText: 'Enter tip amount (₹)',
                          prefixText: '₹ ',
                          border: OutlineInputBorder(
                              borderRadius: BorderRadius.circular(12)),
                          filled: true,
                          fillColor: Colors.grey.shade50,
                        ),
                      ),
                      const SizedBox(height: 32),
                      HelperButton(
                        label: 'Pay Now',
                        isLoading: _isLoading,
                        onPressed: _pay,
                      ),
                    ],
                  ),
                ),
    );
  }
}

class _SummaryRow extends StatelessWidget {
  final String label;
  final double amount;
  final bool isTotal;

  const _SummaryRow(this.label, this.amount, {this.isTotal = false});

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(
            label,
            style: TextStyle(
              fontWeight: isTotal ? FontWeight.bold : FontWeight.normal,
              fontSize: isTotal ? 16 : 14,
              color: isTotal ? const Color(0xFF1A1A2E) : Colors.grey.shade700,
            ),
          ),
          Text(
            '₹${amount.toStringAsFixed(2)}',
            style: TextStyle(
              fontWeight: isTotal ? FontWeight.bold : FontWeight.normal,
              fontSize: isTotal ? 16 : 14,
              color: isTotal ? AppTheme.primary : Colors.grey.shade700,
            ),
          ),
        ],
      ),
    );
  }
}
