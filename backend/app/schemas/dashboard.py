from pydantic import BaseModel


class MetricSchema(BaseModel):
    id: str
    label: str
    value: str


class DepartmentStrengthSchema(BaseModel):
    name: str
    code: str
    count: int


class DashboardResponse(BaseModel):
    metrics: list[MetricSchema]
    total_users: int | None = None
    total_students: int | None = None
    total_staff: int | None = None
    total_departments: int | None = None
    ug_departments: int | None = None
    pg_departments: int | None = None
    daily_fee_collection: float | None = None
    dept_strengths: list[DepartmentStrengthSchema] | None = None
    total_degree_templates: int | None = None
    total_degree_cohorts: int | None = None
    total_course_templates: int | None = None
    active_semesters_count: int | None = None
    verified_students_count: int | None = None
    pending_students_count: int | None = None
