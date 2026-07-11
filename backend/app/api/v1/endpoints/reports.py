import io
from datetime import datetime, date
from typing import Optional
from fastapi import APIRouter, Depends, HTTPException, Response, status
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select, and_
from xhtml2pdf import pisa

from app.core.dependencies import get_db_session, get_current_user
from app.db.models.user import User, UserRole
from app.db.models.student import Student, ParentStudentMap
from app.db.models.faculty import FacultyProfile
from app.db.models.attendance import Attendance
from app.db.models.marks import InternalMark
from app.db.models.academic import Course, Section, Department, Degree
from app.db.models.payroll import Salary, Deduction, SalarySlip
from app.db.models.pf import PFConfiguration, PFContribution
from app.db.models.fee import FeeRecord, FeeStructure, Payment

router = APIRouter()

def render_to_pdf(html_content: str) -> Response:
    """Helper to convert HTML to a PDF response via xhtml2pdf."""
    pdf_buffer = io.BytesIO()
    pisa_status = pisa.CreatePDF(html_content, dest=pdf_buffer)
    if pisa_status.err:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR, 
            detail="Failed to generate PDF document layout."
        )
    pdf_data = pdf_buffer.getvalue()
    pdf_buffer.close()
    return Response(
        content=pdf_data,
        media_type="application/pdf"
    )

