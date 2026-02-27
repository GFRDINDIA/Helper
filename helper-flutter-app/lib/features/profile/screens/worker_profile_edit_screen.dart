import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:geolocator/geolocator.dart';
import '../../worker/data/worker_repository.dart';
import '../../worker/data/models/worker_models.dart';
import '../../../shared/theme/app_theme.dart';
import '../../../shared/widgets/helper_button.dart';
import '../../../core/constants/app_constants.dart';
import '../../../core/network/api_exception.dart';

class WorkerProfileEditScreen extends ConsumerStatefulWidget {
  const WorkerProfileEditScreen({super.key});

  @override
  ConsumerState<WorkerProfileEditScreen> createState() =>
      _WorkerProfileEditScreenState();
}

class _WorkerProfileEditScreenState
    extends ConsumerState<WorkerProfileEditScreen> {
  final _bioCtrl = TextEditingController();
  bool _isLoading = false;
  bool _isFetching = true;
  String? _errorMessage;
  double _lat = 0;
  double _lng = 0;

  // Skills list: {domain, priceModel, fixedRate, radiusKm}
  List<Map<String, dynamic>> _skills = [];

  @override
  void initState() {
    super.initState();
    _loadProfile();
    _fetchLocation();
  }

  Future<void> _fetchLocation() async {
    try {
      await Geolocator.requestPermission();
      final pos = await Geolocator.getCurrentPosition();
      if (mounted) {
        setState(() {
          _lat = pos.latitude;
          _lng = pos.longitude;
        });
      }
    } catch (_) {}
  }

  Future<void> _loadProfile() async {
    try {
      final profile = await WorkerRepository().getProfile();
      _bioCtrl.text = profile['bio'] as String? ?? '';
      final skills = (profile['skills'] as List<dynamic>?) ?? [];
      _skills = skills.map((s) {
        final m = s as Map<String, dynamic>;
        return {
          'domain': m['domain'] as String? ?? kTaskDomains.first,
          'priceModel': m['priceModel'] as String? ?? 'FIXED',
          'fixedRate': (m['fixedRate'] as num?)?.toDouble() ?? 0.0,
          'radiusKm': (m['serviceRadiusKm'] as num?)?.toDouble() ?? 10.0,
        };
      }).toList();
    } catch (_) {
    } finally {
      if (mounted) setState(() => _isFetching = false);
    }
  }

  Future<void> _save() async {
    setState(() {
      _isLoading = true;
      _errorMessage = null;
    });

    try {
      final skills = _skills
          .map((s) => SkillRequest(
                domain: s['domain'] as String,
                priceModel: s['priceModel'] as String,
                fixedRate: (s['priceModel'] as String) == 'FIXED'
                    ? s['fixedRate'] as double?
                    : null,
                latitude: _lat,
                longitude: _lng,
                serviceRadiusKm: s['radiusKm'] as double,
              ))
          .toList();

      await WorkerRepository().updateProfile(WorkerProfileRequest(
        bio: _bioCtrl.text.trim(),
        skills: skills,
      ));

      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
            content: Text('Profile updated'),
            backgroundColor: Colors.green),
      );
    } on ApiException catch (e) {
      setState(() => _errorMessage = e.message);
    } finally {
      if (mounted) setState(() => _isLoading = false);
    }
  }

  void _addSkill() {
    setState(() {
      _skills.add({
        'domain': kTaskDomains.first,
        'priceModel': 'FIXED',
        'fixedRate': 0.0,
        'radiusKm': 10.0,
      });
    });
  }

  void _removeSkill(int index) {
    setState(() => _skills.removeAt(index));
  }

  @override
  void dispose() {
    _bioCtrl.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    if (_isFetching) {
      return const Scaffold(body: Center(child: CircularProgressIndicator()));
    }

    return Scaffold(
      appBar: AppBar(title: const Text('Edit Profile')),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(24),
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
            const Text('Bio',
                style: TextStyle(fontWeight: FontWeight.w600)),
            const SizedBox(height: 8),
            TextFormField(
              controller: _bioCtrl,
              maxLines: 3,
              decoration: InputDecoration(
                hintText: 'Tell customers about yourself...',
                border: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(12)),
                filled: true,
                fillColor: Colors.grey.shade50,
              ),
            ),
            const SizedBox(height: 24),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                const Text('Services Offered',
                    style: TextStyle(
                        fontWeight: FontWeight.bold, fontSize: 16)),
                TextButton.icon(
                  onPressed: _addSkill,
                  icon: const Icon(Icons.add),
                  label: const Text('Add'),
                ),
              ],
            ),
            const SizedBox(height: 12),
            if (_skills.isEmpty)
              Text('No services added yet. Tap Add to start.',
                  style: TextStyle(color: Colors.grey.shade500)),
            ..._skills.asMap().entries.map((entry) {
              final i = entry.key;
              final skill = entry.value;
              return _SkillEditor(
                skill: skill,
                onChanged: (updated) {
                  setState(() => _skills[i] = updated);
                },
                onRemove: () => _removeSkill(i),
              );
            }),
            const SizedBox(height: 32),
            HelperButton(
                label: 'Save Changes',
                isLoading: _isLoading,
                onPressed: _save),
          ],
        ),
      ),
    );
  }
}

