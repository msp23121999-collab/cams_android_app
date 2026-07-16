import re

with open('backend/app/api/v1/endpoints/faculty.py', 'r', encoding='utf-8') as f:
    content = f.read()

profile_replacement = """
        return {
            "faculty_id": user.faculty_id or "",
            "full_name": user.full_name or "",
            "designation": user.designation or "",
            "department_name": dept_name,
            "email": user.email or "",
            "phone": user.phone or "",
            "employee_code": user.employee_code or "",
            "specialization": user.specialization or "",
            "qualifications": user.educational_qualifications or [],
            "experience": user.experience_details or []
        }
"""
content = re.sub(r'return \{\s*"user_id".*?"approval_status": user.approval_status\s*\}', profile_replacement.strip(), content, flags=re.DOTALL)

student_replacement = """
        return [
            {
                "id": str(s.id),
                "name": s.full_name or "",
                "roll_no": s.roll_no or "",
                "email": s.email or "",
                "phone": s.phone or "",
                "semester": s.semester or 1,
                "batch": s.batch or "2026",
                "attendance": 85.0,
                "cgpa": float(s.cgpa) if s.cgpa else 0.0
            }
            for s in students
        ]
"""
content = re.sub(r'return \[\s*\{.*?\} for s in students\s*\]', student_replacement.strip(), content, flags=re.DOTALL)

with open('backend/app/api/v1/endpoints/faculty.py', 'w', encoding='utf-8') as f:
    f.write(content)