# --- 1. Student Profile Resume PDF ---
@router.get("/student/{user_id}/pdf")
async def generate_student_resume_pdf(
    user_id: str,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db_session)
):
    # Fetch user & student profile
    usr_q = await db.execute(select(User).where(User.id == user_id, User.is_deleted.is_(False)))
    usr = usr_q.scalar_one_or_none()
    student = None
    if not usr:
        # Fallback: check if user_id is Student.id
        std_q = await db.execute(select(Student).where(Student.id == user_id, Student.is_deleted.is_(False)))
        student = std_q.scalar_one_or_none()
        if student:
            user_id = student.user_id
            usr_q = await db.execute(select(User).where(User.id == user_id, User.is_deleted.is_(False)))
            usr = usr_q.scalar_one_or_none()
            
    if not usr:
        raise HTTPException(status_code=404, detail="Student user not found")
        
    if not student:
        std_q = await db.execute(select(Student).where(Student.user_id == user_id, Student.is_deleted.is_(False)))
        student = std_q.scalar_one_or_none()
        
    if not student:
        raise HTTPException(status_code=404, detail="Student profile not found")

    dept_name = "N/A"
    if student.department_id:
        dept = await db.get(Department, student.department_id)
        if dept:
            dept_name = dept.name
            
    reg_name = "N/A"
    if student.degree_id:
        reg = await db.get(Degree, student.degree_id)
        if reg:
            reg_name = reg.name

    # Fetch marks
    marks_q = await db.execute(
        select(InternalMark, Course)
        .join(Course, InternalMark.subject_id == Course.id)
        .where(InternalMark.student_id == student.id, InternalMark.is_deleted.is_(False))
    )
    marks_list = marks_q.all()

    # Fetch attendance summary
    from app.services.attendance_service import AttendanceService
    att_service = AttendanceService(db)
    summary = await att_service.get_student_attendance_summary(student.id)
    att_pct = round(summary["percentage"], 1)
    present_att = summary["present"] + summary["od"]
    total_att = summary["total"]

    # Resolve student photo
    photo_html = ""
    name_initial = usr.full_name[0] if usr.full_name else "S"
    if student.profile_photo_url:
        import os
        static_dir = os.path.join(
            os.path.dirname(os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))),
            "static"
        )
        filename = student.profile_photo_url.split("/")[-1]
        photo_path = os.path.join(static_dir, "uploads", filename)
        if os.path.exists(photo_path):
            photo_html = f'<img src="{photo_path}" width="80" height="100" style="border: 1px solid #D1D5DB;" />'
        else:
            photo_html = f'<table style="width: 80px; height: 100px; border: 1px solid #D1D5DB; background-color: #F3F4F6; text-align: center;"><tr><td style="vertical-align: middle; font-size: 24px; font-weight: bold; color: #9CA3AF;">{name_initial}</td></tr></table>'
    else:
        photo_html = f'<table style="width: 80px; height: 100px; border: 1px solid #D1D5DB; background-color: #F3F4F6; text-align: center;"><tr><td style="vertical-align: middle; font-size: 24px; font-weight: bold; color: #9CA3AF;">{name_initial}</td></tr></table>'

    # Safe lists/dicts parsing
    internships = student.internships or []
    certifications = student.certifications or []
    sports_records = student.sports_records or []
    skills = student.skills or []
    special_skills = student.special_skills or []
    languages = student.languages_known or []
    hobbies = student.hobbies_interests or []

    # Internships HTML block
    internships_html = ""
    if internships:
        internships_html += """
        <table class="data-table">
            <thead>
                <tr>
                    <th style="width: 25%;">Organization</th>
                    <th style="width: 20%;">Role / Supervisor</th>
                    <th style="width: 15%;">Period</th>
                    <th style="width: 40%;">Key Responsibilities</th>
                </tr>
            </thead>
            <tbody>
        """
        for item in internships:
            org = item.get("organization", "N/A")
            role = item.get("role", "N/A")
            sup = item.get("supervisor", "N/A")
            start = item.get("startDate", "N/A")
            end = item.get("endDate", "N/A")
            resp = item.get("responsibilities", "N/A")
            internships_html += f"""
                <tr>
                    <td style="font-weight: bold; color: #111827;">{org}</td>
                    <td>{role}<br/><span style="font-size: 7px; color: #6B7280;">Sup: {sup}</span></td>
                    <td>{start} to {end}</td>
                    <td style="font-size: 7px; color: #4B5563;">{resp}</td>
                </tr>
            """
        internships_html += "</tbody></table>"
    else:
        internships_html = '<p style="font-style: italic; color: #6B7280; font-size: 8px;">No internship history recorded.</p>'

    # Certifications HTML block
    cert_html = ""
    if certifications:
        cert_html += """
        <table class="data-table">
            <thead>
                <tr>
                    <th style="width: 15%;">ID</th>
                    <th style="width: 45%;">Certification Title</th>
                    <th style="width: 25%;">Issuing Authority</th>
                    <th style="width: 15%;">Date</th>
                </tr>
            </thead>
            <tbody>
        """
        for c in certifications:
            cid = c.get("id", "N/A")
            title = c.get("title", "N/A")
            auth = c.get("authority", "N/A")
            dt = c.get("date", "N/A")
            cert_html += f"""
                <tr>
                    <td style="font-weight: bold; color: #4F46E5;">{cid}</td>
                    <td>{title}</td>
                    <td>{auth}</td>
                    <td>{dt}</td>
                </tr>
            """
        cert_html += "</tbody></table>"
    else:
        cert_html = '<p style="font-style: italic; color: #6B7280; font-size: 8px;">No moot certifications recorded.</p>'

    # Sports / Extracurricular Achievements HTML block
    sports_html = ""
    if sports_records:
        sports_html += """
        <table class="data-table">
            <thead>
                <tr>
                    <th style="width: 60%;">Event Description</th>
                    <th style="width: 25%;">Award / Honor</th>
                    <th style="width: 15%;">Year</th>
                </tr>
            </thead>
            <tbody>
        """
        for s in sports_records:
            evt = s.get("event", "N/A")
            awd = s.get("award", "N/A")
            yr = s.get("year", "N/A")
            sports_html += f"""
                <tr>
                    <td style="font-weight: bold; color: #111827;">{evt}</td>
                    <td style="color: #10B981; font-weight: bold;">{awd}</td>
                    <td>{yr}</td>
                </tr>
            """
        sports_html += "</tbody></table>"
    else:
        sports_html = '<p style="font-style: italic; color: #6B7280; font-size: 8px;">No extracurricular achievements recorded.</p>'

    # Render Template
    html = f"""
    <html>
    <head>
        <style>
            body {{ font-family: Helvetica, Arial, sans-serif; font-size: 9px; color: #374151; padding: 10px; }}
            .page-break {{ page-break-after: always; }}
            .header-table {{ width: 100%; border-bottom: 2px solid #4F46E5; padding-bottom: 8px; margin-bottom: 12px; }}
            .college-title {{ font-size: 13px; font-weight: bold; color: #4F46E5; }}
            .student-name {{ font-size: 15px; font-weight: bold; color: #111827; text-transform: uppercase; }}
            .section-title {{ font-size: 9px; font-weight: bold; color: #4F46E5; border-bottom: 1px solid #E5E7EB; padding-bottom: 2px; margin-top: 10px; margin-bottom: 5px; text-transform: uppercase; }}
            .details-table {{ width: 100%; margin-bottom: 8px; }}
            .details-table td {{ padding: 3px 4px; vertical-align: top; }}
            .label {{ font-weight: bold; color: #4B5563; width: 25%; }}
            .value {{ color: #1F2937; width: 25%; }}
            .data-table {{ width: 100%; border-collapse: collapse; margin-top: 3px; margin-bottom: 8px; }}
            .data-table th {{ background-color: #F3F4F6; color: #111827; border: 1px solid #D1D5DB; padding: 4px; font-weight: bold; text-align: left; font-size: 8px; }}
            .data-table td {{ border: 1px solid #E5E7EB; padding: 4px; font-size: 8px; }}
            .kpi-table {{ width: 100%; margin-top: 4px; margin-bottom: 8px; }}
            .kpi-card {{ border: 1px solid #E5E7EB; background-color: #F9FAFB; padding: 6px; text-align: center; border-radius: 4px; }}
            .kpi-value {{ font-size: 13px; font-weight: bold; color: #111827; margin-top: 2px; }}
            .kpi-label {{ font-size: 7px; font-weight: bold; color: #6B7280; text-transform: uppercase; }}
            .signatures-table {{ width: 100%; margin-top: 25px; text-align: center; font-size: 8px; }}
            .sig-space {{ border-top: 1px solid #9CA3AF; width: 80%; margin: 0 auto; padding-top: 3px; }}
            .footer {{ text-align: center; margin-top: 20px; font-size: 7px; color: #9CA3AF; border-top: 1px solid #E5E7EB; padding-top: 8px; }}
        </style>
    </head>
    <body>
        <!-- PAGE 1: Personal & Family Profile -->
        <table class="header-table">
            <tr>
                <td style="vertical-align: middle;">
                    <div class="college-title">KNOWLEDGE COLLEGE OF LAW</div>
                    <div style="font-size: 8px; color: #6B7280;">Official Student Portfolio & Resume Profile</div>
                    <div style="height: 8px;"></div>
                    <div class="student-name">{usr.full_name}</div>
                    <div style="font-size: 8px; color: #4B5563; margin-top: 2px;">
                        Roll No: {student.roll_no or "N/A"} | Email: {usr.email}
                    </div>
                </td>
                <td style="text-align: right; width: 85px; vertical-align: middle;">
                    {photo_html}
                </td>
            </tr>
        </table>

        <div class="section-title">Academic Enrollment Details</div>
        <table class="details-table">
            <tr>
                <td class="label">Register Number:</td>
                <td class="value">{student.roll_no or "N/A"}</td>
                <td class="label">Associated Department:</td>
                <td class="value">{dept_name}</td>
            </tr>
            <tr>
                <td class="label">Admission Quota:</td>
                <td class="value">{student.quota or "Government"}</td>
                <td class="label">Degree Program:</td>
                <td class="value">{reg_name}</td>
            </tr>
            <tr>
                <td class="label">Current Semester:</td>
                <td class="value">Semester {student.semester}</td>
                <td class="label">Enrollment Batch:</td>
                <td class="value">{student.batch_year}</td>
            </tr>
        </table>

        <div class="section-title">Personal Profile Details</div>
        <table class="details-table">
            <tr>
                <td class="label">Date of Birth:</td>
                <td class="value">{student.date_of_birth.strftime("%Y-%m-%d") if isinstance(student.date_of_birth, (date, datetime)) else student.date_of_birth or "N/A"}</td>
                <td class="label">Gender:</td>
                <td class="value">{student.gender or "N/A"}</td>
            </tr>
            <tr>
                <td class="label">Blood Group:</td>
                <td class="value">{student.blood_group or "N/A"}</td>
                <td class="label">Nationality:</td>
                <td class="value">{student.nationality or "N/A"}</td>
            </tr>
            <tr>
                <td class="label">Community Category:</td>
                <td class="value">{student.community_category or "N/A"}</td>
                <td class="label">Religion:</td>
                <td class="value">{student.religion or "N/A"}</td>
            </tr>
        </table>

        <div class="section-title">Contact & Identification</div>
        <table class="details-table">
            <tr>
                <td class="label">Mobile Number:</td>
                <td class="value">{student.mobile_number or usr.phone or "N/A"}</td>
                <td class="label">Official Email:</td>
                <td class="value">{usr.email}</td>
            </tr>
            <tr>
                <td class="label">Aadhaar Number:</td>
                <td class="value">{student.aadhaar_number or "N/A"}</td>
                <td class="label">Passport Number:</td>
                <td class="value">{student.passport_number or "N/A"}</td>
            </tr>
        </table>

        <div class="section-title">Parent / Guardian Details</div>
        <table class="details-table">
            <tr>
                <td class="label">Father's Name:</td>
                <td class="value">{student.father_name or "N/A"}</td>
                <td class="label">Mother's Name:</td>
                <td class="value">{student.mother_name or "N/A"}</td>
            </tr>
            <tr>
                <td class="label">Father's Occupation:</td>
                <td class="value">{student.father_occupation or "N/A"}</td>
                <td class="label">Mother's Occupation:</td>
                <td class="value">{student.mother_occupation or "N/A"}</td>
            </tr>
            <tr>
                <td class="label">Father's Mobile:</td>
                <td class="value">{student.father_mobile or "N/A"}</td>
                <td class="label">Mother's Mobile:</td>
                <td class="value">{student.mother_mobile or "N/A"}</td>
            </tr>
            <tr>
                <td class="label">Father's Office Address:</td>
                <td class="value">{student.father_office_address or "N/A"}</td>
                <td class="label">Mother's Office Address:</td>
                <td class="value">{student.mother_office_address or "N/A"}</td>
            </tr>
            <tr>
                <td class="label">Annual Family Income:</td>
                <td class="value">{student.parent_annual_income or "N/A"}</td>
                <td class="label">Emergency Contact:</td>
                <td class="value">{student.emergency_contact_name or "N/A"} ({student.emergency_contact_relationship or "N/A"}) - {student.emergency_contact_number or "N/A"}</td>
            </tr>
        </table>

        <div class="section-title">Residential Addresses</div>
        <table class="details-table">
            <tr>
                <td class="label" style="width: 20%;">Current Address:</td>
                <td class="value" style="width: 80%;">{student.current_address or "N/A"}</td>
            </tr>
            <tr>
                <td class="label" style="width: 20%;">Permanent Address:</td>
                <td class="value" style="width: 80%;">{student.permanent_address or "N/A"}</td>
            </tr>
        </table>

        <div class="footer">
            Generated via CAMS Student Ledger. Page 1 of 2. Integrity Verification Code: {student.id[:8].upper()}-{datetime.now().strftime("%y%m%d")}
        </div>

        <div class="page-break"></div>

        <!-- PAGE 2: Accomplishments & Academic Transcript -->
        <table class="header-table">
            <tr>
                <td style="vertical-align: middle;">
                    <div class="college-title">KNOWLEDGE COLLEGE OF LAW</div>
                    <div style="font-size: 8px; color: #6B7280;">Student Accomplishments, Internships & Transcript</div>
                    <div style="height: 8px;"></div>
                    <div class="student-name">{usr.full_name}</div>
                </td>
                <td style="text-align: right; vertical-align: bottom;">
                    <span style="font-size: 8px; color: #4B5563; font-weight: bold;">Roll No: {student.roll_no or "N/A"}</span>
                </td>
            </tr>
        </table>

        <div class="section-title">Key Academic Performance</div>
        <table class="kpi-table">
            <tr>
                <td style="width: 50%; padding-right: 5px;">
                    <div class="kpi-card">
                        <div class="kpi-label">Cumulative GPA (CGPA)</div>
                        <div class="kpi-value">{student.cgpa or "N/A"}</div>
                    </div>
                </td>
                <td style="width: 50%; padding-left: 5px;">
                    <div class="kpi-card">
                        <div class="kpi-label">Attendance Standing</div>
                        <div class="kpi-value" style="color: {'#10B981' if att_pct >= 75 else '#EF4444'};">{att_pct}% ({present_att}/{total_att} classes)</div>
                    </div>
                </td>
            </tr>
        </table>

        <div class="section-title">Languages, Skills & Competencies</div>
        <table class="details-table">
            <tr>
                <td class="label" style="width: 20%;">Professional Skills:</td>
                <td class="value" style="width: 80%;">{", ".join(skills) if skills else "N/A"}</td>
            </tr>
            <tr>
                <td class="label" style="width: 20%;">Special Core Areas:</td>
                <td class="value" style="width: 80%;">{", ".join(special_skills) if special_skills else "N/A"}</td>
            </tr>
            <tr>
                <td class="label" style="width: 20%;">Languages Known:</td>
                <td class="value" style="width: 80%;">{", ".join(languages) if languages else "N/A"}</td>
            </tr>
            <tr>
                <td class="label" style="width: 20%;">Hobbies & Interests:</td>
                <td class="value" style="width: 80%;">{", ".join(hobbies) if hobbies else "N/A"}</td>
            </tr>
        </table>

        <div class="section-title">Legal Internships Ledger</div>
        {internships_html}

        <div class="section-title">Moot Court Certifications & Workshops</div>
        {cert_html}

        {f'<div class="section-title">Extracurricular Honors & Achievements</div>{sports_html}' if sports_records else ''}

        <div class="section-title">Internal Examination Grades Summary</div>
        {f'<table class="data-table"><thead><tr><th>COURSE CODE</th><th>SUBJECT NAME</th><th>EXAM (40)</th><th>ASG (20)</th><th>PRES (20)</th><th>VIVA (20)</th><th>ATT (10)</th><th>TOTAL (100)</th></tr></thead><tbody>' if len(marks_list) > 0 else '<p style="font-style: italic; color: #6b7280; font-size: 8px;">No exam marks recorded for this semester yet.</p>'}
        """
    for m, c in marks_list:
        html += f"""
        <tr>
            <td style="font-weight: bold; color: #4F46E5;">{c.code or "LAW"}</td>
            <td>{c.name}</td>
            <td>{m.internal_exam_mark}</td>
            <td>{m.assignment_mark}</td>
            <td>{m.presentation_mark}</td>
            <td>{m.viva_voice_mark}</td>
            <td>{m.attendance_mark}</td>
            <td style="font-weight: bold;">{m.total_mark}</td>
        </tr>
        """
    if len(marks_list) > 0:
        html += "</tbody></table>"

    html += f"""
        <table class="signatures-table">
            <tr>
                <td style="width: 33%;">
                    <div style="height: 25px;"></div>
                    <div class="sig-space">Faculty Advisor Signature</div>
                </td>
                <td style="width: 33%;">
                    <div style="height: 25px;"></div>
                    <div class="sig-space">Head of Department</div>
                </td>
                <td style="width: 33%;">
                    <div style="height: 25px;"></div>
                    <div class="sig-space">Principal Office Seal</div>
                </td>
            </tr>
        </table>

        <div class="footer">
            Generated via CAMS Student Ledger. Page 2 of 2. Integrity Verification Code: {student.id[:8].upper()}-{datetime.now().strftime("%y%m%d")}
        </div>
    </body>
    </html>
    """
    return render_to_pdf(html)

