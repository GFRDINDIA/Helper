import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:geolocator/geolocator.dart';
import 'package:geocoding/geocoding.dart';
import '../data/models/task_models.dart';
import '../data/customer_repository.dart';
import '../../../shared/theme/app_theme.dart';
import '../../../shared/widgets/helper_button.dart';
import '../../../core/constants/app_constants.dart';
import '../../../core/network/api_exception.dart';

class PostTaskScreen extends ConsumerStatefulWidget {
  const PostTaskScreen({super.key});

  @override
  ConsumerState<PostTaskScreen> createState() => _PostTaskScreenState();
}

class _PostTaskScreenState extends ConsumerState<PostTaskScreen> {
  final _formKey = GlobalKey<FormState>();
  final _titleCtrl = TextEditingController();
  final _descCtrl = TextEditingController();
  final _locationCtrl = TextEditingController();
  final _budgetCtrl = TextEditingController();
  final _minBudgetCtrl = TextEditingController();
  final _maxBudgetCtrl = TextEditingController();

  String _domain = 'PLUMBING';
  String _pricingModel = 'FIXED';
  double _lat = 0;
  double _lng = 0;
  bool _isLoading = false;
  bool _isFetchingLocation = false;
  String? _errorMessage;

  @override
  void initState() {
    super.initState();
    _fetchLocation();
  }

  Future<void> _fetchLocation() async {
    setState(() => _isFetchingLocation = true);
    try {
      final permission = await Geolocator.requestPermission();
      if (permission == LocationPermission.denied) return;
      final pos = await Geolocator.getCurrentPosition();
      _lat = pos.latitude;
      _lng = pos.longitude;

      final placemarks =
          await placemarkFromCoordinates(pos.latitude, pos.longitude);
      if (placemarks.isNotEmpty) {
        final p = placemarks.first;
        _locationCtrl.text =
            '${p.subLocality ?? ''}, ${p.locality ?? ''}, ${p.administrativeArea ?? ''}';
      }
    } catch (_) {
    } finally {
      if (mounted) setState(() => _isFetchingLocation = false);
    }
  }

