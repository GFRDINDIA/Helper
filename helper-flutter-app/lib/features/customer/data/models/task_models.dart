class CreateTaskRequest {
  final String title;
  final String description;
  final String domain;
  final String pricingModel; // FIXED | BIDDING
  final double? budget;
  final double? minBudget;
  final double? maxBudget;
  final String location;
  final double latitude;
  final double longitude;
  final String? scheduledAt;

  const CreateTaskRequest({
    required this.title,
    required this.description,
    required this.domain,
    required this.pricingModel,
    this.budget,
    this.minBudget,
    this.maxBudget,
    required this.location,
    required this.latitude,
    required this.longitude,
    this.scheduledAt,
  });

  Map<String, dynamic> toJson() {
    final map = <String, dynamic>{
      'title': title,
      'description': description,
      'domain': domain,
      'pricingModel': pricingModel,
      'location': location,
      'latitude': latitude,
      'longitude': longitude,
    };
    if (pricingModel == 'FIXED' && budget != null) map['budget'] = budget;
    if (pricingModel == 'BIDDING') {
      if (minBudget != null) map['minBudget'] = minBudget;
      if (maxBudget != null) map['maxBudget'] = maxBudget;
    }
    if (scheduledAt != null) map['scheduledAt'] = scheduledAt;
    return map;
  }
}

class Task {
  final String id;
  final String title;
  final String description;
  final String domain;
  final String status;
  final String pricingModel;
  final double? budget;
  final double? minBudget;
  final double? maxBudget;
  final double? finalPrice;
  final String location;
  final double? latitude;
  final double? longitude;
  final String? customerId;
  final String? workerId;
  final String? scheduledAt;
  final String? createdAt;
  final int? bidCount;

  const Task({
    required this.id,
    required this.title,
    required this.description,
    required this.domain,
    required this.status,
    required this.pricingModel,
    this.budget,
    this.minBudget,
    this.maxBudget,
    this.finalPrice,
    required this.location,
    this.latitude,
    this.longitude,
    this.customerId,
    this.workerId,
    this.scheduledAt,
    this.createdAt,
    this.bidCount,
  });

  factory Task.fromJson(Map<String, dynamic> json) => Task(
        id: json['id']?.toString() ?? '',
        title: json['title'] as String? ?? '',
        description: json['description'] as String? ?? '',
        domain: json['domain'] as String? ?? '',
        status: json['status'] as String? ?? '',
        pricingModel: json['pricingModel'] as String? ?? 'FIXED',
        budget: (json['budget'] as num?)?.toDouble(),
        minBudget: (json['minBudget'] as num?)?.toDouble(),
        maxBudget: (json['maxBudget'] as num?)?.toDouble(),
        finalPrice: (json['finalPrice'] as num?)?.toDouble(),
        location: json['location'] as String? ?? '',
        latitude: (json['latitude'] as num?)?.toDouble(),
        longitude: (json['longitude'] as num?)?.toDouble(),
        customerId: json['customerId']?.toString(),
        workerId: json['workerId']?.toString(),
        scheduledAt: json['scheduledAt'] as String?,
        createdAt: json['createdAt'] as String?,
        bidCount: json['bidCount'] as int?,
      );

  String get displayBudget {
    if (pricingModel == 'FIXED') return '₹${budget?.toStringAsFixed(0) ?? "—"}';
    return '₹${minBudget?.toStringAsFixed(0) ?? "—"} – ₹${maxBudget?.toStringAsFixed(0) ?? "—"}';
  }
}

class Bid {
  final String id;
  final String taskId;
  final String workerId;
  final String? workerName;
  final double proposedAmount;
  final int? estimatedHours;
  final String? message;
  final String status; // PENDING | ACCEPTED | REJECTED | WITHDRAWN
  final String? createdAt;

  const Bid({
    required this.id,
    required this.taskId,
    required this.workerId,
    this.workerName,
    required this.proposedAmount,
    this.estimatedHours,
    this.message,
    required this.status,
    this.createdAt,
  });

  factory Bid.fromJson(Map<String, dynamic> json) => Bid(
        id: json['id']?.toString() ?? '',
        taskId: json['taskId']?.toString() ?? '',
        workerId: json['workerId']?.toString() ?? '',
        workerName: json['workerName'] as String?,
        proposedAmount: (json['proposedAmount'] as num?)?.toDouble() ?? 0,
        estimatedHours: json['estimatedHours'] as int?,
        message: json['message'] as String?,
        status: json['status'] as String? ?? 'PENDING',
        createdAt: json['createdAt'] as String?,
      );
}

class NearbyWorker {
  final String workerId;
  final String fullName;
  final String? avatarUrl;
  final double? averageRating;
  final int? totalRatings;
  final double? distanceKm;
  final List<String> domains;
  final String verificationStatus;

  const NearbyWorker({
    required this.workerId,
    required this.fullName,
    this.avatarUrl,
    this.averageRating,
    this.totalRatings,
    this.distanceKm,
    required this.domains,
    required this.verificationStatus,
  });

  factory NearbyWorker.fromJson(Map<String, dynamic> json) => NearbyWorker(
        workerId: json['workerId']?.toString() ?? '',
        fullName: json['fullName'] as String? ?? '',
        avatarUrl: json['avatarUrl'] as String?,
        averageRating: (json['averageRating'] as num?)?.toDouble(),
        totalRatings: json['totalRatings'] as int?,
        distanceKm: (json['distanceKm'] as num?)?.toDouble(),
        domains: (json['domains'] as List<dynamic>?)
                ?.map((e) => e.toString())
                .toList() ??
            [],
        verificationStatus: json['verificationStatus'] as String? ?? '',
      );
}