# --- 2. Faculty CV Profile PDF ---
@router.get("/faculty/{user_id}/pdf")
async def generate_faculty_resume_pdf(
    user_id: str,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db_session)
):
    from app.db.models.faculty import FacultyResearch
    
    usr_q = await db.execute(select(User).where(User.id == user_id, User.is_deleted.is_(False)))
    usr = usr_q.scalar_one_or_none()
    fac = None
    if not usr:
        # Fallback: check if user_id is FacultyProfile.id
        fac_q = await db.execute(select(FacultyProfile).where(FacultyProfile.id == user_id, FacultyProfile.is_deleted.is_(False)))
        fac = fac_q.scalar_one_or_none()
        if fac:
            user_id = fac.user_id
            usr_q = await db.execute(select(User).where(User.id == user_id, User.is_deleted.is_(False)))
            usr = usr_q.scalar_one_or_none()
            
    if not usr:
        raise HTTPException(status_code=404, detail="Faculty user not found")
        
    if not fac:
        fac_q = await db.execute(select(FacultyProfile).where(FacultyProfile.user_id == user_id, FacultyProfile.is_deleted.is_(False)))
        fac = fac_q.scalar_one_or_none()
        
    if not fac:
        raise HTTPException(status_code=404, detail="Faculty profile not found")

    # Fetch courses taught by faculty
    courses_q = await db.execute(
        select(Course, Section)
        .join(Section, Course.id == Section.course_id)
        .where(Section.faculty_id == user_id, Section.is_deleted.is_(False))
    )
    courses_list = courses_q.all()

    # Fetch research and publications
    research_q = await db.execute(
        select(FacultyResearch)
        .where(FacultyResearch.faculty_id == user_id, FacultyResearch.is_deleted.is_(False))
        .order_by(FacultyResearch.publication_date.desc())
    )
    research_list = research_q.scalars().all()

    # ── Mandatory Fields Check ─────────────────────────────────────────────────
    # Only block if the absolute essentials are missing (name + email are always
    # set when the User row exists, so this is a safety net only).
    import logging
    logger = logging.getLogger("app.api.v1.reports")

    missing_mandatory = []
    if not usr.full_name or not usr.full_name.strip():
        missing_mandatory.append("Faculty Name")
    if not usr.email or not usr.email.strip():
        missing_mandatory.append("Email")

    if missing_mandatory:
        bullet = "\n".join([f"* {f}" for f in missing_mandatory])
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"Missing Mandatory Fields:\n\n{bullet}"
        )

    logger.info(
        f"Generating CV PDF for faculty user_id={user_id}  "
        f"name={usr.full_name!r}  approval_status={fac.approval_status!r}"
    )

    dept_name = "N/A"
    if usr.department_id:
        dept = await db.get(Department, usr.department_id)
        if dept:
            dept_name = dept.name

    # ═══════════════════════════════════════════════════════════════════════════
    #  ReportLab PDF — pure Python, pixel-perfect layout
    # ═══════════════════════════════════════════════════════════════════════════
    import io, os
    from reportlab.lib.pagesizes import A4
    from reportlab.lib.units   import cm
    from reportlab.lib         import colors
    from reportlab.lib.styles  import ParagraphStyle
    from reportlab.platypus    import (
        SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle
    )
    from reportlab.pdfgen      import canvas as rl_canvas

    # ── Colour palette ────────────────────────────────────────────────────────
    NAVY      = colors.Color(26/255,  53/255,  87/255)    # #1a3557
    NAVY_DARK = colors.Color(15/255,  32/255,  60/255)    # contact strip bg
    GOLD      = colors.Color(200/255, 168/255, 75/255)    # #c8a84b
    L_BLUE    = colors.Color(232/255, 240/255, 248/255)   # #e8f0f8 row alt
    WHITE     = colors.white
    DARK      = colors.Color(31/255,  41/255,  55/255)
    PALE_BLUE = colors.Color(191/255, 219/255, 254/255)
    MID_BLUE  = colors.Color(59/255,  130/255, 246/255)
    BADGE_BG  = colors.Color(29/255,  78/255,  216/255)
    BADGE_TXT = colors.Color(147/255, 197/255, 253/255)
    CTX_TXT   = colors.Color(219/255, 234/255, 254/255)
    DIVIDER   = colors.Color(209/255, 213/255, 219/255)

    # ── Page geometry (points) ────────────────────────────────────────────────
    PW, PH   = A4             # 595.28 x 841.89 pt
    MARGIN   = 2.2 * cm       # 62.36 pt
    BODY_W   = PW - 2*MARGIN
    GOLD_H   = 5              # gold accent stripe at top
    BANNER_H = 100            # navy name/desig band
    STRIP_H  = 26             # contact strip
    HEADER_H = GOLD_H + BANNER_H + STRIP_H   # 131 pt
    FOOTER_H = 28             # footer bar
    TOP_M    = HEADER_H + 12  # body top margin
    BOT_M    = FOOTER_H + 10  # body bottom margin

    # ── Safe date strings ─────────────────────────────────────────────────────
    dob_str = (
        fac.date_of_birth.strftime("%d %b %Y")
        if isinstance(fac.date_of_birth, (date, datetime))
        else (str(fac.date_of_birth) if fac.date_of_birth else "—")
    )
    doj_str = (
        fac.date_of_joining.strftime("%d %b %Y")
        if isinstance(fac.date_of_joining, (date, datetime))
        else (str(fac.date_of_joining) if fac.date_of_joining else "—")
    )
    now_str = datetime.now().strftime("%d %b %Y, %I:%M %p")

    # ── Photo path ────────────────────────────────────────────────────────────
    photo_path = None
    if fac.profile_photo_url:
        static_dir = os.path.join(
            os.path.dirname(os.path.dirname(os.path.dirname(
                os.path.dirname(os.path.abspath(__file__))))), "static"
        )
        candidate = os.path.join(static_dir, "uploads",
                                 fac.profile_photo_url.split("/")[-1])
        if os.path.exists(candidate):
            photo_path = candidate

    # ── Subjects taught ───────────────────────────────────────────────────────
    subjects_handled, seen_s = [], set()
    for c_row, _s in courses_list:
        key = f"{c_row.name} ({c_row.code})"
        if key not in seen_s:
            seen_s.add(key)
            subjects_handled.append(key)

    # ── Faculty initials (for avatar placeholder) ─────────────────────────────
    initials = "".join(p[0].upper() for p in (usr.full_name or "F").split() if p)[:2]

    # ═══════════════════════════════════════════════════════════════════════════
    #  CVCanvas — NumberedCanvas pattern: defers pages so header/footer can
    #  stamp "Page X of Y" only after the total is known.
    # ═══════════════════════════════════════════════════════════════════════════
    class CVCanvas(rl_canvas.Canvas):
        def __init__(self, *args, **kwargs):
            rl_canvas.Canvas.__init__(self, *args, **kwargs)
            self._page_states: list = []

        def showPage(self):
            self._page_states.append(dict(self.__dict__))
            self._startPage()

        def save(self):
            total = len(self._page_states)
            for page_no, state in enumerate(self._page_states, start=1):
                self.__dict__.update(state)
                self._stamp(page_no, total)
                rl_canvas.Canvas.showPage(self)
            rl_canvas.Canvas.save(self)

        # ── Per-page stamp ────────────────────────────────────────────────────
        def _stamp(self, page_no: int, total: int):
            self.saveState()

            if page_no == 1:
                # ════════════════════════════════════════════════
                #  PAGE 1 — Full premium header
                # ════════════════════════════════════════════════

                # ① Gold accent bar at very top
                self.setFillColor(GOLD)
                self.rect(0, PH - GOLD_H, PW, GOLD_H, fill=1, stroke=0)

                # ② Navy banner
                banner_y = PH - GOLD_H - BANNER_H
                self.setFillColor(NAVY)
                self.rect(0, banner_y, PW, BANNER_H, fill=1, stroke=0)

                # ③ Left accent bar (gold vertical stripe inside banner)
                self.setFillColor(GOLD)
                self.rect(0, banner_y, 5, BANNER_H, fill=1, stroke=0)

                # ④ Faculty name
                name_y = PH - GOLD_H - 32
                self.setFont("Helvetica-Bold", 20)
                self.setFillColor(WHITE)
                self.drawString(MARGIN + 8, name_y, (usr.full_name or "").upper())

                # Gold underline
                self.setStrokeColor(GOLD)
                self.setLineWidth(1.5)
                self.line(MARGIN + 8, name_y - 5, MARGIN + 220, name_y - 5)

                # ⑤ Designation — gold, 15pt bold
                self.setFont("Helvetica-Bold", 15)
                self.setFillColor(GOLD)
                self.drawString(MARGIN + 8, name_y - 24,
                                fac.designation or "Faculty Member")

                # ⑥ Department — pale blue, 12pt
                self.setFont("Helvetica", 12)
                self.setFillColor(PALE_BLUE)
                self.drawString(MARGIN + 8, name_y - 43,
                                f"Department of {dept_name}")

                # ⑦ Institution — italic, 10pt
                self.setFont("Helvetica-Oblique", 10)
                self.setFillColor(BADGE_TXT)
                self.drawString(MARGIN + 8, name_y - 59,
                                "CAMS Law  \u2022  Department of Law  \u2022  Institution of Legal Excellence")

                # ⑧ Photo / monogram (right side of banner)
                PBW, PBH = 72, 88
                px = PW - MARGIN - PBW
                py = banner_y + (BANNER_H - PBH) // 2
                if photo_path:
                    try:
                        self.drawImage(photo_path, px, py,
                                       width=PBW, height=PBH,
                                       preserveAspectRatio=True, mask="auto")
                    except Exception:
                        self._monogram(px, py, PBW, PBH)
                else:
                    self._monogram(px, py, PBW, PBH)
                self.setStrokeColor(GOLD)
                self.setLineWidth(2)
                self.rect(px, py, PBW, PBH, fill=0, stroke=1)

                # ⑨ Contact strip
                strip_y = banner_y - STRIP_H
                self.setFillColor(NAVY_DARK)
                self.rect(0, strip_y, PW, STRIP_H, fill=1, stroke=0)
                contacts = [
                    ("EMAIL", usr.email        or "\u2014"),
                    ("PHONE", usr.phone        or "\u2014"),
                    ("ID",    fac.faculty_id   or "\u2014"),
                    ("EMP",   fac.employee_code or "\u2014"),
                ]
                col_w = BODY_W / len(contacts)
                for i, (badge, val) in enumerate(contacts):
                    cx = MARGIN + i * col_w
                    self.setFillColor(BADGE_BG)
                    self.roundRect(cx, strip_y + 5, 32, 15, 2, fill=1, stroke=0)
                    self.setFont("Helvetica-Bold", 7)
                    self.setFillColor(BADGE_TXT)
                    self.drawCentredString(cx + 16, strip_y + 9, badge)
                    self.setFont("Helvetica", 8.5)
                    self.setFillColor(CTX_TXT)
                    self.drawString(cx + 36, strip_y + 9, str(val)[:30])
                    if i < len(contacts) - 1:
                        self.setStrokeColor(MID_BLUE)
                        self.setLineWidth(0.4)
                        self.line(cx + col_w - 3, strip_y + 4,
                                  cx + col_w - 3, strip_y + 22)

            else:
                # ════════════════════════════════════════════════
                #  PAGE 2+ — Slim continuation bar only
                #  (sits in the top margin area, no repetition)
                # ════════════════════════════════════════════════
                CONT_H = 30   # slim bar height (pt)
                cont_y = PH - CONT_H

                # Gold 3pt accent stripe
                self.setFillColor(GOLD)
                self.rect(0, PH - 3, PW, 3, fill=1, stroke=0)

                # Slim navy bar
                self.setFillColor(NAVY)
                self.rect(0, cont_y, PW, CONT_H - 3, fill=1, stroke=0)

                # Gold left accent stripe
                self.setFillColor(GOLD)
                self.rect(0, cont_y, 4, CONT_H - 3, fill=1, stroke=0)

                # Faculty name (left)
                self.setFont("Helvetica-Bold", 11)
                self.setFillColor(WHITE)
                self.drawString(MARGIN + 8, cont_y + 8,
                                (usr.full_name or "").upper())

                # Right: "Curriculum Vitae  |  Page X of Y"
                right_txt = f"Curriculum Vitae  |  Page {page_no} of {total}"
                self.setFont("Helvetica-Oblique", 9)
                self.setFillColor(PALE_BLUE)
                self.drawRightString(PW - MARGIN - 6, cont_y + 9, right_txt)

            # ════════════════════════════════════════════════
            #  FOOTER — every page
            # ════════════════════════════════════════════════
            self.setFillColor(NAVY)
            self.rect(0, 0, PW, FOOTER_H, fill=1, stroke=0)
            footer_parts = [
                f"Faculty ID: {fac.faculty_id or 'N/A'}",
                f"Dept: {dept_name}",
                "CAMS Law School",
                f"Page {page_no} of {total}",
                f"Generated: {now_str}",
            ]
            part_w = BODY_W / len(footer_parts)
            self.setFont("Helvetica", 7.5)
            self.setFillColor(CTX_TXT)
            for i, part in enumerate(footer_parts):
                fx = MARGIN + i * part_w
                self.drawString(fx, 9, part)
                if i < len(footer_parts) - 1:
                    self.setStrokeColor(MID_BLUE)
                    self.setLineWidth(0.3)
                    self.line(fx + part_w - 5, 4, fx + part_w - 5, 22)

            self.restoreState()


        def _monogram(self, x, y, w, h):
            self.setFillColor(NAVY)
            self.rect(x, y, w, h, fill=1, stroke=0)
            self.setFont("Helvetica-Bold", 26)
            self.setFillColor(GOLD)
            self.drawCentredString(x + w / 2, y + h / 2 - 9, initials)

    # ═══════════════════════════════════════════════════════════════════════════
    #  Paragraph styles
    # ═══════════════════════════════════════════════════════════════════════════
    S_BODY = ParagraphStyle("body", fontName="Helvetica",      fontSize=10,
                             textColor=DARK,  leading=14)
    S_LBL  = ParagraphStyle("lbl",  fontName="Helvetica-Bold", fontSize=10,
                             textColor=DARK,  leading=14)
    S_SEC  = ParagraphStyle("sec",  fontName="Helvetica-Bold", fontSize=11,
                             textColor=WHITE, leading=16)
    S_TH   = ParagraphStyle("th",   fontName="Helvetica-Bold", fontSize=9.5,
                             textColor=WHITE, leading=13)
    S_TD   = ParagraphStyle("td",   fontName="Helvetica",      fontSize=9.5,
                             textColor=DARK,  leading=13)
    S_CERT = ParagraphStyle("cert", fontName="Helvetica",      fontSize=10.5,
                             textColor=DARK,  leading=17)
    S_CHK  = ParagraphStyle("chk",  fontName="Helvetica-Bold", fontSize=13,
                             textColor=GOLD,  leading=17)

    # ═══════════════════════════════════════════════════════════════════════════
    #  Flowable helpers
    # ═══════════════════════════════════════════════════════════════════════════
    def sec_heading(num: int, title: str):
        """Navy bar with white bold numbered section title."""
        t = Table(
            [[Paragraph(f"<b>{num:02d}&nbsp;&nbsp;{title.upper()}</b>", S_SEC)]],
            colWidths=[BODY_W]
        )
        t.setStyle(TableStyle([
            ("BACKGROUND",   (0,0),(-1,-1), NAVY),
            ("LEFTPADDING",  (0,0),(-1,-1), 12),
            ("RIGHTPADDING", (0,0),(-1,-1), 12),
            ("TOPPADDING",   (0,0),(-1,-1), 8),
            ("BOTTOMPADDING",(0,0),(-1,-1), 8),
        ]))
        return t

    def info_grid(pairs):
        """4-column (lbl | val | lbl | val) with alternating row shading."""
        rows = []
        for i in range(0, len(pairs), 2):
            l1, v1 = pairs[i]
            l2, v2 = pairs[i+1] if i+1 < len(pairs) else ("", "")
            rows.append([
                Paragraph(f"<b>{l1}</b>",    S_LBL),
                Paragraph(str(v1),           S_BODY),
                Paragraph(f"<b>{l2}</b>",    S_LBL)  if l2 else Paragraph("", S_LBL),
                Paragraph(str(v2),           S_BODY) if l2 else Paragraph("", S_BODY),
            ])
        cw = [BODY_W * f for f in [0.21, 0.29, 0.21, 0.29]]
        t  = Table(rows, colWidths=cw)
        ts = [
            ("GRID",         (0,0),(-1,-1), 0.5, DIVIDER),
            ("TOPPADDING",   (0,0),(-1,-1), 6),
            ("BOTTOMPADDING",(0,0),(-1,-1), 6),
            ("LEFTPADDING",  (0,0),(-1,-1), 9),
            ("RIGHTPADDING", (0,0),(-1,-1), 9),
            ("VALIGN",       (0,0),(-1,-1), "TOP"),
        ]
        for idx in range(len(rows)):
            ts.append(("BACKGROUND",(0,idx),(-1,idx),
                       L_BLUE if idx % 2 == 0 else WHITE))
        t.setStyle(TableStyle(ts))
        return t

    def data_table(headers, rows, col_widths):
        """Bordered table: navy header + alternating body rows."""
        hdr  = [Paragraph(f"<b>{h}</b>", S_TH) for h in headers]
        body = [[Paragraph(str(c), S_TD) for c in r] for r in rows]
        t    = Table([hdr] + body, colWidths=col_widths, repeatRows=1)
        ts   = [
            ("BACKGROUND",   (0,0), (-1,0),  NAVY),
            ("GRID",         (0,0), (-1,-1), 0.5, DIVIDER),
            ("TOPPADDING",   (0,0), (-1,-1), 6),
            ("BOTTOMPADDING",(0,0), (-1,-1), 6),
            ("LEFTPADDING",  (0,0), (-1,-1), 7),
            ("RIGHTPADDING", (0,0), (-1,-1), 7),
            ("VALIGN",       (0,0), (-1,-1), "TOP"),
        ]
        for idx in range(1, len(body)+1):
            ts.append(("BACKGROUND",(0,idx),(-1,idx),
                       L_BLUE if idx % 2 == 1 else WHITE))
        t.setStyle(TableStyle(ts))
        return t

    # ═══════════════════════════════════════════════════════════════════════════
    #  Build story
    # ═══════════════════════════════════════════════════════════════════════════
    story = []
    SM = Spacer(1, 0.22*cm)   # small gap
    LG = Spacer(1, 0.45*cm)   # gap between sections

    # 01 Personal Information
    story += [SM, sec_heading(1, "Personal Information"), SM]
    story.append(info_grid([
        ("Faculty ID",      fac.faculty_id       or "—"),
        ("Employee Code",   fac.employee_code    or "—"),
        ("Official Email",  usr.email            or "—"),
        ("Official Phone",  usr.phone            or "—"),
        ("Alternate Phone", fac.alternate_phone  or "—"),
        ("Personal Email",  fac.personal_email   or "—"),
        ("Gender",          fac.gender           or "—"),
        ("Date of Birth",   dob_str),
        ("Blood Group",     fac.blood_group      or "—"),
        ("Nationality",     fac.nationality      or "—"),
        ("Marital Status",  fac.marital_status   or "—"),
        ("Community",       fac.community        or "—"),
    ]))
    story.append(LG)

    # 02 Professional Information
    story += [sec_heading(2, "Professional Information"), SM]
    story.append(info_grid([
        ("Department",       dept_name),
        ("Designation",      fac.designation          or "—"),
        ("Employment Type",  fac.faculty_type         or "—"),
        ("Category",         fac.employment_category  or "—"),
        ("Date of Joining",  doj_str),
        ("Service Status",   fac.employment_status    or "—"),
        ("Specialization",   fac.specialization       or "—"),
        ("Subjects Handled", ", ".join(subjects_handled) if subjects_handled else "—"),
    ]))
    story.append(LG)

    # 03 Educational Qualifications
    quals = fac.educational_qualifications or []
    if quals:
        story += [sec_heading(3, "Educational Qualifications"), SM]
        q_cw = [BODY_W * w for w in [0.17, 0.33, 0.22, 0.13, 0.15]]
        q_rows = []
        for q in quals:
            inst = q.get("institution", "—")
            univ = q.get("university", "")
            q_rows.append([
                q.get("degree",             "—"),
                f"{inst}\n{univ}" if univ and univ != inst else inst,
                q.get("specialization",     "—"),
                q.get("year_of_completion", "—"),
                q.get("percentage_cgpa",    "—"),
            ])
        story.append(data_table(
            ["Degree", "Institution / University", "Specialization", "Year", "Grade / CGPA"],
            q_rows, q_cw
        ))
        story.append(LG)

    # 04 Research Publications & Projects
    if research_list:
        story += [sec_heading(4, "Research Publications & Projects"), SM]
        r_cw = [BODY_W * w for w in [0.05, 0.29, 0.24, 0.12, 0.13, 0.17]]
        r_rows = []
        for idx, r in enumerate(research_list, start=1):
            pub_d = (
                r.publication_date.strftime("%b %Y")
                if isinstance(r.publication_date, (date, datetime))
                else (str(r.publication_date) if r.publication_date else "—")
            )
            r_rows.append([
                str(idx),
                r.title            or "—",
                r.publication      or "—",
                r.research_type    or "Journal",
                pub_d,
                r.isbn_issn        or "—",
            ])
        story.append(data_table(
            ["#", "Title", "Journal / Conference", "Type", "Date", "ISBN / ISSN"],
            r_rows, r_cw
        ))
        story.append(LG)

    # 05 Certifications & Achievements
    certs = fac.certifications_achievements or []
    if certs:
        story += [sec_heading(5, "Certifications & Achievements"), SM]
        cert_rows = [
            [Paragraph("<b>\u2714</b>", S_CHK),
             Paragraph(str(c), S_CERT)]
            for c in certs
        ]
        ct = Table(cert_rows, colWidths=[0.65*cm, BODY_W - 0.65*cm])
        ct.setStyle(TableStyle([
            ("VALIGN",       (0,0),(-1,-1), "TOP"),
            ("TOPPADDING",   (0,0),(-1,-1), 4),
            ("BOTTOMPADDING",(0,0),(-1,-1), 4),
            ("LEFTPADDING",  (0,0),(-1,-1), 2),
            ("RIGHTPADDING", (0,0),(-1,-1), 4),
        ]))
        story += [ct, LG]

    # ═══════════════════════════════════════════════════════════════════════════
    #  Assemble PDF
    # ═══════════════════════════════════════════════════════════════════════════
    buf = io.BytesIO()
    doc = SimpleDocTemplate(
        buf,
        pagesize=A4,
        leftMargin=MARGIN,
        rightMargin=MARGIN,
        topMargin=TOP_M,
        bottomMargin=BOT_M,
    )
    doc.build(story, canvasmaker=CVCanvas)

    pdf_bytes = buf.getvalue()
    fname = f"faculty_cv_{fac.faculty_id or user_id}.pdf"
    return Response(
        content=pdf_bytes,
        media_type="application/pdf",
        headers={"Content-Disposition": f'attachment; filename="{fname}"'},
    )





