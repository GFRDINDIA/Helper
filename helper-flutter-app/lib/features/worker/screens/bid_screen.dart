import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../data/worker_repository.dart';
import '../data/models/worker_models.dart';
import '../../customer/providers/customer_provider.dart';
import '../../../shared/theme/app_theme.dart';
import '../../../shared/widgets/helper_button.dart';
import '../../../core/constants/app_constants.dart';
import '../../../core/network/api_exception.dart';

class BidScreen extends ConsumerStatefulWidget {
  final String taskId;

  const BidScreen({super.key, required this.taskId});

  @override
  ConsumerState<BidScreen> createState() => _BidScreenState();
}

class _BidScreenState extends ConsumerState<BidScreen> {
  final _formKey = GlobalKey<FormState>();
  final _amountCtrl = TextEditingController();
  final _hoursCtrl = TextEditingController();
  final _messageCtrl = TextEditingController();
  bool _isLoading = false;
  String? _errorMessage;

  @override
  void dispose() {
    _amountCtrl.dispose();
    _hoursCtrl.dispose();
    _messageCtrl.dispose();
    super.dispose();
  }

  Future<void> _submit() async {
    if (!_formKey.currentState!.validate()) return;
    setState(() {
      _isLoading = true;
      _errorMessage = null;
    });

    try {
      await WorkerRepository().submitBid(SubmitBidRequest(
        taskId: widget.taskId,
        proposedAmount: double.parse(_amountCtrl.text),
        estimatedHours: _hoursCtrl.text.isNotEmpty
            ? int.tryParse(_hoursCtrl.text)
            : null,
        message: _messageCtrl.text.trim().isNotEmpty
            ? _messageCtrl.text.trim()
            : null,
      ));

      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
            content: Text('Bid submitted successfully'),
            backgroundColor: Colors.green),
      );
      context.pop();
    } on ApiException catch (e) {
      setState(() => _errorMessage = e.message);
    } finally {
      if (mounted) setState(() => _isLoading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final taskAsync = ref.watch(taskDetailProvider(widget.taskId));

    return Scaffold(
      appBar: AppBar(title: const Text('Submit Bid')),
      body: taskAsync.when(
        data: (task) => SingleChildScrollView(
          padding: const EdgeInsets.all(24),
          child: Form(
            key: _formKey,
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                // Task summary
                Container(
                  padding: const EdgeInsets.all(16),
                  decoration: BoxDecoration(
                    color: Colors.blue.shade50,
                    borderRadius: BorderRadius.circular(12),
                    border: Border.all(color: Colors.blue.shade100),
                  ),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        children: [
                          Text(kDomainIcons[task.domain] ?? '🛠️',
                              style: const TextStyle(fontSize: 20)),
                          const SizedBox(width: 8),
                          Expanded(
                            child: Text(task.title,
                                style: const TextStyle(
                                    fontWeight: FontWeight.bold)),
                          ),
                        ],
                      ),
                      const SizedBox(height: 4),
                      Text(
                        'Budget: ${task.displayBudget}',
                        style: TextStyle(
                            color: AppTheme.primary,
                            fontWeight: FontWeight.w600),
                      ),
                      const SizedBox(height: 4),
                      Text(
                        task.description,
                        maxLines: 3,
                        overflow: TextOverflow.ellipsis,
                        style: TextStyle(
                            color: Colors.grey.shade700, fontSize: 13),
                      ),
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
                        style: TextStyle(color: Colors.red.shade700)),
                  ),
                const Text('Your Bid Amount (₹)',
                    style: TextStyle(fontWeight: FontWeight.w600)),
                const SizedBox(height: 8),
                TextFormField(
                  controller: _amountCtrl,
                  keyboardType: TextInputType.number,
                  decoration: _inputDec('Enter your price'),
                  validator: (v) {
                    final d = double.tryParse(v ?? '');
                    if (d == null || d <= 0) return 'Enter a valid amount';
                    return null;
                  },
                ),
                const SizedBox(height: 16),
                const Text('Estimated Hours (optional)',
                    style: TextStyle(fontWeight: FontWeight.w600)),
                const SizedBox(height: 8),
                TextFormField(
                  controller: _hoursCtrl,
                  keyboardType: TextInputType.number,
                  decoration: _inputDec('e.g. 2'),
                ),
                const SizedBox(height: 16),
                const Text('Message to Customer (optional)',
                    style: TextStyle(fontWeight: FontWeight.w600)),
                const SizedBox(height: 8),
                TextFormField(
                  controller: _messageCtrl,
                  maxLines: 4,
                  decoration: _inputDec(
                      'Introduce yourself, describe your approach...'),
                ),
                const SizedBox(height: 32),
                HelperButton(
                  label: 'Submit Bid',
                  isLoading: _isLoading,
                  onPressed: _submit,
                ),
              ],
            ),
          ),
        ),
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (e, _) => Center(child: Text('Error: $e')),
      ),
    );
  }

  InputDecoration _inputDec(String hint) => InputDecoration(
        hintText: hint,
        border: OutlineInputBorder(borderRadius: BorderRadius.circular(12)),
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
      );
}
