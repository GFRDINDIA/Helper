import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import 'package:dio/dio.dart';
import '../../../core/network/dio_client.dart';
import '../../../core/constants/api_constants.dart';
import '../../../core/constants/app_constants.dart';
import '../../../shared/theme/app_theme.dart';
import '../../../shared/widgets/helper_button.dart';
import '../../../core/network/api_exception.dart';

class KycUploadScreen extends StatefulWidget {
  const KycUploadScreen({super.key});

  @override
  State<KycUploadScreen> createState() => _KycUploadScreenState();
}

class _KycUploadScreenState extends State<KycUploadScreen> {
  final _picker = ImagePicker();
  String _selectedDocType = 'AADHAAR';
  XFile? _frontImage;
  XFile? _backImage;
  bool _isLoading = false;
  String? _errorMessage;
  String? _verificationStatus;

  @override
  void initState() {
    super.initState();
    _loadStatus();
  }

  Future<void> _loadStatus() async {
    try {
      final dio = DioClient.instance;
      final r = await dio.get(ApiEndpoints.kycStatus);
      final data = r.data['data'] ?? r.data;
      if (mounted && data is Map) {
        setState(() =>
            _verificationStatus = data['verificationStatus'] as String?);
      }
    } catch (_) {}
  }

  Future<void> _pickImage(bool isFront) async {
    final image = await _picker.pickImage(
      source: ImageSource.gallery,
      imageQuality: 80,
    );
    if (image != null) {
      setState(() {
        if (isFront) {
          _frontImage = image;
        } else {
          _backImage = image;
        }
      });
    }
  }

  Future<void> _submit() async {
    if (_frontImage == null) {
      setState(() => _errorMessage = 'Please select the front image');
      return;
    }

    setState(() {
      _isLoading = true;
      _errorMessage = null;
    });

    try {
      final formData = FormData.fromMap({
        'documentType': _selectedDocType,
        'frontImage': await MultipartFile.fromFile(
          _frontImage!.path,
          filename: _frontImage!.name,
        ),
        if (_backImage != null)
          'backImage': await MultipartFile.fromFile(
            _backImage!.path,
            filename: _backImage!.name,
          ),
      });

      await DioClient.instance.post(
        ApiEndpoints.kycUpload,
        data: formData,
        options: Options(contentType: 'multipart/form-data'),
      );

      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('KYC documents submitted for review'),
          backgroundColor: Colors.green,
        ),
      );
      setState(() {
        _frontImage = null;
        _backImage = null;
        _verificationStatus = 'PENDING';
      });
    } on ApiException catch (e) {
      setState(() => _errorMessage = e.message);
    } catch (e) {
      setState(() => _errorMessage = 'Upload failed. Please try again.');
    } finally {
      if (mounted) setState(() => _isLoading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('KYC Verification')),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(24),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Status banner
            if (_verificationStatus != null)
              Container(
                padding: const EdgeInsets.all(12),
                margin: const EdgeInsets.only(bottom: 20),
                decoration: BoxDecoration(
                  color: _statusColor(_verificationStatus!).withOpacity(0.1),
                  borderRadius: BorderRadius.circular(10),
                  border: Border.all(
                      color: _statusColor(_verificationStatus!)
                          .withOpacity(0.3)),
                ),
                child: Row(
                  children: [
                    Icon(
                      _statusIcon(_verificationStatus!),
                      color: _statusColor(_verificationStatus!),
                    ),
                    const SizedBox(width: 8),
                    Text(
                      'Verification Status: $_verificationStatus',
                      style: TextStyle(
                          color: _statusColor(_verificationStatus!),
                          fontWeight: FontWeight.w600),
                    ),
                  ],
                ),
              ),
            const Text(
              'Upload Identity Documents',
              style: TextStyle(
                  fontSize: 18, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 8),
            Text(
              'Your documents are reviewed within 24 hours.',
              style: TextStyle(color: Colors.grey.shade600),
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
            const Text('Document Type',
                style: TextStyle(fontWeight: FontWeight.w600)),
            const SizedBox(height: 8),
            DropdownButtonFormField<String>(
              value: _selectedDocType,
              decoration: InputDecoration(
                border: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(12)),
                filled: true,
                fillColor: Colors.grey.shade50,
              ),
              items: kKycDocTypes
                  .map((t) => DropdownMenuItem(
                        value: t,
                        child: Text(kKycDocLabels[t] ?? t),
                      ))
                  .toList(),
              onChanged: (v) => setState(() => _selectedDocType = v!),
            ),
            const SizedBox(height: 24),
            _ImagePicker(
              label: 'Front Side',
              image: _frontImage,
              onPick: () => _pickImage(true),
            ),
            const SizedBox(height: 16),
            _ImagePicker(
              label: 'Back Side (optional)',
              image: _backImage,
              onPick: () => _pickImage(false),
            ),
            const SizedBox(height: 32),
            HelperButton(
              label: 'Submit for Verification',
              isLoading: _isLoading,
              onPressed: _submit,
            ),
          ],
        ),
      ),
    );
  }

  Color _statusColor(String status) {
    switch (status) {
      case 'VERIFIED':
        return Colors.green;
      case 'REJECTED':
        return Colors.red;
      default:
        return Colors.orange;
    }
  }

  IconData _statusIcon(String status) {
    switch (status) {
      case 'VERIFIED':
        return Icons.verified;
      case 'REJECTED':
        return Icons.cancel;
      default:
        return Icons.pending;
    }
  }
}

class _ImagePicker extends StatelessWidget {
  final String label;
  final XFile? image;
  final VoidCallback onPick;

  const _ImagePicker(
      {required this.label, this.image, required this.onPick});

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onPick,
      child: Container(
        height: 140,
        decoration: BoxDecoration(
          color: Colors.grey.shade50,
          border: Border.all(
              color: image != null ? AppTheme.primary : Colors.grey.shade300,
              width: image != null ? 2 : 1),
          borderRadius: BorderRadius.circular(12),
        ),
        child: image == null
            ? Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Icon(Icons.add_photo_alternate_outlined,
                      size: 36, color: Colors.grey.shade400),
                  const SizedBox(height: 8),
                  Text(label,
                      style: TextStyle(color: Colors.grey.shade500)),
                  Text('Tap to select',
                      style: TextStyle(
                          color: Colors.grey.shade400, fontSize: 12)),
                ],
              )
            : Stack(
                fit: StackFit.expand,
                children: [
                  ClipRRect(
                    borderRadius: BorderRadius.circular(12),
                    child: Image.network(
                      image!.path,
                      fit: BoxFit.cover,
                      errorBuilder: (_, __, ___) => Center(
                        child: Column(
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: [
                            Icon(Icons.check_circle,
                                color: AppTheme.primary, size: 32),
                            const SizedBox(height: 4),
                            Text(image!.name,
                                style: const TextStyle(fontSize: 12)),
                          ],
                        ),
                      ),
                    ),
                  ),
                  Positioned(
                    top: 8,
                    right: 8,
                    child: CircleAvatar(
                      radius: 14,
                      backgroundColor: AppTheme.primary,
                      child: const Icon(Icons.check,
                          color: Colors.white, size: 16),
                    ),
                  ),
                ],
              ),
      ),
    );
  }
}