# --- 3. Filtered Attendance Report PDF ---
@router.get("/attendance/pdf")
async def generate_attendance_report_pdf(
    student_id: Optional[str] = None,
    section_id: Optional[str] = None,
    start_date: Optional[str] = None,
    end_date: Optional[str] = None,
    status_filter: Optional[str] = None,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db_session)
):
    # 1. Resolve student if student_id is provided
    student = None
    if student_id:
        student = await db.get(Student, student_id)
        if not student:
            std_q = await db.execute(select(Student).where(Student.user_id == student_id, Student.is_deleted.is_(False)))
            student = std_q.scalar_one_or_none()

    # 2. Build a mapping of sections to their students
    students_by_section = {}
    
    # Let's get the list of sections we need to query attendance for
    section_ids = []
    if section_id:
        section_ids = [section_id]
    elif student:
        section_name = "A"
        if student.section_id:
            sec_stmt = select(Section).where(Section.id == student.section_id)
            sec_res = await db.execute(sec_stmt)
            sec_obj = sec_res.scalar_one_or_none()
            if sec_obj:
                section_name = sec_obj.section_name or "A"
                
        section_ids_stmt = (
            select(Section.id)
            .join(Course, Section.course_id == Course.id)
            .where(
                Course.semester == student.semester,
                Course.dept_id == student.department_id,
                Section.section_name == section_name,
                Section.is_deleted.is_(False)
            )
        )
        section_ids_res = await db.execute(section_ids_stmt)
        section_ids = [r[0] for r in section_ids_res.all()]
        
    query = select(Attendance).where(Attendance.is_deleted.is_(False))
    if section_ids:
        query = query.where(Attendance.section_id.in_(section_ids))
    if start_date:
        query = query.where(Attendance.date >= date.fromisoformat(start_date))
    if end_date:
        query = query.where(Attendance.date <= date.fromisoformat(end_date))
        
    res = await db.execute(query.order_by(Attendance.date.desc()).limit(150))
    attendance_sessions = res.scalars().all()
    
    # Retrieve students for these sections
    unique_sec_ids = {a.section_id for a in attendance_sessions}
    for sec_id in unique_sec_ids:
        if student:
            student_user = await db.get(User, student.user_id)
            students_by_section[sec_id] = [(student, student_user)]
        else:
            sec_obj = await db.get(Section, sec_id)
            if sec_obj:
                course_obj = await db.get(Course, sec_obj.course_id)
                if course_obj:
                    stmt_s = select(Student, User).join(User, Student.user_id == User.id).where(
                        Student.section_id == sec_id,
                        Student.department_id == course_obj.dept_id,
                        Student.semester == course_obj.semester,
                        Student.is_deleted.is_(False)
                    )
                    res_s = await db.execute(stmt_s)
                    students_by_section[sec_id] = res_s.all()
                    
    # Construct virtual flat records list
    records = []
    for att in attendance_sessions:
        students_in_sec = students_by_section.get(att.section_id, [])
        for s, u in students_in_sec:
            status_val = "PRESENT"
            if att.absentee_ids and s.id in att.absentee_ids:
                status_val = "ABSENT"
            elif att.od_ids and s.id in att.od_ids:
                status_val = "OD"
                
            if status_filter and status_filter.upper() not in status_val:
                continue
                
            records.append({
                "student_name": u.full_name,
                "section_id": att.section_id,
                "date": att.date,
                "status_val": status_val,
                "status_style": "present" if status_val in ("PRESENT", "OD") else "absent"
            })
            
    total_rec = len(records)
    present_rec = sum(1 for r in records if r["status_val"] in ("PRESENT", "OD"))
    compliance_pct = round((present_rec / total_rec) * 100, 1) if total_rec > 0 else 100.0

    html = f"""
    <html>
    <head>
        <style>
            body {{ font-family: Helvetica, Arial, sans-serif; font-size: 9px; color: #374151; padding: 25px; }}
            .header {{ border-bottom: 2px solid #EF4444; padding-bottom: 10px; margin-bottom: 15px; }}
            .title {{ font-size: 14px; font-weight: bold; color: #EF4444; text-transform: uppercase; }}
            .meta-table {{ width: 100%; margin-bottom: 15px; border-bottom: 1px dashed #E5E7EB; padding-bottom: 8px; }}
            .data-table {{ width: 100%; border-collapse: collapse; }}
            .data-table th {{ background-color: #F3F4F6; color: #111827; border: 1px solid #D1D5DB; padding: 6px; font-weight: bold; text-align: left; }}
            .data-table td {{ border: 1px solid #E5E7EB; padding: 6px; }}
            .present {{ color: #10B981; font-weight: bold; }}
            .absent {{ color: #EF4444; font-weight: bold; }}
            .footer {{ text-align: center; margin-top: 30px; font-size: 8px; color: #9CA3AF; }}
        </style>
    </head>
    <body>
        <div class="header">
            <div class="title">CAMS Attendance Compliance Report</div>
            <div style="font-size: 8px; color: #6B7280;">Office of the Registrar · Academic Verification Ledger</div>
        </div>

        <table class="meta-table">
            <tr>
                <td style="font-weight: bold; color: #4b5563;">Report Date:</td>
                <td>{datetime.now().strftime("%Y-%m-%d %H:%M")}</td>
                <td style="font-weight: bold; color: #4b5563;">Overall Compliance:</td>
                <td style="font-weight: bold; color: {'#10B981' if compliance_pct >= 75 else '#EF4444'};">{compliance_pct}%</td>
            </tr>
            <tr>
                <td style="font-weight: bold; color: #4b5563;">Scope:</td>
                <td>{f'Student: {student.full_name if student else student_id}' if student_id else 'All Students'}</td>
                <td style="font-weight: bold; color: #4b5563;">Attendance Status:</td>
                <td>Present: {present_rec} / Absent: {total_rec - present_rec} (Total: {total_rec})</td>
            </tr>
        </table>

        <table class="data-table">
            <thead>
                <tr>
                    <th>STUDENT NAME</th>
                    <th>SECTION CODE</th>
                    <th>DATE</th>
                    <th>STATUS</th>
                </tr>
            </thead>
            <tbody>
    """
    for r in records:
        sec_code = r["section_id"][:8].upper() if r["section_id"] else "N/A"
        std_name = r["student_name"] or "Student Record"
        status_val = r["status_val"]
        status_style = r["status_style"]
        
        html += f"""
        <tr>
            <td style="font-weight: bold; color: #111827;">{std_name}</td>
            <td>{sec_code}</td>
            <td>{r["date"].isoformat()}</td>
            <td class="{status_style}">{status_val}</td>
        </tr>
        """
        
    html += f"""
            </tbody>
        </table>
        <div class="footer">
            System generated report. Compliance Index verified at {datetime.now().strftime("%Y-%m-%d")}.
        </div>
    </body>
    </html>
    """
    return render_to_pdf(html)

