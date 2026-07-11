from datetime import date
from sqlalchemy import select, update
from sqlalchemy.ext.asyncio import AsyncSession
from app.db.models.academic import Department, Course, Section, Timetable, TimetableApproval, Exam, ExamSetting, Weekday
from app.db.models.attendance import Attendance
from app.db.models.marks import Mark

class AcademicRepository:
    def __init__(self, db: AsyncSession) -> None:
        self.db = db

    async def get_departments(self) -> list[Department]:
        result = await self.db.execute(select(Department).where(Department.is_deleted.is_(False)))
        return list(result.scalars().all())

    async def get_courses_by_dept(self, dept_id: str) -> list[Course]:
        result = await self.db.execute(
            select(Course).where(Course.dept_id == dept_id, Course.is_deleted.is_(False))
        )
        return list(result.scalars().all())

    async def get_sections_by_course(self, course_id: str) -> list[Section]:
        result = await self.db.execute(
            select(Section).where(Section.course_id == course_id, Section.is_deleted.is_(False))
        )
        return list(result.scalars().all())

    async def get_timetable_by_section(self, section_id: str) -> list[Timetable]:
        result = await self.db.execute(
            select(Timetable).where(Timetable.section_id == section_id, Timetable.is_deleted.is_(False))
        )
        return list(result.scalars().all())

    async def get_timetable_by_faculty(self, faculty_id: str) -> list[Timetable]:
        result = await self.db.execute(
            select(Timetable)
            .where(
                Timetable.faculty_id == faculty_id,
                Timetable.is_deleted.is_(False)
            )
        )
        return list(result.scalars().all())

    async def get_timetable_approvals(self) -> list[TimetableApproval]:
        result = await self.db.execute(
            select(TimetableApproval).where(TimetableApproval.is_deleted.is_(False))
        )
        return list(result.scalars().all())

    async def approve_timetable(self, timetable_id: str, approved_by: str, status: str, comments: str | None = None) -> TimetableApproval:
        q = select(TimetableApproval).where(TimetableApproval.timetable_id == timetable_id, TimetableApproval.is_deleted.is_(False))
        res = await self.db.execute(q)
        approval = res.scalar_one_or_none()
        if approval:
            approval.status = status
            approval.approved_by = approved_by
            approval.comments = comments
        else:
            approval = TimetableApproval(
                timetable_id=timetable_id,
                status=status,
                approved_by=approved_by,
                comments=comments
            )
            self.db.add(approval)
        await self.db.flush()
        return approval

    async def get_attendance_by_student(self, student_id: str) -> list[Attendance]:
        # Helper to query all sessions in student's section
        from app.db.models.student import Student
        from app.db.models.academic import Section, Course
        
        student_stmt = select(Student).where(Student.id == student_id)
        student_res = await self.db.execute(student_stmt)
        student = student_res.scalar_one_or_none()
        if not student:
            return []
            
        section_name = "A"
        if student.section_id:
            sec_stmt = select(Section.section_name).where(Section.id == student.section_id)
            sec_res = await self.db.execute(sec_stmt)
            section_name = sec_res.scalar_one() or "A"
            
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
        section_ids_res = await self.db.execute(section_ids_stmt)
        student_section_ids = [r[0] for r in section_ids_res.all()]
        
        if not student_section_ids:
            return []
            
        result = await self.db.execute(
            select(Attendance).where(
                Attendance.section_id.in_(student_section_ids),
                Attendance.is_deleted.is_(False)
            )
        )
        return list(result.scalars().all())

    async def get_attendance_with_details_by_student(self, student_id: str) -> list[dict]:
        from app.db.models.student import Student
        from app.db.models.academic import Section, Course
        from app.db.models.attendance import AttendanceStatus
        
        student_stmt = select(Student).where(Student.id == student_id)
        student_res = await self.db.execute(student_stmt)
        student = student_res.scalar_one_or_none()
        if not student:
            return []
            
        section_name = "A"
        if student.section_id:
            sec_stmt = select(Section.section_name).where(Section.id == student.section_id)
            sec_res = await self.db.execute(sec_stmt)
            section_name = sec_res.scalar_one() or "A"
            
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
        section_ids_res = await self.db.execute(section_ids_stmt)
        student_section_ids = [r[0] for r in section_ids_res.all()]
        
        if not student_section_ids:
            return []

        stmt = (
            select(Attendance, Section, Course)
            .join(Section, Attendance.section_id == Section.id)
            .join(Course, Section.course_id == Course.id)
            .where(
                Attendance.section_id.in_(student_section_ids),
                Attendance.is_deleted.is_(False)
            )
            .order_by(Attendance.date.desc())
        )
        result = await self.db.execute(stmt)
        rows = result.all()
        
        records_list = []
        for att, sec, course in rows:
            # Resolve status
            status = AttendanceStatus.PRESENT
            if att.absentee_ids and student_id in att.absentee_ids:
                status = AttendanceStatus.ABSENT
            elif att.od_ids and student_id in att.od_ids:
                status = AttendanceStatus.OD
                
            records_list.append({
                "id": att.id,
                "date": att.date,
                "status": status,
                "subject_name": course.name,
                "subject_code": course.code,
                "section_name": sec.section_name
            })
        return records_list

    async def mark_attendance(self, student_id: str, section_id: str, date_val: date, status: str, subject_id: str | None = None, faculty_id: str | None = None, hour: str = "Hour 1") -> Attendance:
        from app.db.models.user import User, UserRole
        from app.db.models.academic import Section, Course
        
        if not subject_id or not faculty_id:
            sec_q = await self.db.execute(select(Section).where(Section.id == section_id))
            sec = sec_q.scalar_one_or_none()
            if sec:
                subject_id = subject_id or sec.course_id
                faculty_id = faculty_id or sec.faculty_id
                
        if not subject_id:
            c_q = await self.db.execute(select(Course.id))
            subject_id = c_q.scalars().first() or "dummy-subject"
            
        if not faculty_id:
            f_q = await self.db.execute(select(User.id).where(User.role == UserRole.FACULTY))
            faculty_id = f_q.scalars().first() or "dummy-faculty"

        stmt = select(Attendance).where(
            Attendance.section_id == section_id,
            Attendance.subject_id == subject_id,
            Attendance.date == date_val,
            Attendance.hour == hour,
            Attendance.is_deleted.is_(False)
        )
        res = await self.db.execute(stmt)
        att = res.scalar_one_or_none()

        if not att:
            att = Attendance(
                section_id=section_id,
                subject_id=subject_id,
                faculty_id=faculty_id,
                date=date_val,
                hour=hour,
                absentee_ids=[],
                od_ids=[]
            )
            self.db.add(att)

        absentees = list(att.absentee_ids or [])
        ods = list(att.od_ids or [])
        
        if student_id in absentees:
            absentees.remove(student_id)
        if student_id in ods:
            ods.remove(student_id)

        if status.lower() == "absent":
            absentees.append(student_id)
        elif status.lower() == "od":
            ods.append(student_id)

        att.absentee_ids = absentees
        att.od_ids = ods

        await self.db.flush()
        return att

    async def get_marks_by_student(self, student_id: str) -> list[Mark]:
        result = await self.db.execute(
            select(Mark).where(Mark.student_id == student_id, Mark.is_deleted.is_(False))
        )
        return list(result.scalars().all())

    async def add_mark(self, student_id: str, section_id: str, exam_type: str, mark_val: float, max_mark: float) -> Mark:
        m = Mark(student_id=student_id, section_id=section_id, exam_type=exam_type, mark=mark_val, max_mark=max_mark)
        self.db.add(m)
        await self.db.flush()
        return m

    async def get_exams(self) -> list[Exam]:
        result = await self.db.execute(select(Exam).where(Exam.is_deleted.is_(False)))
        return list(result.scalars().all())

    async def get_exam_settings(self, exam_id: str) -> ExamSetting | None:
        result = await self.db.execute(
            select(ExamSetting).where(ExamSetting.exam_id == exam_id, ExamSetting.is_deleted.is_(False))
        )
        return result.scalar_one_or_none()
