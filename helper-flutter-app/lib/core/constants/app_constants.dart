const String kAppName = 'Helper';
const String kAppTagline = 'Your trusted home services partner';

// Storage keys
const String kAccessToken = 'access_token';
const String kRefreshToken = 'refresh_token';
const String kUserRole = 'user_role';
const String kUserId = 'user_id';
const String kUserEmail = 'user_email';

// Task domains
const List<String> kTaskDomains = [
  'PLUMBING',
  'ELECTRICAL',
  'CLEANING',
  'CARPENTRY',
  'PAINTING',
  'GARDENING',
  'APPLIANCE_REPAIR',
  'PEST_CONTROL',
  'MOVING',
  'OTHER',
];

const Map<String, String> kDomainLabels = {
  'PLUMBING': 'Plumbing',
  'ELECTRICAL': 'Electrical',
  'CLEANING': 'Cleaning',
  'CARPENTRY': 'Carpentry',
  'PAINTING': 'Painting',
  'GARDENING': 'Gardening',
  'APPLIANCE_REPAIR': 'Appliance Repair',
  'PEST_CONTROL': 'Pest Control',
  'MOVING': 'Moving',
  'OTHER': 'Other',
};

const Map<String, String> kDomainIcons = {
  'PLUMBING': '🔧',
  'ELECTRICAL': '⚡',
  'CLEANING': '🧹',
  'CARPENTRY': '🪚',
  'PAINTING': '🎨',
  'GARDENING': '🌱',
  'APPLIANCE_REPAIR': '🔌',
  'PEST_CONTROL': '🐛',
  'MOVING': '📦',
  'OTHER': '🛠️',
};

// KYC document types
const List<String> kKycDocTypes = [
  'AADHAAR',
  'PAN',
  'DRIVING_LICENSE',
  'PASSPORT',
  'GST',
  'BANK_PASSBOOK',
];

const Map<String, String> kKycDocLabels = {
  'AADHAAR': 'Aadhaar Card',
  'PAN': 'PAN Card',
  'DRIVING_LICENSE': 'Driving License',
  'PASSPORT': 'Passport',
  'GST': 'GST Certificate',
  'BANK_PASSBOOK': 'Bank Passbook',
};