# --- 4. Grade Sheet / Marks Report PDF ---
@router.get("/marks/pdf")
async def generate_marks_report_pdf(
    student_id: str,
    course_id: Optional[str] = None,
    academic_year: Optional[str] = None,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db_session)
):
    student = await db.get(Student, student_id)
    if not student:
        std_q = await db.execute(select(Student).where(Student.user_id == student_id, Student.is_deleted.is_(False)))
        student = std_q.scalar_one_or_none()
    if not student:
        raise HTTPException(status_code=404, detail="Student not found")
        
    usr = await db.get(User, student.user_id)
    if not usr:
        raise HTTPException(status_code=404, detail="Student user not found")

    dept_name = "N/A"
    if student.department_id:
        dept = await db.get(Department, student.department_id)
        if dept:
            dept_name = dept.name

    query = select(InternalMark, Course).join(Course, InternalMark.subject_id == Course.id).where(
        InternalMark.student_id == student.id,
        InternalMark.is_deleted.is_(False)
    )
    if course_id:
        query = query.where(InternalMark.subject_id == course_id)
    if academic_year:
        query = query.where(InternalMark.academic_year == academic_year)
        
    res = await db.execute(query)
    marks_list = res.all()

    html = f"""
    <html>
    <head>
        <style>
            body {{ font-family: Helvetica, Arial, sans-serif; font-size: 10px; color: #374151; padding: 25px; }}
            .header-table {{ width: 100%; border-bottom: 2px solid #10B981; padding-bottom: 12px; margin-bottom: 20px; }}
            .college-title {{ font-size: 16px; font-weight: bold; color: #10B981; }}
            .student-name {{ font-size: 18px; font-weight: bold; color: #111827; }}
            .section-title {{ font-size: 11px; font-weight: bold; color: #10B981; border-bottom: 1px solid #E5E7EB; padding-bottom: 4px; margin-top: 15px; margin-bottom: 8px; text-transform: uppercase; }}
            .details-table {{ width: 100%; margin-bottom: 15px; }}
            .details-table td {{ padding: 4px 6px; vertical-align: top; }}
            .label {{ font-weight: bold; color: #4b5563; width: 35%; }}
            .value {{ color: #1f2937; }}
            .data-table {{ width: 100%; border-collapse: collapse; margin-top: 5px; }}
            .data-table th {{ background-color: #F3F4F6; color: #111827; border: 1px solid #D1D5DB; padding: 5px; font-weight: bold; text-align: left; }}
            .data-table td {{ border: 1px solid #E5E7EB; padding: 5px; }}
            .signatures-table {{ width: 100%; margin-top: 50px; text-align: center; font-size: 9px; }}
            .sig-space {{ border-top: 1px solid #9CA3AF; width: 80%; margin: 0 auto; padding-top: 4px; }}
            .footer {{ text-align: center; margin-top: 40px; font-size: 8px; color: #9CA3AF; border-top: 1px solid #E5E7EB; padding-top: 10px; }}
        </style>
    </head>
    <body>
        <table class="header-table">
            <tr>
                <td>
                    <div class="college-title">KNOWLEDGE COLLEGE OF LAW</div>
                    <div style="font-size: 8px; color: #6B7280;">Official Statement of Internal Assessment Grades</div>
                </td>
                <td style="text-align: right;">
                    <div class="student-name">{usr.full_name}</div>
                    <div style="font-size: 8px; color: #4b5563;">Roll No: {student.roll_no or "N/A"} | Department: {dept_name}</div>
                </td>
            </tr>
        </table>

        <div class="section-title">Academic Details Scope</div>
        <table class="details-table">
            <tr>
                <td class="label">Register Number:</td>
                <td class="value">{student.roll_no or "N/A"}</td>
                <td class="label">Academic Year:</td>
                <td class="value">{academic_year or "2025-2026"}</td>
            </tr>
            <tr>
                <td class="label">Degree Program:</td>
                <td class="value">Integrated BA.LLB (Hons)</td>
                <td class="label">Semester Level:</td>
                <td class="value">Semester {student.semester}</td>
            </tr>
        </table>

        <div class="section-title">Grade Sheet breakdown</div>
        <table class="data-table">
            <thead>
                <tr>
                    <th>SUBJECT CODE</th>
                    <th>SUBJECT NAME</th>
                    <th>EXAM (40)</th>
                    <th>ASG (20)</th>
                    <th>PRES (20)</th>
                    <th>VIVA (20)</th>
                    <th>ATT (10)</th>
                    <th>TOTAL (100)</th>
                </tr>
            </thead>
            <tbody>
    """
    for m, c in marks_list:
        html += f"""
        <tr>
            <td style="font-weight: bold; color: #10B981;">{c.code or "LAW"}</td>
            <td>{c.name}</td>
            <td>{m.internal_exam_mark}</td>
            <td>{m.assignment_mark}</td>
            <td>{m.presentation_mark}</td>
            <td>{m.viva_voice_mark}</td>
            <td>{m.attendance_mark}</td>
            <td style="font-weight: bold;">{m.total_mark}</td>
        </tr>
        """
    if not marks_list:
        html += "<tr><td colspan='8' style='text-align: center; font-style: italic; color: #6B7280;'>No grading details found for active scope parameters.</td></tr>"
        
    html += f"""
            </tbody>
        </table>

        <table class="signatures-table">
            <tr>
                <td style="width: 33%;">
                    <div style="height: 35px;"></div>
                    <div class="sig-space">Faculty Advisor Signature</div>
                </td>
                <td style="width: 33%;">
                    <div style="height: 35px;"></div>
                    <div class="sig-space">Head of Department</div>
                </td>
                <td style="width: 33%;">
                    <div style="height: 35px;"></div>
                    <div class="sig-space">Principal Office Seal</div>
                </td>
            </tr>
        </table>

        <div class="footer">
            This statement of marks is computer generated and carries administrative validation stamp from Registrar.
        </div>
    </body>
    </html>
    """
    return render_to_pdf(html)

