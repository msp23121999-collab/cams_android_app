import re

def fix_all():
    # 1. CamsApiService.kt
    api_path = 'app/src/main/java/com/example/core/network/CamsApiService.kt'
    with open(api_path, 'r', encoding='utf-8') as f:
        api = f.read()

    api = api.replace(
        'suspend fun getAllCourses(): Response<List<AdminCourseDto>>',
        'suspend fun getAllCoursesAdmin(): Response<List<AdminCourseDto>>'
    )
    api = api.replace(
        'suspend fun getBackups(): Response<List<AdminBackupDto>>',
        'suspend fun getBackupsAdmin(): Response<List<AdminBackupDto>>'
    )
    api = api.replace(
        'suspend fun getHODSyllabusCourses(): Response<List<HODSyllabusCourseDto>>',
        'suspend fun getHODSyllabusCourses(): Response<List<HODCourseDto>>'
    )
    api = api.replace(
        'suspend fun verifyResearchProof(@Path("id") proofId: String, @Body request: VerificationRequestDto): Response<Unit>',
        'suspend fun verifyResearchProof(@Path("id") proofId: String, @Body request: VerificationRequestDto): Response<Map<String, String>>'
    )
    with open(api_path, 'w', encoding='utf-8') as f:
        f.write(api)

    # 2. AdminRepository.kt
    admin_repo_path = 'app/src/main/java/com/example/core/repository/AdminRepository.kt'
    with open(admin_repo_path, 'r', encoding='utf-8') as f:
        admin_repo = f.read()
    
    admin_repo = admin_repo.replace(
        'val response = apiService.getAllCourses()',
        'val response = apiService.getAllCoursesAdmin()'
    )
    admin_repo = admin_repo.replace(
        'val response = apiService.getBackups()',
        'val response = apiService.getBackupsAdmin()'
    )
    with open(admin_repo_path, 'w', encoding='utf-8') as f:
        f.write(admin_repo)

    # 3. FacultyRepositoryImpl.kt
    faculty_repo_path = 'app/src/main/java/com/example/core/repository/FacultyRepositoryImpl.kt'
    with open(faculty_repo_path, 'r', encoding='utf-8') as f:
        faculty = f.read()

    bad_timetable_mapping = """                        com.example.features.parent.models.TimetablePeriod(
                            periodNo = index + 1,
                            time = "${itemDto.startTime} - ${itemDto.endTime}",
                            subjectName = itemDto.courseName,
                            subjectCode = itemDto.courseCode,
                            room = itemDto.roomNo,
                            facultyName = "", // Not provided in DTO
                            type = itemDto.sessionType
                        )"""
    good_timetable_mapping = """                        com.example.features.parent.models.TimetablePeriod(
                            periodNo = index + 1,
                            time = "${itemDto.startTime} - ${itemDto.endTime}",
                            subjectName = itemDto.courseName,
                            subjectCode = "",
                            room = itemDto.roomNo,
                            instructor = ""
                        )"""
    faculty = faculty.replace(bad_timetable_mapping, good_timetable_mapping)
    with open(faculty_repo_path, 'w', encoding='utf-8') as f:
        f.write(faculty)

    print("Fixed all remaining build errors")

if __name__ == '__main__':
    fix_all()
