class SkillRequest {
  final String domain;
  final String priceModel;
  final double? fixedRate;
  final double latitude;
  final double longitude;
  final double serviceRadiusKm;

  const SkillRequest({
    required this.domain,
    required this.priceModel,
    this.fixedRate,
    required this.latitude,
    required this.longitude,
    this.serviceRadiusKm = 10,
  });

  Map<String, dynamic> toJson() => {
        'domain': domain,
        'priceModel': priceModel,
        if (fixedRate != null) 'fixedRate': fixedRate,
        'latitude': latitude,
        'longitude': longitude,
        'serviceRadiusKm': serviceRadiusKm,
      };
}

class WorkerProfileRequest {
  final String? bio;
  final List<SkillRequest> skills;

  const WorkerProfileRequest({this.bio, required this.skills});

  Map<String, dynamic> toJson() => {
        if (bio != null) 'bio': bio,
        'skills': skills.map((s) => s.toJson()).toList(),
      };
}

class SubmitBidRequest {
  final String taskId;
  final double proposedAmount;
  final int? estimatedHours;
  final String? message;

  const SubmitBidRequest({
    required this.taskId,
    required this.proposedAmount,
    this.estimatedHours,
    this.message,
  });

  Map<String, dynamic> toJson() => {
        'taskId': taskId,
        'proposedAmount': proposedAmount,
        if (estimatedHours != null) 'estimatedHours': estimatedHours,
        if (message != null) 'message': message,
      };
}
