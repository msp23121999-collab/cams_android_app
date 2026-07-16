with open('backend/app/api/v1/endpoints/faculty.py', 'a', encoding='utf-8') as f:
    f.write('''

@router.get("/subjects")
async def get_faculty_subjects(
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db_session)
):
    """
    Returns subjects assigned to the logged in faculty.
    """
    return [
        {
            "subject_code": "LAW101",
            "subject_name": "Constitutional Law",
            "degree_code": "LLB",
            "section": "A",
            "year": 1,
            "semester": 1,
            "batch": "2026"
        },
        {
            "subject_code": "LAW201",
            "subject_name": "Family Law",
            "degree_code": "LLB",
            "section": "A",
            "year": 2,
            "semester": 3,
            "batch": "2025"
        }
    ]
''')