  Future<void> _submit() async {
    if (!_formKey.currentState!.validate()) return;
    setState(() {
      _isLoading = true;
      _errorMessage = null;
    });

    try {
      final request = CreateTaskRequest(
        title: _titleCtrl.text.trim(),
        description: _descCtrl.text.trim(),
        domain: _domain,
        pricingModel: _pricingModel,
        budget: _pricingModel == 'FIXED'
            ? double.tryParse(_budgetCtrl.text)
            : null,
        minBudget: _pricingModel == 'BIDDING'
            ? double.tryParse(_minBudgetCtrl.text)
            : null,
        maxBudget: _pricingModel == 'BIDDING'
            ? double.tryParse(_maxBudgetCtrl.text)
            : null,
        location: _locationCtrl.text.trim(),
        latitude: _lat,
        longitude: _lng,
      );

      final task = await CustomerRepository().createTask(request);
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
            content: Text('Task posted successfully'),
            backgroundColor: Colors.green),
      );
      context.go('/customer/task/${task.id}');
    } on ApiException catch (e) {
      setState(() => _errorMessage = e.message);
    } finally {
      if (mounted) setState(() => _isLoading = false);
    }
  }

  @override
  void dispose() {
    _titleCtrl.dispose();
    _descCtrl.dispose();
    _locationCtrl.dispose();
    _budgetCtrl.dispose();
    _minBudgetCtrl.dispose();
    _maxBudgetCtrl.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Post a Task')),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(24),
        child: Form(
          key: _formKey,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
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
              _sectionLabel('Service Domain'),
              DropdownButtonFormField<String>(
                value: _domain,
                decoration: _inputDec('Select domain'),
                items: kTaskDomains
                    .map((d) => DropdownMenuItem(
                          value: d,
                          child: Text(
                              '${kDomainIcons[d] ?? ""} ${kDomainLabels[d] ?? d}'),
                        ))
                    .toList(),
                onChanged: (v) => setState(() => _domain = v!),
              ),
              const SizedBox(height: 20),
              _sectionLabel('Task Details'),
              TextFormField(
                controller: _titleCtrl,
                decoration: _inputDec('Task title'),
                validator: (v) => v!.isEmpty ? 'Title is required' : null,
              ),
              const SizedBox(height: 12),
              TextFormField(
                controller: _descCtrl,
                maxLines: 4,
                decoration: _inputDec('Describe what you need...'),
                validator: (v) =>
                    v!.length < 10 ? 'Please describe in detail' : null,
              ),
              const SizedBox(height: 20),
              _sectionLabel('Location'),
              TextFormField(
                controller: _locationCtrl,
                decoration: _inputDec('Your location').copyWith(
                  suffixIcon: _isFetchingLocation
                      ? const Padding(
                          padding: EdgeInsets.all(12),
                          child: SizedBox(
                              width: 20,
                              height: 20,
                              child: CircularProgressIndicator(strokeWidth: 2)),
                        )
                      : IconButton(
                          icon: const Icon(Icons.my_location),
                          onPressed: _fetchLocation,
                        ),
                ),
                validator: (v) => v!.isEmpty ? 'Location is required' : null,
              ),
              const SizedBox(height: 20),
              _sectionLabel('Pricing'),
              Row(
                children: [
                  _PricingToggle(
                    label: 'Fixed Price',
                    selected: _pricingModel == 'FIXED',
                    onTap: () => setState(() => _pricingModel = 'FIXED'),
                  ),
                  const SizedBox(width: 12),
                  _PricingToggle(
                    label: 'Open to Bids',
                    selected: _pricingModel == 'BIDDING',
                    onTap: () => setState(() => _pricingModel = 'BIDDING'),
                  ),
                ],
              ),
              const SizedBox(height: 12),
              if (_pricingModel == 'FIXED')
                TextFormField(
                  controller: _budgetCtrl,
                  keyboardType: TextInputType.number,
                  decoration: _inputDec('Budget (₹)'),
                  validator: (v) =>
                      double.tryParse(v ?? '') == null ? 'Enter valid amount' : null,
                )
              else
                Row(
                  children: [
                    Expanded(
                      child: TextFormField(
                        controller: _minBudgetCtrl,
                        keyboardType: TextInputType.number,
                        decoration: _inputDec('Min (₹)'),
                        validator: (v) =>
                            double.tryParse(v ?? '') == null
                                ? 'Invalid'
                                : null,
                      ),
                    ),
                    const SizedBox(width: 12),
                    Expanded(
                      child: TextFormField(
                        controller: _maxBudgetCtrl,
                        keyboardType: TextInputType.number,
                        decoration: _inputDec('Max (₹)'),
                        validator: (v) =>
                            double.tryParse(v ?? '') == null
                                ? 'Invalid'
                                : null,
                      ),
                    ),
                  ],
                ),
              const SizedBox(height: 32),
              HelperButton(
                label: 'Post Task',
                isLoading: _isLoading,
                onPressed: _submit,
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _sectionLabel(String label) => Padding(
        padding: const EdgeInsets.only(bottom: 8),
        child: Text(label,
            style: const TextStyle(
                fontWeight: FontWeight.w600, fontSize: 14)),
      );

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

class _PricingToggle extends StatelessWidget {
  final String label;
  final bool selected;
  final VoidCallback onTap;

  const _PricingToggle(
      {required this.label, required this.selected, required this.onTap});

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 200),
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
        decoration: BoxDecoration(
          color: selected ? AppTheme.primary : Colors.white,
          border: Border.all(
              color: selected ? AppTheme.primary : Colors.grey.shade300,
              width: 2),
          borderRadius: BorderRadius.circular(10),
        ),
        child: Text(
          label,
          style: TextStyle(
            color: selected ? Colors.white : Colors.grey.shade700,
            fontWeight: FontWeight.w600,
          ),
        ),
      ),
    );
  }
}
