import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/network/dio_client.dart';
import '../../../core/constants/api_constants.dart';
import '../../../core/constants/app_constants.dart';
import '../../../shared/theme/app_theme.dart';

final _workerProfileProvider =
    FutureProvider.family<Map<String, dynamic>, String>((ref, workerId) async {
  final dio = DioClient.instance;
  final r = await dio.get('/api/v1/workers/$workerId/profile');
  return (r.data['data'] ?? r.data) as Map<String, dynamic>;
});

class WorkerProfileScreen extends ConsumerWidget {
  final String workerId;

  const WorkerProfileScreen({super.key, required this.workerId});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final profileAsync = ref.watch(_workerProfileProvider(workerId));
    final ratingsAsync =
        ref.watch(_workerRatingsProvider(workerId));

    return Scaffold(
      appBar: AppBar(title: const Text('Worker Profile')),
      body: profileAsync.when(
        data: (profile) => SingleChildScrollView(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Header
              Row(
                children: [
                  CircleAvatar(
                    radius: 36,
                    backgroundColor: AppTheme.primary.withOpacity(0.1),
                    child: Text(
                      (profile['fullName'] as String? ?? 'W')[0]
                          .toUpperCase(),
                      style: TextStyle(
                          fontSize: 28,
                          color: AppTheme.primary,
                          fontWeight: FontWeight.bold),
                    ),
                  ),
                  const SizedBox(width: 16),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          profile['fullName'] as String? ?? '',
                          style: const TextStyle(
                              fontSize: 20, fontWeight: FontWeight.bold),
                        ),
                        const SizedBox(height: 4),
                        Row(
                          children: [
                            Icon(Icons.star,
                                size: 16, color: Colors.amber.shade600),
                            const SizedBox(width: 4),
                            Text(
                              '${(profile['averageRating'] as num?)?.toStringAsFixed(1) ?? "New"} (${profile['totalRatings'] ?? 0} reviews)',
                              style: TextStyle(color: Colors.grey.shade600),
                            ),
                          ],
                        ),
                        const SizedBox(height: 4),
                        _VerificationBadge(
                            status: profile['verificationStatus'] as String? ??
                                ''),
                      ],
                    ),
                  ),
                ],
              ),
              const Divider(height: 32),
              const Text('Services Offered',
                  style: TextStyle(
                      fontSize: 16, fontWeight: FontWeight.bold)),
              const SizedBox(height: 12),
              Wrap(
                spacing: 8,
                runSpacing: 8,
                children: ((profile['skills'] as List<dynamic>?) ?? [])
                    .map((s) {
                  final domain = s['domain'] as String? ?? '';
                  final priceModel = s['priceModel'] as String? ?? '';
                  final rate = s['fixedRate'] as num?;
                  return Container(
                    padding: const EdgeInsets.symmetric(
                        horizontal: 12, vertical: 8),
                    decoration: BoxDecoration(
                      color: Colors.blue.shade50,
                      borderRadius: BorderRadius.circular(8),
                      border: Border.all(color: Colors.blue.shade100),
                    ),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          '${kDomainIcons[domain] ?? ""} ${kDomainLabels[domain] ?? domain}',
                          style: const TextStyle(
                              fontWeight: FontWeight.w600),
                        ),
                        Text(
                          priceModel == 'FIXED' && rate != null
                              ? '₹${rate.toStringAsFixed(0)}/visit'
                              : 'Open to bids',
                          style: TextStyle(
                              color: Colors.blue.shade700, fontSize: 12),
                        ),
                      ],
                    ),
                  );
                }).toList(),
              ),
              const Divider(height: 32),
              const Text('Reviews',
                  style: TextStyle(
                      fontSize: 16, fontWeight: FontWeight.bold)),
              const SizedBox(height: 12),
              ratingsAsync.when(
                data: (ratings) => ratings.isEmpty
                    ? Text('No reviews yet',
                        style: TextStyle(color: Colors.grey.shade500))
                    : Column(
                        children: ratings
                            .take(5)
                            .map((r) => _ReviewCard(review: r))
                            .toList(),
                      ),
                loading: () => const Center(child: CircularProgressIndicator()),
                error: (_, __) => const SizedBox.shrink(),
              ),
            ],
          ),
        ),
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (e, _) => Center(child: Text('Error: $e')),
      ),
    );
  }
}

final _workerRatingsProvider =
    FutureProvider.family<List<Map<String, dynamic>>, String>(
        (ref, workerId) async {
  final dio = DioClient.instance;
  final r = await dio.get(ApiEndpoints.ratingsByWorker(workerId));
  final data = r.data['data'] ?? r.data;
  final list = data is List ? data : [];
  return list.map((e) => e as Map<String, dynamic>).toList();
});

class _VerificationBadge extends StatelessWidget {
  final String status;

  const _VerificationBadge({required this.status});

  @override
  Widget build(BuildContext context) {
    final isVerified = status == 'VERIFIED';
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
      decoration: BoxDecoration(
        color: isVerified ? Colors.green.shade50 : Colors.orange.shade50,
        borderRadius: BorderRadius.circular(4),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(
            isVerified ? Icons.verified : Icons.pending_outlined,
            size: 12,
            color: isVerified ? Colors.green : Colors.orange,
          ),
          const SizedBox(width: 4),
          Text(
            isVerified ? 'Verified' : status,
            style: TextStyle(
              fontSize: 11,
              color: isVerified ? Colors.green.shade700 : Colors.orange.shade700,
              fontWeight: FontWeight.w600,
            ),
          ),
        ],
      ),
    );
  }
}

class _ReviewCard extends StatelessWidget {
  final Map<String, dynamic> review;

  const _ReviewCard({required this.review});

  @override
  Widget build(BuildContext context) {
    final score = (review['score'] as num?)?.toInt() ?? 0;
    return Card(
      margin: const EdgeInsets.only(bottom: 8),
      child: Padding(
        padding: const EdgeInsets.all(12),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: List.generate(
                5,
                (i) => Icon(
                  i < score ? Icons.star : Icons.star_border,
                  size: 16,
                  color: Colors.amber.shade600,
                ),
              ),
            ),
            if (review['feedback'] != null &&
                (review['feedback'] as String).isNotEmpty) ...[
              const SizedBox(height: 6),
              Text(review['feedback'] as String,
                  style: TextStyle(color: Colors.grey.shade700, fontSize: 13)),
            ],
          ],
        ),
      ),
    );
  }
}