# --- 5. Staff Salary Payslip PDF ---
@router.get("/salary/pdf")
async def generate_salary_slip_pdf(
    faculty_id: str,
    year: int,
    month: int,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db_session)
):
    fac_user = await db.get(User, faculty_id)
    if not fac_user:
        raise HTTPException(status_code=404, detail="Faculty user not found")
        
    fac_profile_q = await db.execute(select(FacultyProfile).where(FacultyProfile.user_id == faculty_id, FacultyProfile.is_deleted.is_(False)))
    fac_profile = fac_profile_q.scalar_one_or_none()

    dept_name = "N/A"
    if fac_user.department_id:
        dept = await db.get(Department, fac_user.department_id)
        if dept:
            dept_name = dept.name

    sal_q = await db.execute(
        select(Salary).where(
            Salary.faculty_id == faculty_id,
            Salary.month == month,
            Salary.year == year,
            Salary.is_deleted.is_(False)
        )
    )
    sal = sal_q.scalar_one_or_none()

    # Dynamic draft values if payroll hasn't run yet
    basic_salary = 60000.0
    allowances = 5000.0
    gross = 65000.0
    pf_ded = 1800.0
    leave_ded = 0.0
    net_salary = 63200.0
    designation = "Assistant Professor"
    working_days = 30
    leave_days = 0
    doj_str = "N/A"

    if sal:
        basic_salary = float(sal.basic)
        allowances = float(sal.allowances)
        gross = float(sal.gross)
        pf_ded = float(sal.pf_deduction)
        leave_ded = float(sal.leave_deduction)
        net_salary = float(sal.net_salary)
        designation = sal.designation or designation
        working_days = sal.working_days
        leave_days = sal.leave_days
        if sal.joining_date:
            doj_str = sal.joining_date.strftime("%Y-%m-%d")
    else:
        # Check PF configuration for fallback defaults
        pf_conf_q = await db.execute(select(PFConfiguration).where(PFConfiguration.faculty_id == faculty_id))
        pf_conf = pf_conf_q.scalar_one_or_none()
        if pf_conf:
            basic_salary = float(pf_conf.basic_salary)
            gross = basic_salary
            net_salary = gross
            if pf_conf.joining_date:
                doj_str = pf_conf.joining_date.strftime("%Y-%m-%d")
        if fac_profile:
            designation = fac_profile.designation or designation

    total_deductions = leave_ded

    # Render Template
    month_name = date(year, month, 1).strftime("%B %Y").upper()
    html = f"""
    <html>
    <head>
        <style>
            body {{ font-family: Helvetica, Arial, sans-serif; font-size: 10px; color: #374151; padding: 25px; }}
            .header-table {{ width: 100%; border-bottom: 2px solid #4F46E5; padding-bottom: 12px; margin-bottom: 20px; }}
            .college-title {{ font-size: 16px; font-weight: bold; color: #4F46E5; }}
            .slip-title {{ font-size: 14px; font-weight: bold; color: #111827; }}
            .section-title {{ font-size: 11px; font-weight: bold; color: #4F46E5; border-bottom: 1px solid #E5E7EB; padding-bottom: 4px; margin-top: 15px; margin-bottom: 8px; text-transform: uppercase; }}
            .details-table {{ width: 100%; margin-bottom: 15px; }}
            .details-table td {{ padding: 4px 6px; vertical-align: top; }}
            .label {{ font-weight: bold; color: #4b5563; width: 35%; }}
            .value {{ color: #1f2937; }}
            .data-table {{ width: 100%; border-collapse: collapse; margin-top: 5px; }}
            .data-table th {{ background-color: #F3F4F6; color: #111827; border: 1px solid #D1D5DB; padding: 6px; font-weight: bold; text-align: left; }}
            .data-table td {{ border: 1px solid #E5E7EB; padding: 6px; }}
            .signatures-table {{ width: 100%; margin-top: 50px; text-align: center; font-size: 9px; }}
            .sig-space {{ border-top: 1px solid #9CA3AF; width: 80%; margin: 0 auto; padding-top: 4px; }}
            .footer {{ text-align: center; margin-top: 40px; font-size: 8px; color: #9CA3AF; border-top: 1px solid #E5E7EB; padding-top: 10px; }}
        </style>
    </head>
    <body>
        <table class="header-table">
            <tr>
                <td>
                    <div class="college-title">KNOWLEDGE COLLEGE OF LAW</div>
                    <div style="font-size: 8px; color: #6B7280;">Affiliated to State Law University · Approved by BCI</div>
                </td>
                <td style="text-align: right;">
                    <div class="slip-title">SALARY PAY SLIP</div>
                    <div style="font-size: 9px; font-weight: bold; color: #4b5563;">FOR THE MONTH OF {month_name}</div>
                </td>
            </tr>
        </table>

        <div class="section-title">Employee Details Summary</div>
        <table class="details-table">
            <tr>
                <td class="label">Employee Name:</td>
                <td class="value">{fac_user.full_name}</td>
                <td class="label">Associated Department:</td>
                <td class="value">{dept_name}</td>
            </tr>
            <tr>
                <td class="label">Staff ID:</td>
                <td class="value">{fac_user.id[:8].upper()}</td>
                <td class="label">Designation:</td>
                <td class="value">{designation}</td>
            </tr>
            <tr>
                <td class="label">Date of Joining:</td>
                <td class="value">{doj_str}</td>
                <td class="label">Days Mapped / Leave:</td>
                <td class="value">{working_days} Days Worked / {leave_days} Days LOP</td>
            </tr>
        </table>

        <div class="section-title">Salary Breakdown Ledger</div>
        <table class="data-table">
            <thead>
                <tr>
                    <th style="width: 50%;">EARNINGS DESCRIPTION</th>
                    <th style="width: 50%;">DEDUCTIONS DESCRIPTION</th>
                </tr>
            </thead>
            <tbody>
                <tr>
                    <td>
                        <table style="width: 100%;">
                            <tr><td>Basic Salary:</td><td style="text-align: right; font-weight: bold;">INR {basic_salary:,.2f}</td></tr>
                            <tr><td>Special Allowances:</td><td style="text-align: right; font-weight: bold;">INR {allowances:,.2f}</td></tr>
                            <tr style="height: 40px;"><td></td><td></td></tr>
                        </table>
                    </td>
                    <td>
                        <table style="width: 100%;">
                            <tr><td>LOP Deduction:</td><td style="text-align: right; font-weight: bold;">INR {leave_ded:,.2f}</td></tr>
                            <tr style="height: 60px;"><td></td><td></td></tr>
                        </table>
                    </td>
                </tr>
                <tr style="background-color: #F9FAF5; font-weight: bold; font-size: 11px;">
                    <td>
                        <table style="width: 100%;">
                            <tr><td>GROSS PAY:</td><td style="text-align: right; color: #4F46E5;">INR {gross:,.2f}</td></tr>
                        </table>
                    </td>
                    <td>
                        <table style="width: 100%;">
                            <tr><td>TOTAL DEDUCTION:</td><td style="text-align: right; color: #EF4444;">INR {total_deductions:,.2f}</td></tr>
                        </table>
                    </td>
                </tr>
            </tbody>
        </table>

        <div style="background-color: #EEF2FF; border: 1px solid #C7D2FE; border-radius: 6px; padding: 12px; margin-top: 15px; text-align: center;">
            <div style="font-size: 11px; font-weight: bold; color: #4F46E5;">NET PAYABLE REMUNERATION:</div>
            <div style="font-size: 20px; font-weight: 900; color: #1E1B4B; margin-top: 5px;">INR {net_salary:,.2f}</div>
        </div>

        <table class="signatures-table">
            <tr>
                <td style="width: 50%;">
                    <div style="height: 40px;"></div>
                    <div class="sig-space">Account Officer Signature</div>
                </td>
                <td style="width: 50%;">
                    <div style="height: 40px;"></div>
                    <div class="sig-space">Principal Seal & Stamp</div>
                </td>
            </tr>
        </table>

        <div class="footer">
            Knowledge College of Law. System Payslip generation token: {fac_user.id[:4].upper()}-{year}-{month:02d}
        </div>
    </body>
    </html>
    """
    return render_to_pdf(html)

