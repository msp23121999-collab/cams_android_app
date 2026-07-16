api_path = 'app/src/main/java/com/example/core/network/CamsApiService.kt'
with open(api_path, 'r', encoding='utf-8') as f:
    api = f.read()

# Make remaining ParentChildProfileDto fields nullable with defaults
replacements = [
    ('    val gender: String,', '    val gender: String? = "",'),
    ('    @Json(name = "blood_group") val bloodGroup: String,', '    @Json(name = "blood_group") val bloodGroup: String? = "",'),
    ('    val nationality: String,', '    val nationality: String? = "",'),
    ('    @Json(name = "aadhaar_no") val aadhaarNo: String,', '    @Json(name = "aadhaar_no") val aadhaarNo: String? = "",'),
    ('    @Json(name = "contact_mobile") val contactMobile: String,', '    @Json(name = "contact_mobile") val contactMobile: String? = "",'),
    ('    @Json(name = "contact_email") val contactEmail: String,', '    @Json(name = "contact_email") val contactEmail: String? = "",'),
    ('    @Json(name = "emergency_contact") val emergencyContact: String,', '    @Json(name = "emergency_contact") val emergencyContact: String? = "",'),
    ('    @Json(name = "emergency_phone") val emergencyPhone: String,', '    @Json(name = "emergency_phone") val emergencyPhone: String? = "",'),
    ('    @Json(name = "father_name") val fatherName: String,', '    @Json(name = "father_name") val fatherName: String? = "",'),
    ('    @Json(name = "father_occupation") val fatherOccupation: String,', '    @Json(name = "father_occupation") val fatherOccupation: String? = "",'),
    ('    @Json(name = "father_mobile") val fatherMobile: String,', '    @Json(name = "father_mobile") val fatherMobile: String? = "",'),
    ('    @Json(name = "father_email") val fatherEmail: String,', '    @Json(name = "father_email") val fatherEmail: String? = "",'),
    ('    @Json(name = "mother_name") val motherName: String,', '    @Json(name = "mother_name") val motherName: String? = "",'),
    ('    @Json(name = "mother_occupation") val motherOccupation: String,', '    @Json(name = "mother_occupation") val motherOccupation: String? = "",'),
    ('    @Json(name = "mother_mobile") val motherMobile: String,', '    @Json(name = "mother_mobile") val motherMobile: String? = "",'),
    ('    @Json(name = "mother_email") val motherEmail: String,', '    @Json(name = "mother_email") val motherEmail: String? = "",'),
    ('    val certifications: List<ParentChildCertificationDto>', '    val certifications: List<ParentChildCertificationDto>? = emptyList()'),
]

for old, new in replacements:
    api = api.replace(old, new)

with open(api_path, 'w', encoding='utf-8') as f:
    f.write(api)

print("Made ParentChildProfileDto fields nullable")