class _SkillEditor extends StatefulWidget {
  final Map<String, dynamic> skill;
  final ValueChanged<Map<String, dynamic>> onChanged;
  final VoidCallback onRemove;

  const _SkillEditor(
      {required this.skill,
      required this.onChanged,
      required this.onRemove});

  @override
  State<_SkillEditor> createState() => _SkillEditorState();
}

class _SkillEditorState extends State<_SkillEditor> {
  late Map<String, dynamic> _skill;
  final _rateCtrl = TextEditingController();
  final _radiusCtrl = TextEditingController();

  @override
  void initState() {
    super.initState();
    _skill = Map.from(widget.skill);
    _rateCtrl.text = (_skill['fixedRate'] as double).toStringAsFixed(0);
    _radiusCtrl.text = (_skill['radiusKm'] as double).toStringAsFixed(0);
  }

  @override
  void dispose() {
    _rateCtrl.dispose();
    _radiusCtrl.dispose();
    super.dispose();
  }

  void _update() {
    _skill['fixedRate'] = double.tryParse(_rateCtrl.text) ?? 0.0;
    _skill['radiusKm'] = double.tryParse(_radiusCtrl.text) ?? 10.0;
    widget.onChanged(_skill);
  }

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
      child: Padding(
        padding: const EdgeInsets.all(12),
        child: Column(
          children: [
            Row(
              children: [
                Expanded(
                  child: DropdownButtonFormField<String>(
                    value: _skill['domain'] as String,
                    decoration: const InputDecoration(
                        labelText: 'Domain',
                        border: OutlineInputBorder(),
                        isDense: true),
                    items: kTaskDomains
                        .map((d) => DropdownMenuItem(
                              value: d,
                              child: Text(
                                  '${kDomainIcons[d] ?? ""} ${kDomainLabels[d] ?? d}',
                                  style: const TextStyle(fontSize: 13)),
                            ))
                        .toList(),
                    onChanged: (v) {
                      setState(() => _skill['domain'] = v!);
                      _update();
                    },
                  ),
                ),
                IconButton(
                  icon: const Icon(Icons.delete_outline, color: Colors.red),
                  onPressed: widget.onRemove,
                ),
              ],
            ),
            const SizedBox(height: 8),
            Row(
              children: [
                Expanded(
                  child: DropdownButtonFormField<String>(
                    value: _skill['priceModel'] as String,
                    decoration: const InputDecoration(
                        labelText: 'Pricing',
                        border: OutlineInputBorder(),
                        isDense: true),
                    items: const [
                      DropdownMenuItem(
                          value: 'FIXED', child: Text('Fixed Rate')),
                      DropdownMenuItem(
                          value: 'BIDDING', child: Text('Bidding')),
                    ],
                    onChanged: (v) {
                      setState(() => _skill['priceModel'] = v!);
                      _update();
                    },
                  ),
                ),
                if (_skill['priceModel'] == 'FIXED') ...[
                  const SizedBox(width: 8),
                  Expanded(
                    child: TextFormField(
                      controller: _rateCtrl,
                      keyboardType: TextInputType.number,
                      decoration: const InputDecoration(
                          labelText: 'Rate (₹)',
                          border: OutlineInputBorder(),
                          isDense: true),
                      onChanged: (_) => _update(),
                    ),
                  ),
                ],
              ],
            ),
            const SizedBox(height: 8),
            TextFormField(
              controller: _radiusCtrl,
              keyboardType: TextInputType.number,
              decoration: const InputDecoration(
                  labelText: 'Service Radius (km)',
                  border: OutlineInputBorder(),
                  isDense: true),
              onChanged: (_) => _update(),
            ),
          ],
        ),
      ),
    );
  }
}