# --- 6. Student Fee Receipt / Statement PDF ---
@router.get("/fees/pdf")
async def generate_fees_receipt_pdf(
    student_id: Optional[str] = None,
    record_id: Optional[str] = None,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db_session)
):
    if record_id:
        record_res = await db.execute(select(FeeRecord).where(FeeRecord.id == record_id, FeeRecord.is_deleted.is_(False)))
        record = record_res.scalar_one_or_none()
        if not record:
            raise HTTPException(status_code=404, detail="Fee record not found")
        student_id = record.student_id
    else:
        if not student_id:
            raise HTTPException(status_code=400, detail="Either record_id or student_id is required")
            
    student = await db.get(Student, student_id)
    if not student:
        std_q = await db.execute(select(Student).where(Student.user_id == student_id, Student.is_deleted.is_(False)))
        student = std_q.scalar_one_or_none()
    if not student:
        raise HTTPException(status_code=404, detail="Student profile not found")
    
    # Ensure student_id is the resolved Student record ID for subsequent queries
    student_id = student.id
        
    usr = await db.get(User, student.user_id)
    if not usr:
        raise HTTPException(status_code=404, detail="Student user record not found")

    dept_name = "N/A"
    if student.department_id:
        dept = await db.get(Department, student.department_id)
        if dept:
            dept_name = dept.name

    from app.services.fee_service import FeeService
    fee_service = FeeService(db)
    fee_summary = await fee_service.get_student_fee_summary(student_id)

    total_fee = fee_summary["total_fees"]
    scholarship_deduction = fee_summary["scholarship_deduction"]
    other_deductions = fee_summary["other_deductions"]
    net_fee = fee_summary["net_fees"]
    paid_fee = fee_summary["amount_paid"]
    due_fee = fee_summary["pending_balance"]

    html = f"""
    <html>
    <head>
        <style>
            body {{ font-family: Helvetica, Arial, sans-serif; font-size: 10px; color: #374151; padding: 25px; }}
            .header-table {{ width: 100%; border-bottom: 2px solid #F59E0B; padding-bottom: 12px; margin-bottom: 20px; }}
            .college-title {{ font-size: 16px; font-weight: bold; color: #F59E0B; }}
            .receipt-title {{ font-size: 18px; font-weight: bold; color: #111827; }}
            .section-title {{ font-size: 11px; font-weight: bold; color: #F59E0B; border-bottom: 1px solid #E5E7EB; padding-bottom: 4px; margin-top: 15px; margin-bottom: 8px; text-transform: uppercase; }}
            .details-table {{ width: 100%; margin-bottom: 15px; }}
            .details-table td {{ padding: 4px 6px; vertical-align: top; }}
            .label {{ font-weight: bold; color: #4b5563; width: 35%; }}
            .value {{ color: #1f2937; }}
            .data-table {{ width: 100%; border-collapse: collapse; margin-top: 5px; }}
            .data-table th {{ background-color: #F3F4F6; color: #111827; border: 1px solid #D1D5DB; padding: 6px; font-weight: bold; text-align: left; }}
            .data-table td {{ border: 1px solid #E5E7EB; padding: 6px; }}
            .paid {{ color: #10B981; font-weight: bold; }}
            .unpaid {{ color: #EF4444; font-weight: bold; }}
            .footer {{ text-align: center; margin-top: 40px; font-size: 8px; color: #9CA3AF; border-top: 1px solid #E5E7EB; padding-top: 10px; }}
        </style>
    </head>
    <body>
        <table class="header-table">
            <tr>
                <td>
                    <div class="college-title">KNOWLEDGE COLLEGE OF LAW</div>
                    <div style="font-size: 8px; color: #6B7280;">Student Fees Receipt & Ledger Account Statement</div>
                </td>
                <td style="text-align: right;">
                    <div class="receipt-title">{usr.full_name}</div>
                    <div style="font-size: 8px; color: #4b5563;">Roll No: {student.roll_no or "N/A"} | Program: Integrated BA.LLB (Hons)</div>
                </td>
            </tr>
        </table>

        <div class="section-title">General Student Details</div>
        <table class="details-table">
            <tr>
                <td class="label">Student Name:</td>
                <td class="value">{usr.full_name}</td>
                <td class="label">Course Stream:</td>
                <td class="value">{dept_name}</td>
            </tr>
            <tr>
                <td class="label">Register Number:</td>
                <td class="value">{student.roll_no or "N/A"}</td>
                <td class="label">Academic Semester:</td>
                <td class="value">Semester {student.semester}</td>
            </tr>
        </table>

        <div class="section-title">Fees Ledger Details</div>
        <table class="data-table">
            <thead>
                <tr>
                    <th>FEE HEAD DESCRIPTION</th>
                    <th>TERM DURATION</th>
                    <th>DUE DATE</th>
                    <th>AMOUNT</th>
                    <th>PAYMENT STATUS</th>
                </tr>
            </thead>
            <tbody>
    """
    
    for r in fee_summary["records"]:
        status_val = r["status"].upper()
        status_style = "paid" if status_val == "PAID" else "unpaid"
            
        html += f"""
        <tr>
            <td style="font-weight: bold; color: #111827;">{r["fee_type"]}</td>
            <td>Semester {student.semester}</td>
            <td>{r["due_date"].isoformat() if r["due_date"] else 'N/A'}</td>
            <td style="font-weight: bold;">INR {float(r["amount"]):,.2f}</td>
            <td class="{status_style}">{status_val}</td>
        </tr>
        """
        
    html += f"""
            </tbody>
        </table>

        <table class="data-table" style="margin-top: 15px;">
            <tr style="background-color: #FEF3C7; font-weight: bold; font-size: 8px; text-align: center;">
                <td>GROSS FEES:<br/>INR {total_fee:,.2f}</td>
                <td style="color: #4F46E5;">SCHOLARSHIPS:<br/>INR {scholarship_deduction:,.2f}</td>
                <td style="color: #8B5CF6;">DEDUCTIONS:<br/>INR {other_deductions:,.2f}</td>
                <td style="color: #1E1B4B;">NET FEES:<br/>INR {net_fee:,.2f}</td>
                <td style="color: #10B981;">TOTAL PAID:<br/>INR {paid_fee:,.2f}</td>
                <td style="color: #EF4444;">BALANCE DUE:<br/>INR {due_fee:,.2f}</td>
            </tr>
        </table>

        <div class="footer">
            CAMS Student Fee Registry System. Seal of Administrative Officer. Verification MD5: {student.id[:8].upper()}
        </div>
    </body>
    </html>
    """
    return render_to_pdf(html)
