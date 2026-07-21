-- CAMS Enterprise - database schema reference
--
-- GENERATED from this project's own Alembic migrations (backend/alembic/).
-- Not copied from any other project.
--
-- The AUTHORITATIVE schema is the migration chain in backend/alembic/versions/.
-- Do not apply this file to a database - run `alembic upgrade head` instead.
-- This exists so a reviewer can read the data model without running anything.
--
-- Regenerate:  cd backend && alembic upgrade head   (then dump the database)
--
-- Tables: 99   Indexes: 28
-- Generated: 2026-07-21


-- academic_years ----------------------------------------------
CREATE TABLE "academic_years" (
	name VARCHAR(128) NOT NULL, 
	start_date DATE NOT NULL, 
	end_date DATE NOT NULL, 
	regulation_id VARCHAR(36), 
	batch VARCHAR(128) NOT NULL, 
	current_semester INTEGER NOT NULL, 
	is_semester_open BOOLEAN NOT NULL, 
	is_exam_period BOOLEAN NOT NULL, 
	is_active BOOLEAN NOT NULL, 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME NOT NULL, 
	updated_at DATETIME NOT NULL, 
	is_deleted BOOLEAN NOT NULL, 
	deleted_at DATETIME, 
	degree_id VARCHAR(36), 
	CONSTRAINT pk_academic_years PRIMARY KEY (id), 
	CONSTRAINT fk_academic_years_regulation_id_regulations FOREIGN KEY(regulation_id) REFERENCES regulations (id)
);

-- activity_logs -----------------------------------------------
CREATE TABLE activity_logs (
	user_id VARCHAR(36), 
	action VARCHAR(255) NOT NULL, 
	action_metadata VARCHAR(4000), 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME NOT NULL, 
	updated_at DATETIME NOT NULL, 
	is_deleted BOOLEAN NOT NULL, 
	deleted_at DATETIME, 
	CONSTRAINT pk_activity_logs PRIMARY KEY (id), 
	CONSTRAINT fk_activity_logs_user_id_users FOREIGN KEY(user_id) REFERENCES users (id)
);

-- activity_point_categories -----------------------------------
CREATE TABLE activity_point_categories (
	code VARCHAR(64) NOT NULL, 
	name VARCHAR(255) NOT NULL, 
	max_points NUMERIC(6, 2) NOT NULL, 
	description VARCHAR(1024), 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME DEFAULT (CURRENT_TIMESTAMP) NOT NULL, 
	updated_at DATETIME DEFAULT (CURRENT_TIMESTAMP) NOT NULL, 
	is_deleted BOOLEAN DEFAULT 'false' NOT NULL, 
	deleted_at DATETIME, 
	CONSTRAINT pk_activity_point_categories PRIMARY KEY (id), 
	CONSTRAINT uq_activity_point_categories_code UNIQUE (code)
);

-- alembic_version ---------------------------------------------
CREATE TABLE alembic_version (
	version_num VARCHAR(32) NOT NULL, 
	CONSTRAINT alembic_version_pkc PRIMARY KEY (version_num)
);

-- assignment_submissions --------------------------------------
CREATE TABLE assignment_submissions (
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME DEFAULT (CURRENT_TIMESTAMP) NOT NULL, 
	updated_at DATETIME DEFAULT (CURRENT_TIMESTAMP) NOT NULL, 
	is_deleted BOOLEAN DEFAULT 'false' NOT NULL, 
	deleted_at DATETIME, 
	assignment_id VARCHAR(36) NOT NULL, 
	student_id VARCHAR(36) NOT NULL, 
	submitted_file_url VARCHAR(512), 
	submitted_text VARCHAR(4000), 
	marks_obtained FLOAT, 
	grade VARCHAR(16), 
	feedback VARCHAR(2048), 
	remarks VARCHAR(2048), 
	status VARCHAR(32) DEFAULT 'Submitted' NOT NULL, 
	submitted_at DATETIME DEFAULT (CURRENT_TIMESTAMP) NOT NULL, 
	CONSTRAINT pk_assignment_submissions PRIMARY KEY (id), 
	CONSTRAINT fk_assignment_submissions_assignment_id_assignments FOREIGN KEY(assignment_id) REFERENCES assignments (id), 
	CONSTRAINT fk_assignment_submissions_student_id_students FOREIGN KEY(student_id) REFERENCES students (id)
);

-- assignments -------------------------------------------------
CREATE TABLE assignments (
	section_id VARCHAR(36) NOT NULL, 
	faculty_id VARCHAR(36) NOT NULL, 
	title VARCHAR(255) NOT NULL, 
	deadline DATE NOT NULL, 
	submission_count INTEGER NOT NULL, 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME NOT NULL, 
	updated_at DATETIME NOT NULL, 
	is_deleted BOOLEAN NOT NULL, 
	deleted_at DATETIME, type VARCHAR(64), subject VARCHAR(255), unit VARCHAR(128), topic VARCHAR(255), description VARCHAR(4000), instructions VARCHAR(4000), total_marks INTEGER, semester VARCHAR(32), section VARCHAR(64), attachments VARCHAR(4000), status VARCHAR(32) DEFAULT 'Draft' NOT NULL, 
	CONSTRAINT pk_assignments PRIMARY KEY (id), 
	CONSTRAINT fk_assignments_faculty_id_users FOREIGN KEY(faculty_id) REFERENCES users (id), 
	CONSTRAINT fk_assignments_section_id_sections FOREIGN KEY(section_id) REFERENCES sections (id)
);

-- attendance --------------------------------------------------
CREATE TABLE "attendance" (
	student_id VARCHAR(36), 
	section_id VARCHAR(36) NOT NULL, 
	date DATE NOT NULL, 
	status VARCHAR(7), 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME NOT NULL, 
	updated_at DATETIME NOT NULL, 
	is_deleted BOOLEAN NOT NULL, 
	deleted_at DATETIME, 
	subject_id VARCHAR(36), 
	faculty_id VARCHAR(36), 
	hour VARCHAR(32), 
	absentee_ids JSON, 
	od_ids JSON, 
	CONSTRAINT pk_attendance PRIMARY KEY (id), 
	CONSTRAINT fk_attendance_student_id_students FOREIGN KEY(student_id) REFERENCES students (id), 
	CONSTRAINT fk_attendance_section_id_sections FOREIGN KEY(section_id) REFERENCES sections (id)
);

-- attendance_corrections --------------------------------------
CREATE TABLE attendance_corrections (
	student_reg_no VARCHAR(64) NOT NULL, 
	student_name VARCHAR(256) NOT NULL, 
	subject VARCHAR(256) NOT NULL, 
	date DATE NOT NULL, 
	previous_status VARCHAR(64) NOT NULL, 
	updated_status VARCHAR(64) NOT NULL, 
	reason VARCHAR(1024) NOT NULL, 
	status VARCHAR(64) NOT NULL, 
	remarks VARCHAR(1024), 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME NOT NULL, 
	updated_at DATETIME NOT NULL, 
	is_deleted BOOLEAN NOT NULL, 
	deleted_at DATETIME, 
	CONSTRAINT pk_attendance_corrections PRIMARY KEY (id)
);

-- audit_logs --------------------------------------------------
CREATE TABLE audit_logs (
	user_id VARCHAR(36), 
	action VARCHAR(255) NOT NULL, 
	entity VARCHAR(255) NOT NULL, 
	entity_id VARCHAR, 
	ip_address VARCHAR(64), 
	timestamp DATETIME NOT NULL, 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME NOT NULL, 
	updated_at DATETIME NOT NULL, 
	is_deleted BOOLEAN NOT NULL, 
	deleted_at DATETIME, 
	CONSTRAINT pk_audit_logs PRIMARY KEY (id), 
	CONSTRAINT fk_audit_logs_user_id_users FOREIGN KEY(user_id) REFERENCES users (id)
);

-- backup_configurations ---------------------------------------
CREATE TABLE backup_configurations (
	auto_backup_enabled BOOLEAN NOT NULL, 
	schedule_time VARCHAR(5) NOT NULL, 
	retention_count INTEGER NOT NULL, 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME NOT NULL, 
	updated_at DATETIME NOT NULL, 
	is_deleted BOOLEAN NOT NULL, 
	deleted_at DATETIME, 
	CONSTRAINT pk_backup_configurations PRIMARY KEY (id)
);

-- backup_history ----------------------------------------------
CREATE TABLE backup_history (
	filename VARCHAR(255) NOT NULL, 
	filepath VARCHAR(1000) NOT NULL, 
	size_bytes BIGINT NOT NULL, 
	status VARCHAR(50) NOT NULL, 
	trigger_type VARCHAR(50) NOT NULL, 
	created_by VARCHAR(36), 
	error_message VARCHAR(4000), 
	is_incremental BOOLEAN NOT NULL, 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME NOT NULL, 
	updated_at DATETIME NOT NULL, 
	is_deleted BOOLEAN NOT NULL, 
	deleted_at DATETIME, 
	CONSTRAINT pk_backup_history PRIMARY KEY (id), 
	CONSTRAINT fk_backup_history_created_by_users FOREIGN KEY(created_by) REFERENCES users (id)
);

-- budget_expenses ---------------------------------------------
CREATE TABLE budget_expenses (
	line_item_id VARCHAR(36) NOT NULL, 
	description VARCHAR(512) NOT NULL, 
	amount NUMERIC(14, 2) NOT NULL, 
	expense_date VARCHAR(32) NOT NULL, 
	recorded_by VARCHAR(36) NOT NULL, 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, 
	updated_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, 
	is_deleted BOOLEAN DEFAULT 'false' NOT NULL, 
	deleted_at DATETIME, 
	CONSTRAINT pk_budget_expenses PRIMARY KEY (id), 
	CONSTRAINT fk_budget_expenses_line_item_id_budget_line_items FOREIGN KEY(line_item_id) REFERENCES budget_line_items (id), 
	CONSTRAINT fk_budget_expenses_recorded_by_users FOREIGN KEY(recorded_by) REFERENCES users (id)
);

-- budget_line_items -------------------------------------------
CREATE TABLE budget_line_items (
	fiscal_year VARCHAR(16) NOT NULL, 
	title VARCHAR(255) NOT NULL, 
	category VARCHAR(64) DEFAULT 'General' NOT NULL, 
	department_id VARCHAR(36), 
	allocated_amount NUMERIC(14, 2) DEFAULT '0' NOT NULL, 
	spent_amount NUMERIC(14, 2) DEFAULT '0' NOT NULL, 
	status VARCHAR(16) DEFAULT 'ACTIVE' NOT NULL, 
	notes VARCHAR(1024), 
	created_by VARCHAR(36) NOT NULL, 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, 
	updated_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, 
	is_deleted BOOLEAN DEFAULT 'false' NOT NULL, 
	deleted_at DATETIME, 
	CONSTRAINT pk_budget_line_items PRIMARY KEY (id), 
	CONSTRAINT fk_budget_line_items_department_id_departments FOREIGN KEY(department_id) REFERENCES departments (id), 
	CONSTRAINT fk_budget_line_items_created_by_users FOREIGN KEY(created_by) REFERENCES users (id)
);

-- certifications ----------------------------------------------
CREATE TABLE certifications (
	student_id VARCHAR(36) NOT NULL, 
	title VARCHAR(255) NOT NULL, 
	issuer VARCHAR(255) NOT NULL, 
	date VARCHAR(32) NOT NULL, 
	category VARCHAR(64) NOT NULL, 
	type VARCHAR(32) DEFAULT 'training' NOT NULL, 
	is_verified BOOLEAN DEFAULT '0' NOT NULL, 
	file_url VARCHAR(512), 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME NOT NULL, 
	updated_at DATETIME NOT NULL, 
	is_deleted BOOLEAN NOT NULL, 
	deleted_at DATETIME, 
	CONSTRAINT pk_certifications PRIMARY KEY (id), 
	CONSTRAINT fk_certifications_student_id_students FOREIGN KEY(student_id) REFERENCES students (id)
);

-- chat_messages -----------------------------------------------
CREATE TABLE chat_messages (
	session_id VARCHAR(36) NOT NULL, 
	role VARCHAR(6) NOT NULL, 
	content TEXT NOT NULL, 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME DEFAULT (CURRENT_TIMESTAMP) NOT NULL, 
	updated_at DATETIME DEFAULT (CURRENT_TIMESTAMP) NOT NULL, 
	deleted_at DATETIME, is_deleted BOOLEAN DEFAULT false, 
	CONSTRAINT pk_chat_messages PRIMARY KEY (id), 
	CONSTRAINT fk_chat_messages_session_id_chat_sessions FOREIGN KEY(session_id) REFERENCES chat_sessions (id)
);

-- chat_sessions -----------------------------------------------
CREATE TABLE chat_sessions (
	user_id VARCHAR(36) NOT NULL, 
	title VARCHAR(255), 
	is_active BOOLEAN NOT NULL, 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME DEFAULT (CURRENT_TIMESTAMP) NOT NULL, 
	updated_at DATETIME DEFAULT (CURRENT_TIMESTAMP) NOT NULL, 
	deleted_at DATETIME, is_deleted BOOLEAN DEFAULT false, 
	CONSTRAINT pk_chat_sessions PRIMARY KEY (id), 
	CONSTRAINT fk_chat_sessions_user_id_users FOREIGN KEY(user_id) REFERENCES users (id)
);

-- class_advisors ----------------------------------------------
CREATE TABLE class_advisors (
	academic_year_id VARCHAR(36) NOT NULL, 
	faculty_id VARCHAR(36) NOT NULL, 
	department_id VARCHAR(36) NOT NULL, 
	batch VARCHAR(128) NOT NULL, 
	section_name VARCHAR(32) NOT NULL, 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME DEFAULT (CURRENT_TIMESTAMP) NOT NULL, 
	updated_at DATETIME DEFAULT (CURRENT_TIMESTAMP) NOT NULL, 
	is_deleted BOOLEAN DEFAULT false NOT NULL, 
	deleted_at DATETIME, 
	CONSTRAINT pk_class_advisors PRIMARY KEY (id), 
	CONSTRAINT fk_class_advisors_academic_year_id_academic_years FOREIGN KEY(academic_year_id) REFERENCES academic_years (id), 
	CONSTRAINT fk_class_advisors_department_id_departments FOREIGN KEY(department_id) REFERENCES departments (id), 
	CONSTRAINT fk_class_advisors_faculty_id_users FOREIGN KEY(faculty_id) REFERENCES users (id)
);

-- class_diaries -----------------------------------------------
CREATE TABLE class_diaries (
	faculty_id VARCHAR(36) NOT NULL, 
	date VARCHAR(16) NOT NULL, 
	subject VARCHAR(255) NOT NULL, 
	course VARCHAR(255), 
	semester VARCHAR(32), 
	section VARCHAR(64), 
	hour VARCHAR(32), 
	year VARCHAR(16), 
	unit VARCHAR(255), 
	topic VARCHAR(1024), 
	subtopic VARCHAR(1024), 
	teaching_method VARCHAR(255), 
	learning_outcome VARCHAR(2048), 
	class_activity VARCHAR(2048), 
	remarks VARCHAR(2048), 
	status VARCHAR(32) DEFAULT 'Draft' NOT NULL, 
	deviation_reason VARCHAR(1024), 
	revised_date VARCHAR(16), 
	attachment_url VARCHAR(512), 
	attachment_name VARCHAR(255), 
	completion_status VARCHAR(32) DEFAULT 'Completed', 
	json_entry_id VARCHAR(64), 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME DEFAULT (CURRENT_TIMESTAMP) NOT NULL, 
	updated_at DATETIME DEFAULT (CURRENT_TIMESTAMP) NOT NULL, 
	is_deleted BOOLEAN DEFAULT 'false' NOT NULL, 
	deleted_at DATETIME, 
	CONSTRAINT pk_class_diaries PRIMARY KEY (id), 
	CONSTRAINT fk_class_diaries_faculty_id_users FOREIGN KEY(faculty_id) REFERENCES users (id), 
	CONSTRAINT uq_class_diaries_json_entry_id UNIQUE (json_entry_id)
);

-- classroom_activities ----------------------------------------
CREATE TABLE classroom_activities (
	faculty_id VARCHAR(36) NOT NULL, 
	section_id VARCHAR(36) NOT NULL, 
	activity_type VARCHAR(64) NOT NULL, 
	topic VARCHAR(255) NOT NULL, 
	duration_minutes INTEGER NOT NULL, 
	remarks TEXT, 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME NOT NULL, 
	updated_at DATETIME NOT NULL, 
	is_deleted BOOLEAN NOT NULL, 
	deleted_at DATETIME, 
	CONSTRAINT pk_classroom_activities PRIMARY KEY (id), 
	CONSTRAINT fk_classroom_activities_faculty_id_users FOREIGN KEY(faculty_id) REFERENCES users (id), 
	CONSTRAINT fk_classroom_activities_section_id_sections FOREIGN KEY(section_id) REFERENCES sections (id)
);

-- club_announcements ------------------------------------------
CREATE TABLE club_announcements (
	club_id VARCHAR(36) NOT NULL, 
	posted_by VARCHAR(36) NOT NULL, 
	title VARCHAR(255) NOT NULL, 
	is_urgent BOOLEAN DEFAULT '0' NOT NULL, 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME NOT NULL, 
	updated_at DATETIME NOT NULL, 
	is_deleted BOOLEAN NOT NULL, 
	deleted_at DATETIME, 
	CONSTRAINT pk_club_announcements PRIMARY KEY (id), 
	CONSTRAINT fk_club_announcements_club_id_clubs FOREIGN KEY(club_id) REFERENCES clubs (id), 
	CONSTRAINT fk_club_announcements_posted_by_users FOREIGN KEY(posted_by) REFERENCES users (id)
);

-- club_memberships --------------------------------------------
CREATE TABLE club_memberships (
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME DEFAULT (CURRENT_TIMESTAMP) NOT NULL, 
	updated_at DATETIME DEFAULT (CURRENT_TIMESTAMP) NOT NULL, 
	is_deleted BOOLEAN DEFAULT 'false' NOT NULL, 
	deleted_at DATETIME, 
	club_id VARCHAR(36) NOT NULL, 
	user_id VARCHAR(36) NOT NULL, 
	role VARCHAR(32) DEFAULT 'Member' NOT NULL, 
	CONSTRAINT pk_club_memberships PRIMARY KEY (id), 
	CONSTRAINT uq_club_membership_club_user UNIQUE (club_id, user_id), 
	CONSTRAINT fk_club_memberships_club_id_clubs FOREIGN KEY(club_id) REFERENCES clubs (id), 
	CONSTRAINT fk_club_memberships_user_id_users FOREIGN KEY(user_id) REFERENCES users (id)
);

-- clubs -------------------------------------------------------
CREATE TABLE clubs (
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME DEFAULT (CURRENT_TIMESTAMP) NOT NULL, 
	updated_at DATETIME DEFAULT (CURRENT_TIMESTAMP) NOT NULL, 
	is_deleted BOOLEAN DEFAULT 'false' NOT NULL, 
	deleted_at DATETIME, 
	name VARCHAR(255) NOT NULL, 
	description VARCHAR(2048), 
	category VARCHAR(128), 
	member_count INTEGER DEFAULT '0' NOT NULL, 
	CONSTRAINT pk_clubs PRIMARY KEY (id)
);

-- courses -----------------------------------------------------
CREATE TABLE courses (
	dept_id VARCHAR(36), 
	regulation_id VARCHAR(36), 
	code VARCHAR(32) NOT NULL, 
	name VARCHAR(255) NOT NULL, 
	credits INTEGER NOT NULL, 
	semester INTEGER NOT NULL, 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME NOT NULL, 
	updated_at DATETIME NOT NULL, 
	is_deleted BOOLEAN NOT NULL, 
	deleted_at DATETIME, degree_id VARCHAR(36), 
	CONSTRAINT pk_courses PRIMARY KEY (id), 
	CONSTRAINT fk_courses_dept_id_departments FOREIGN KEY(dept_id) REFERENCES departments (id), 
	CONSTRAINT fk_courses_regulation_id_regulations FOREIGN KEY(regulation_id) REFERENCES regulations (id), 
	CONSTRAINT uq_courses_code UNIQUE (code)
);

-- deductions --------------------------------------------------
CREATE TABLE deductions (
	salary_id VARCHAR(36) NOT NULL, 
	type VARCHAR(3) NOT NULL, 
	days INTEGER NOT NULL, 
	amount NUMERIC(12, 2) NOT NULL, 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME NOT NULL, 
	updated_at DATETIME NOT NULL, 
	is_deleted BOOLEAN NOT NULL, 
	deleted_at DATETIME, 
	CONSTRAINT pk_deductions PRIMARY KEY (id), 
	CONSTRAINT fk_deductions_salary_id_salary FOREIGN KEY(salary_id) REFERENCES salary (id)
);

-- degrees -----------------------------------------------------
CREATE TABLE degrees (
	code VARCHAR(32) NOT NULL, 
	name VARCHAR(255) NOT NULL, 
	applicable_batch VARCHAR(128) NOT NULL, 
	program_level VARCHAR(32) NOT NULL, 
	duration_years INTEGER NOT NULL, 
	dept_id VARCHAR(36), 
	credit_pattern VARCHAR(255), 
	exam_formula VARCHAR(255), 
	passing_marks INTEGER NOT NULL, 
	grade_boundaries VARCHAR(2000), 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME NOT NULL, 
	updated_at DATETIME NOT NULL, 
	is_deleted BOOLEAN NOT NULL, 
	deleted_at DATETIME, 
	CONSTRAINT pk_degrees PRIMARY KEY (id), 
	CONSTRAINT fk_degrees_dept_id_departments FOREIGN KEY(dept_id) REFERENCES departments (id)
);

-- departments -------------------------------------------------
CREATE TABLE departments (
	name VARCHAR(255) NOT NULL, 
	code VARCHAR(32) NOT NULL, 
	hod_id VARCHAR(36), 
	course_name VARCHAR(255), 
	duration_years INTEGER, 
	sem_count INTEGER, 
	establish_year INTEGER, 
	program_level VARCHAR(32), 
	intake INTEGER, 
	affiliation_code VARCHAR(255), 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME NOT NULL, 
	updated_at DATETIME NOT NULL, 
	is_deleted BOOLEAN NOT NULL, 
	deleted_at DATETIME, 
	CONSTRAINT pk_departments PRIMARY KEY (id), 
	CONSTRAINT uq_departments_code UNIQUE (code), 
	CONSTRAINT uq_departments_name UNIQUE (name)
);

-- exam_settings -----------------------------------------------
CREATE TABLE exam_settings (
	exam_id VARCHAR(36) NOT NULL, 
	halls VARCHAR(255) NOT NULL, 
	rules VARCHAR(4000) NOT NULL, 
	is_published BOOLEAN NOT NULL, 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME NOT NULL, 
	updated_at DATETIME NOT NULL, 
	is_deleted BOOLEAN NOT NULL, 
	deleted_at DATETIME, 
	CONSTRAINT pk_exam_settings PRIMARY KEY (id), 
	CONSTRAINT fk_exam_settings_exam_id_exams FOREIGN KEY(exam_id) REFERENCES exams (id)
);

-- exams -------------------------------------------------------
CREATE TABLE exams (
	course_id VARCHAR(36) NOT NULL, 
	type VARCHAR(8) NOT NULL, 
	center VARCHAR(128) NOT NULL, 
	date DATE NOT NULL, 
	start_time TIME NOT NULL, 
	end_time TIME NOT NULL, 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME NOT NULL, 
	updated_at DATETIME NOT NULL, 
	is_deleted BOOLEAN NOT NULL, 
	deleted_at DATETIME, 
	CONSTRAINT pk_exams PRIMARY KEY (id), 
	CONSTRAINT fk_exams_course_id_courses FOREIGN KEY(course_id) REFERENCES courses (id)
);

-- faculty_absences --------------------------------------------
CREATE TABLE faculty_absences (
	faculty_id VARCHAR(36) NOT NULL, 
	date DATE NOT NULL, 
	reason VARCHAR(512), 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME NOT NULL, 
	updated_at DATETIME NOT NULL, 
	is_deleted BOOLEAN NOT NULL, 
	deleted_at DATETIME, 
	CONSTRAINT pk_faculty_absences PRIMARY KEY (id), 
	CONSTRAINT fk_faculty_absences_faculty_id_users FOREIGN KEY(faculty_id) REFERENCES users (id)
);

-- faculty_profile_update_requests -----------------------------
CREATE TABLE faculty_profile_update_requests (
	user_id VARCHAR(36) NOT NULL, 
	status VARCHAR(64) NOT NULL, 
	faculty_id VARCHAR(64), 
	employee_code VARCHAR(64), 
	official_email VARCHAR(255), 
	official_phone VARCHAR(32), 
	gender VARCHAR(16), 
	date_of_birth DATE, 
	blood_group VARCHAR(16), 
	nationality VARCHAR(64), 
	designation VARCHAR(64), 
	department_name VARCHAR(255), 
	comments VARCHAR(512), 
	processed_at DATETIME, 
	processed_by VARCHAR(36), 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME NOT NULL, 
	updated_at DATETIME NOT NULL, 
	is_deleted BOOLEAN NOT NULL, 
	deleted_at DATETIME, 
	CONSTRAINT pk_faculty_profile_update_requests PRIMARY KEY (id), 
	CONSTRAINT fk_faculty_profile_update_requests_processed_by_users FOREIGN KEY(processed_by) REFERENCES users (id), 
	CONSTRAINT fk_faculty_profile_update_requests_user_id_users FOREIGN KEY(user_id) REFERENCES users (id)
);

-- faculty_profiles --------------------------------------------
CREATE TABLE faculty_profiles (
	user_id VARCHAR(36) NOT NULL, 
	designation VARCHAR(128) NOT NULL, 
	specialization VARCHAR(255), 
	approval_status VARCHAR(64) DEFAULT 'APPROVED' NOT NULL, 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME NOT NULL, 
	updated_at DATETIME NOT NULL, 
	is_deleted BOOLEAN NOT NULL, 
	deleted_at DATETIME, employee_code VARCHAR(64), gender VARCHAR(16), date_of_birth DATE, blood_group VARCHAR(16), marital_status VARCHAR(32), nationality VARCHAR(64), alternate_phone VARCHAR(32), personal_email VARCHAR(255), current_address VARCHAR(512), permanent_address VARCHAR(512), city VARCHAR(128), state VARCHAR(128), pincode VARCHAR(16), profile_photo_url VARCHAR(512), faculty_type VARCHAR(64), employment_category VARCHAR(64), date_of_joining DATE, employment_status VARCHAR(64), reporting_hod_id VARCHAR(36), reporting_principal_id VARCHAR(36), confirmation_date DATE, educational_qualifications JSON, experience_details JSON, academic_responsibilities JSON, certifications_achievements JSON, promotion_history JSON, increment_history JSON, documents_repository JSON, notification_preferences JSON, faculty_id VARCHAR(64), community VARCHAR(64), 
	CONSTRAINT pk_faculty_profiles PRIMARY KEY (id), 
	CONSTRAINT fk_faculty_profiles_user_id_users FOREIGN KEY(user_id) REFERENCES users (id), 
	CONSTRAINT uq_faculty_profiles_user_id UNIQUE (user_id)
);

-- faculty_research --------------------------------------------
CREATE TABLE "faculty_research" (
	faculty_id VARCHAR(36) NOT NULL, 
	title VARCHAR(255) NOT NULL, 
	publication VARCHAR(255), 
	grant_amount NUMERIC(12, 2), 
	proof_file_url VARCHAR(512), 
	status VARCHAR(64) NOT NULL, 
	comments VARCHAR(512), 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME NOT NULL, 
	updated_at DATETIME NOT NULL, 
	is_deleted BOOLEAN NOT NULL, 
	deleted_at DATETIME, publisher VARCHAR(255), publication_date DATE, isbn_issn VARCHAR(64), research_type VARCHAR(64), 
	CONSTRAINT pk_faculty_research PRIMARY KEY (id), 
	CONSTRAINT fk_faculty_research_faculty_id_users FOREIGN KEY(faculty_id) REFERENCES users (id)
);

-- faculty_workload --------------------------------------------
CREATE TABLE faculty_workload (
	faculty_id VARCHAR(36) NOT NULL, 
	semester INTEGER NOT NULL, 
	teaching_hours INTEGER NOT NULL, 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME NOT NULL, 
	updated_at DATETIME NOT NULL, 
	is_deleted BOOLEAN NOT NULL, 
	deleted_at DATETIME, 
	CONSTRAINT pk_faculty_workload PRIMARY KEY (id), 
	CONSTRAINT fk_faculty_workload_faculty_id_users FOREIGN KEY(faculty_id) REFERENCES users (id)
);

-- fee_records -------------------------------------------------
CREATE TABLE fee_records (
	student_id VARCHAR(36) NOT NULL, 
	fee_structure_id VARCHAR(36) NOT NULL, 
	status VARCHAR(7) NOT NULL, 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME NOT NULL, 
	updated_at DATETIME NOT NULL, 
	is_deleted BOOLEAN NOT NULL, 
	deleted_at DATETIME, 
	CONSTRAINT pk_fee_records PRIMARY KEY (id), 
	CONSTRAINT fk_fee_records_fee_structure_id_fee_structure FOREIGN KEY(fee_structure_id) REFERENCES fee_structure (id), 
	CONSTRAINT fk_fee_records_student_id_students FOREIGN KEY(student_id) REFERENCES students (id)
);

-- fee_structure -----------------------------------------------
CREATE TABLE fee_structure (
	dept_id VARCHAR(36) NOT NULL, 
	semester INTEGER NOT NULL, 
	amount NUMERIC(12, 2) NOT NULL, 
	due_date DATE NOT NULL, 
	fee_type VARCHAR(64) NOT NULL, 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME NOT NULL, 
	updated_at DATETIME NOT NULL, 
	is_deleted BOOLEAN NOT NULL, 
	deleted_at DATETIME, 
	CONSTRAINT pk_fee_structure PRIMARY KEY (id), 
	CONSTRAINT fk_fee_structure_dept_id_departments FOREIGN KEY(dept_id) REFERENCES departments (id)
);

-- financial_assistance_requests -------------------------------
CREATE TABLE financial_assistance_requests (
	student_id VARCHAR(36) NOT NULL, 
	type VARCHAR(64) NOT NULL, 
	reason VARCHAR(2048) NOT NULL, 
	status VARCHAR(32) DEFAULT 'PENDING' NOT NULL, 
	admin_remarks VARCHAR(1024), 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME NOT NULL, 
	updated_at DATETIME NOT NULL, 
	is_deleted BOOLEAN NOT NULL, 
	deleted_at DATETIME, 
	CONSTRAINT pk_financial_assistance_requests PRIMARY KEY (id), 
	CONSTRAINT fk_financial_assistance_requests_student_id_students FOREIGN KEY(student_id) REFERENCES students (id)
);

-- grants ------------------------------------------------------
CREATE TABLE grants (
	title VARCHAR(255) NOT NULL, 
	funding_agency VARCHAR(255) NOT NULL, 
	department_id VARCHAR(36), 
	principal_investigator VARCHAR(255), 
	sanctioned_amount NUMERIC(14, 2) DEFAULT '0' NOT NULL, 
	disbursed_amount NUMERIC(14, 2) DEFAULT '0' NOT NULL, 
	status VARCHAR(16) DEFAULT 'PROPOSED' NOT NULL, 
	start_date VARCHAR(32), 
	end_date VARCHAR(32), 
	notes VARCHAR(1024), 
	created_by VARCHAR(36) NOT NULL, 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, 
	updated_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, 
	is_deleted BOOLEAN DEFAULT 'false' NOT NULL, 
	deleted_at DATETIME, 
	CONSTRAINT pk_grants PRIMARY KEY (id), 
	CONSTRAINT fk_grants_department_id_departments FOREIGN KEY(department_id) REFERENCES departments (id), 
	CONSTRAINT fk_grants_created_by_users FOREIGN KEY(created_by) REFERENCES users (id)
);

-- grievances --------------------------------------------------
CREATE TABLE grievances (
	raised_by VARCHAR(36) NOT NULL, 
	category VARCHAR(128) NOT NULL, 
	description VARCHAR(4000) NOT NULL, 
	status VARCHAR(32) NOT NULL, 
	assigned_to VARCHAR(36), 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME NOT NULL, 
	updated_at DATETIME NOT NULL, 
	is_deleted BOOLEAN NOT NULL, 
	deleted_at DATETIME, subject VARCHAR(255) DEFAULT 'General' NOT NULL, priority VARCHAR(16) DEFAULT 'Medium' NOT NULL, resolution_date VARCHAR(32), resolution_rating INTEGER, resolution_feedback VARCHAR(2000), 
	CONSTRAINT pk_grievances PRIMARY KEY (id), 
	CONSTRAINT fk_grievances_assigned_to_users FOREIGN KEY(assigned_to) REFERENCES users (id), 
	CONSTRAINT fk_grievances_raised_by_users FOREIGN KEY(raised_by) REFERENCES users (id)
);

-- hall_tickets ------------------------------------------------
CREATE TABLE hall_tickets (
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME DEFAULT (CURRENT_TIMESTAMP) NOT NULL, 
	updated_at DATETIME DEFAULT (CURRENT_TIMESTAMP) NOT NULL, 
	is_deleted BOOLEAN DEFAULT 'false' NOT NULL, 
	deleted_at DATETIME, 
	student_id VARCHAR(36) NOT NULL, 
	exam_id VARCHAR(36), 
	exam_name VARCHAR(255) NOT NULL, 
	is_eligible BOOLEAN DEFAULT 'true' NOT NULL, 
	ineligibility_reason VARCHAR(1024), 
	is_issued BOOLEAN DEFAULT 'false' NOT NULL, 
	file_url VARCHAR(512), 
	issued_at DATETIME, 
	exam_center VARCHAR(255), 
	exam_date VARCHAR(64), 
	student_signature_url VARCHAR(512), 
	principal_signature_url VARCHAR(512), 
	coe_signature_url VARCHAR(512), 
	CONSTRAINT pk_hall_tickets PRIMARY KEY (id), 
	CONSTRAINT fk_hall_tickets_student_id_students FOREIGN KEY(student_id) REFERENCES students (id), 
	CONSTRAINT fk_hall_tickets_exam_id_exams FOREIGN KEY(exam_id) REFERENCES exams (id)
);

-- hostel_allocations ------------------------------------------
CREATE TABLE hostel_allocations (
	room_id VARCHAR(36) NOT NULL, 
	student_id VARCHAR(36) NOT NULL, 
	allocated_on DATE NOT NULL, 
	vacated_on DATE, 
	status VARCHAR(16) NOT NULL, 
	remarks VARCHAR(512), 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, 
	updated_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, 
	is_deleted BOOLEAN DEFAULT 'false' NOT NULL, 
	deleted_at DATETIME, 
	CONSTRAINT pk_hostel_allocations PRIMARY KEY (id), 
	CONSTRAINT fk_hostel_allocations_room_id_hostel_rooms FOREIGN KEY(room_id) REFERENCES hostel_rooms (id), 
	CONSTRAINT fk_hostel_allocations_student_id_students FOREIGN KEY(student_id) REFERENCES students (id)
);

-- hostel_blocks -----------------------------------------------
CREATE TABLE hostel_blocks (
	name VARCHAR(128) NOT NULL, 
	code VARCHAR(32) NOT NULL, 
	hostel_type VARCHAR(16) NOT NULL, 
	warden_name VARCHAR(128), 
	warden_phone VARCHAR(20), 
	address VARCHAR(512), 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, 
	updated_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, 
	is_deleted BOOLEAN DEFAULT 'false' NOT NULL, 
	deleted_at DATETIME, 
	CONSTRAINT pk_hostel_blocks PRIMARY KEY (id), 
	CONSTRAINT uq_hostel_block_code UNIQUE (code)
);

-- hostel_rooms ------------------------------------------------
CREATE TABLE hostel_rooms (
	block_id VARCHAR(36) NOT NULL, 
	room_number VARCHAR(32) NOT NULL, 
	floor INTEGER NOT NULL, 
	capacity INTEGER NOT NULL, 
	room_type VARCHAR(64), 
	monthly_rent NUMERIC(12, 2), 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, 
	updated_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, 
	is_deleted BOOLEAN DEFAULT 'false' NOT NULL, 
	deleted_at DATETIME, 
	CONSTRAINT pk_hostel_rooms PRIMARY KEY (id), 
	CONSTRAINT fk_hostel_rooms_block_id_hostel_blocks FOREIGN KEY(block_id) REFERENCES hostel_blocks (id), 
	CONSTRAINT uq_hostel_room_block_number UNIQUE (block_id, room_number)
);

-- internal_marks ----------------------------------------------
CREATE TABLE internal_marks (
	student_id VARCHAR(36) NOT NULL, 
	section_id VARCHAR(36) NOT NULL, 
	subject_id VARCHAR(36) NOT NULL, 
	academic_year VARCHAR(32) NOT NULL, 
	semester VARCHAR(32), 
	internal_exam_mark NUMERIC(5, 2) NOT NULL, 
	assignment_mark NUMERIC(5, 2) NOT NULL, 
	presentation_mark NUMERIC(5, 2) NOT NULL, 
	viva_voice_mark NUMERIC(5, 2) NOT NULL, 
	attendance_mark NUMERIC(5, 2) NOT NULL, 
	total_mark NUMERIC(5, 2) NOT NULL, 
	status VARCHAR(32) NOT NULL, 
	hod_message VARCHAR(1024), 
	faculty_reply VARCHAR(1024), 
	is_message_visible_to_student BOOLEAN NOT NULL, 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME NOT NULL, 
	updated_at DATETIME NOT NULL, 
	is_deleted BOOLEAN NOT NULL, 
	deleted_at DATETIME, 
	CONSTRAINT pk_internal_marks PRIMARY KEY (id), 
	CONSTRAINT fk_internal_marks_student_id_students FOREIGN KEY(student_id) REFERENCES students (id), 
	CONSTRAINT fk_internal_marks_section_id_sections FOREIGN KEY(section_id) REFERENCES sections (id), 
	CONSTRAINT fk_internal_marks_subject_id_courses FOREIGN KEY(subject_id) REFERENCES courses (id)
);

-- internship_applications -------------------------------------
CREATE TABLE internship_applications (
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME DEFAULT (CURRENT_TIMESTAMP) NOT NULL, 
	updated_at DATETIME DEFAULT (CURRENT_TIMESTAMP) NOT NULL, 
	is_deleted BOOLEAN DEFAULT 'false' NOT NULL, 
	deleted_at DATETIME, 
	drive_id VARCHAR(36) NOT NULL, 
	student_id VARCHAR(36) NOT NULL, 
	status VARCHAR(32) DEFAULT 'Applied' NOT NULL, 
	CONSTRAINT pk_internship_applications PRIMARY KEY (id), 
	CONSTRAINT uq_internship_application_drive_student UNIQUE (drive_id, student_id), 
	CONSTRAINT fk_internship_applications_drive_id_internship_drives FOREIGN KEY(drive_id) REFERENCES internship_drives (id), 
	CONSTRAINT fk_internship_applications_student_id_students FOREIGN KEY(student_id) REFERENCES students (id)
);

-- internship_drives -------------------------------------------
CREATE TABLE internship_drives (
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME DEFAULT (CURRENT_TIMESTAMP) NOT NULL, 
	updated_at DATETIME DEFAULT (CURRENT_TIMESTAMP) NOT NULL, 
	is_deleted BOOLEAN DEFAULT 'false' NOT NULL, 
	deleted_at DATETIME, 
	company_name VARCHAR(255) NOT NULL, 
	role VARCHAR(255) NOT NULL, 
	package VARCHAR(128), 
	drive_date DATE, 
	status VARCHAR(32) DEFAULT 'Hiring' NOT NULL, 
	description VARCHAR(2048), 
	CONSTRAINT pk_internship_drives PRIMARY KEY (id)
);

-- inventory_items ---------------------------------------------
CREATE TABLE inventory_items (
	name VARCHAR(255) NOT NULL, 
	code VARCHAR(64) NOT NULL, 
	category VARCHAR(128), 
	unit VARCHAR(32) NOT NULL, 
	quantity INTEGER NOT NULL, 
	min_quantity INTEGER NOT NULL, 
	unit_price NUMERIC(12, 2), 
	location VARCHAR(255), 
	supplier VARCHAR(255), 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, 
	updated_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, 
	is_deleted BOOLEAN DEFAULT 'false' NOT NULL, 
	deleted_at DATETIME, 
	CONSTRAINT pk_inventory_items PRIMARY KEY (id), 
	CONSTRAINT uq_inventory_item_code UNIQUE (code)
);

-- inventory_transactions --------------------------------------
CREATE TABLE inventory_transactions (
	item_id VARCHAR(36) NOT NULL, 
	movement VARCHAR(16) NOT NULL, 
	quantity INTEGER NOT NULL, 
	resulting_quantity INTEGER NOT NULL, 
	reason VARCHAR(512), 
	performed_by VARCHAR(36), 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, 
	updated_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, 
	is_deleted BOOLEAN DEFAULT 'false' NOT NULL, 
	deleted_at DATETIME, 
	CONSTRAINT pk_inventory_transactions PRIMARY KEY (id), 
	CONSTRAINT fk_inventory_transactions_item_id_inventory_items FOREIGN KEY(item_id) REFERENCES inventory_items (id), 
	CONSTRAINT fk_inventory_transactions_performed_by_users FOREIGN KEY(performed_by) REFERENCES users (id)
);

-- leave_approvals ---------------------------------------------
CREATE TABLE leave_approvals (
	leave_id VARCHAR(36) NOT NULL, 
	approved_by VARCHAR(36) NOT NULL, 
	status VARCHAR(21) NOT NULL, 
	remarks VARCHAR(1024), 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME NOT NULL, 
	updated_at DATETIME NOT NULL, 
	is_deleted BOOLEAN NOT NULL, 
	deleted_at DATETIME, 
	CONSTRAINT pk_leave_approvals PRIMARY KEY (id), 
	CONSTRAINT fk_leave_approvals_approved_by_users FOREIGN KEY(approved_by) REFERENCES users (id), 
	CONSTRAINT fk_leave_approvals_leave_id_leaves FOREIGN KEY(leave_id) REFERENCES leaves (id)
);

-- leave_balances ----------------------------------------------
CREATE TABLE leave_balances (
	user_id VARCHAR(36) NOT NULL, 
	casual_leave FLOAT NOT NULL, 
	sick_leave FLOAT NOT NULL, 
	earned_leave FLOAT NOT NULL, 
	on_duty_leave FLOAT NOT NULL, 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME NOT NULL, 
	updated_at DATETIME NOT NULL, 
	is_deleted BOOLEAN NOT NULL, 
	deleted_at DATETIME, 
	CONSTRAINT pk_leave_balances PRIMARY KEY (id), 
	CONSTRAINT fk_leave_balances_user_id_users FOREIGN KEY(user_id) REFERENCES users (id), 
	CONSTRAINT uq_leave_balances_user_id UNIQUE (user_id)
);

-- leaves ------------------------------------------------------
CREATE TABLE leaves (
	user_id VARCHAR(36) NOT NULL, 
	type VARCHAR(64) NOT NULL, 
	from_date DATE NOT NULL, 
	to_date DATE NOT NULL, 
	num_days FLOAT NOT NULL, 
	reason VARCHAR(1024) NOT NULL, 
	emergency_contact VARCHAR(32) NOT NULL, 
	attachment_url VARCHAR(512), 
	status VARCHAR(21) NOT NULL, 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME NOT NULL, 
	updated_at DATETIME NOT NULL, 
	is_deleted BOOLEAN NOT NULL, 
	deleted_at DATETIME, hod_status VARCHAR(32), hod_action_by VARCHAR(36), hod_action_date DATETIME, hod_remarks VARCHAR(1024), principal_action_by VARCHAR(36), principal_action_date DATETIME, principal_remarks VARCHAR(1024), 
	CONSTRAINT pk_leaves PRIMARY KEY (id), 
	CONSTRAINT fk_leaves_user_id_users FOREIGN KEY(user_id) REFERENCES users (id)
);

-- library_books -----------------------------------------------
CREATE TABLE library_books (
	title VARCHAR(512) NOT NULL, 
	author VARCHAR(255), 
	accession_no VARCHAR(64) NOT NULL, 
	isbn VARCHAR(32), 
	category VARCHAR(128), 
	publisher VARCHAR(255), 
	published_year INTEGER, 
	shelf_location VARCHAR(128), 
	total_copies INTEGER NOT NULL, 
	available_copies INTEGER NOT NULL, 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, 
	updated_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, 
	is_deleted BOOLEAN DEFAULT 'false' NOT NULL, 
	deleted_at DATETIME, 
	CONSTRAINT pk_library_books PRIMARY KEY (id), 
	CONSTRAINT uq_library_book_accession UNIQUE (accession_no)
);

-- library_issues ----------------------------------------------
CREATE TABLE library_issues (
	book_id VARCHAR(36) NOT NULL, 
	member_id VARCHAR(36) NOT NULL, 
	issued_on DATE NOT NULL, 
	due_on DATE NOT NULL, 
	returned_on DATE, 
	fine_amount NUMERIC(12, 2) NOT NULL, 
	status VARCHAR(16) NOT NULL, 
	remarks VARCHAR(512), 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, 
	updated_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, 
	is_deleted BOOLEAN DEFAULT 'false' NOT NULL, 
	deleted_at DATETIME, 
	CONSTRAINT pk_library_issues PRIMARY KEY (id), 
	CONSTRAINT fk_library_issues_book_id_library_books FOREIGN KEY(book_id) REFERENCES library_books (id), 
	CONSTRAINT fk_library_issues_member_id_users FOREIGN KEY(member_id) REFERENCES users (id)
);

-- local_json_db -----------------------------------------------
CREATE TABLE local_json_db (
	"key" VARCHAR(255) NOT NULL, 
	data TEXT, 
	updated_at DATETIME DEFAULT (CURRENT_TIMESTAMP), 
	CONSTRAINT pk_local_json_db PRIMARY KEY ("key")
);

-- marks -------------------------------------------------------
CREATE TABLE marks (
	student_id VARCHAR(36) NOT NULL, 
	section_id VARCHAR(36) NOT NULL, 
	exam_type VARCHAR(8) NOT NULL, 
	mark NUMERIC(5, 2) NOT NULL, 
	max_mark NUMERIC(5, 2) NOT NULL, 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME NOT NULL, 
	updated_at DATETIME NOT NULL, 
	is_deleted BOOLEAN NOT NULL, 
	deleted_at DATETIME, 
	CONSTRAINT pk_marks PRIMARY KEY (id), 
	CONSTRAINT fk_marks_section_id_sections FOREIGN KEY(section_id) REFERENCES sections (id), 
	CONSTRAINT fk_marks_student_id_students FOREIGN KEY(student_id) REFERENCES students (id)
);

-- mentorship_records ------------------------------------------
CREATE TABLE mentorship_records (
	student_id VARCHAR(36) NOT NULL, 
	mentor_id VARCHAR(36) NOT NULL, 
	meeting_log VARCHAR(4000), 
	academic_review VARCHAR(4000), 
	improvement_plan VARCHAR(4000), 
	remarks VARCHAR(4000), 
	follow_up VARCHAR(4000), 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME NOT NULL, 
	updated_at DATETIME NOT NULL, 
	is_deleted BOOLEAN NOT NULL, 
	deleted_at DATETIME, 
	CONSTRAINT pk_mentorship_records PRIMARY KEY (id), 
	CONSTRAINT fk_mentorship_records_student_id_students FOREIGN KEY(student_id) REFERENCES students (id), 
	CONSTRAINT fk_mentorship_records_mentor_id_users FOREIGN KEY(mentor_id) REFERENCES users (id)
);

-- messages ----------------------------------------------------
CREATE TABLE messages (
	sender_id VARCHAR(36) NOT NULL, 
	receiver_id VARCHAR(36) NOT NULL, 
	body VARCHAR(4000) NOT NULL, 
	is_read BOOLEAN NOT NULL, 
	read_at DATETIME, 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME NOT NULL, 
	updated_at DATETIME NOT NULL, 
	is_deleted BOOLEAN NOT NULL, 
	deleted_at DATETIME, 
	CONSTRAINT pk_messages PRIMARY KEY (id), 
	CONSTRAINT fk_messages_receiver_id_users FOREIGN KEY(receiver_id) REFERENCES users (id), 
	CONSTRAINT fk_messages_sender_id_users FOREIGN KEY(sender_id) REFERENCES users (id)
);

-- moot_court_memorials ----------------------------------------
CREATE TABLE moot_court_memorials (
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME DEFAULT (CURRENT_TIMESTAMP) NOT NULL, 
	updated_at DATETIME DEFAULT (CURRENT_TIMESTAMP) NOT NULL, 
	is_deleted BOOLEAN DEFAULT 'false' NOT NULL, 
	deleted_at DATETIME, 
	student_id VARCHAR(36) NOT NULL, 
	title VARCHAR(255) NOT NULL, 
	case_name VARCHAR(255), 
	content TEXT NOT NULL, 
	status VARCHAR(32) DEFAULT 'draft' NOT NULL, 
	CONSTRAINT pk_moot_court_memorials PRIMARY KEY (id), 
	CONSTRAINT fk_moot_court_memorials_student_id_students FOREIGN KEY(student_id) REFERENCES students (id)
);

-- notice_acknowledgements -------------------------------------
CREATE TABLE notice_acknowledgements (
	notice_id VARCHAR(36) NOT NULL, 
	user_id VARCHAR(36) NOT NULL, 
	is_read BOOLEAN NOT NULL, 
	read_at DATETIME, 
	is_acknowledged BOOLEAN NOT NULL, 
	acknowledged_at DATETIME, 
	status VARCHAR(32) NOT NULL, 
	is_archived BOOLEAN NOT NULL, 
	archived_at DATETIME, 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME DEFAULT (CURRENT_TIMESTAMP) NOT NULL, 
	updated_at DATETIME DEFAULT (CURRENT_TIMESTAMP) NOT NULL, 
	is_deleted BOOLEAN DEFAULT false NOT NULL, 
	deleted_at DATETIME, 
	CONSTRAINT pk_notice_acknowledgements PRIMARY KEY (id), 
	CONSTRAINT fk_notice_acknowledgements_notice_id_notices FOREIGN KEY(notice_id) REFERENCES notices (id), 
	CONSTRAINT fk_notice_acknowledgements_user_id_users FOREIGN KEY(user_id) REFERENCES users (id)
);

-- notices -----------------------------------------------------
CREATE TABLE notices (
	created_by VARCHAR(36) NOT NULL, 
	title VARCHAR(255) NOT NULL, 
	body VARCHAR(4000) NOT NULL, 
	audience_type VARCHAR(64) NOT NULL, 
	publish_date DATE NOT NULL, 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME NOT NULL, 
	updated_at DATETIME NOT NULL, 
	is_deleted BOOLEAN NOT NULL, 
	deleted_at DATETIME, category VARCHAR(64), expiry_date DATE, priority VARCHAR(32), status VARCHAR(32), publisher_role VARCHAR(32), target_department VARCHAR(64), target_semester INTEGER, target_section VARCHAR(64), event_date DATE, audience_types VARCHAR(512), degree_id VARCHAR(36), batch_id VARCHAR(64), department_id VARCHAR(36), attachment_url VARCHAR(512), 
	CONSTRAINT pk_notices PRIMARY KEY (id), 
	CONSTRAINT fk_notices_created_by_users FOREIGN KEY(created_by) REFERENCES users (id)
);

-- notifications -----------------------------------------------
CREATE TABLE notifications (
	user_id VARCHAR(36) NOT NULL, 
	type VARCHAR(64) NOT NULL, 
	message VARCHAR(1024) NOT NULL, 
	is_read BOOLEAN NOT NULL, 
	sent_via VARCHAR(32) NOT NULL, 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME NOT NULL, 
	updated_at DATETIME NOT NULL, 
	is_deleted BOOLEAN NOT NULL, 
	deleted_at DATETIME, 
	CONSTRAINT pk_notifications PRIMARY KEY (id), 
	CONSTRAINT fk_notifications_user_id_users FOREIGN KEY(user_id) REFERENCES users (id)
);

-- parent_student_map ------------------------------------------
CREATE TABLE parent_student_map (
	parent_id VARCHAR(36) NOT NULL, 
	student_id VARCHAR(36) NOT NULL, 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME NOT NULL, 
	updated_at DATETIME NOT NULL, 
	is_deleted BOOLEAN NOT NULL, 
	deleted_at DATETIME, 
	CONSTRAINT pk_parent_student_map PRIMARY KEY (id), 
	CONSTRAINT fk_parent_student_map_parent_id_users FOREIGN KEY(parent_id) REFERENCES users (id), 
	CONSTRAINT fk_parent_student_map_student_id_students FOREIGN KEY(student_id) REFERENCES students (id)
);

-- partner_companies -------------------------------------------
CREATE TABLE partner_companies (
	name VARCHAR(255) NOT NULL, 
	industry VARCHAR(128) NOT NULL, 
	status VARCHAR(32) DEFAULT 'Active' NOT NULL, 
	contact_email VARCHAR(255), 
	contact_phone VARCHAR(32), 
	notes VARCHAR(1024), 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME DEFAULT (CURRENT_TIMESTAMP) NOT NULL, 
	updated_at DATETIME DEFAULT (CURRENT_TIMESTAMP) NOT NULL, 
	is_deleted BOOLEAN DEFAULT 'false' NOT NULL, 
	deleted_at DATETIME, 
	CONSTRAINT pk_partner_companies PRIMARY KEY (id)
);

-- password_reset_tokens ---------------------------------------
CREATE TABLE password_reset_tokens (
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME DEFAULT (CURRENT_TIMESTAMP) NOT NULL, 
	updated_at DATETIME DEFAULT (CURRENT_TIMESTAMP) NOT NULL, 
	is_deleted BOOLEAN DEFAULT 'false' NOT NULL, 
	deleted_at DATETIME, 
	user_id VARCHAR(36) NOT NULL, 
	token VARCHAR(128) NOT NULL, 
	expires_at DATETIME NOT NULL, 
	used_at DATETIME, 
	CONSTRAINT pk_password_reset_tokens PRIMARY KEY (id), 
	CONSTRAINT fk_password_reset_tokens_user_id_users FOREIGN KEY(user_id) REFERENCES users (id)
);

-- payments ----------------------------------------------------
CREATE TABLE payments (
	fee_record_id VARCHAR(36) NOT NULL, 
	amount NUMERIC(12, 2) NOT NULL, 
	mode VARCHAR(32) NOT NULL, 
	txn_id VARCHAR(128) NOT NULL, 
	receipt_url VARCHAR(512), 
	paid_at DATETIME NOT NULL, 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME NOT NULL, 
	updated_at DATETIME NOT NULL, 
	is_deleted BOOLEAN NOT NULL, 
	deleted_at DATETIME, razorpay_order_id VARCHAR(128), razorpay_payment_id VARCHAR(128), razorpay_signature VARCHAR(256), status VARCHAR(32) DEFAULT 'created' NOT NULL, 
	CONSTRAINT pk_payments PRIMARY KEY (id), 
	CONSTRAINT fk_payments_fee_record_id_fee_records FOREIGN KEY(fee_record_id) REFERENCES fee_records (id), 
	CONSTRAINT uq_payments_txn_id UNIQUE (txn_id)
);

-- pf_audit_logs -----------------------------------------------
CREATE TABLE pf_audit_logs (
	faculty_id VARCHAR(36) NOT NULL, 
	action VARCHAR(255) NOT NULL, 
	details TEXT, 
	performed_by VARCHAR(36) NOT NULL, 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME NOT NULL, 
	updated_at DATETIME NOT NULL, 
	is_deleted BOOLEAN NOT NULL, 
	deleted_at DATETIME, 
	CONSTRAINT pk_pf_audit_logs PRIMARY KEY (id), 
	CONSTRAINT fk_pf_audit_logs_faculty_id_users FOREIGN KEY(faculty_id) REFERENCES users (id), 
	CONSTRAINT fk_pf_audit_logs_performed_by_users FOREIGN KEY(performed_by) REFERENCES users (id)
);

-- pf_claims ---------------------------------------------------
CREATE TABLE pf_claims (
	faculty_id VARCHAR(36) NOT NULL, 
	claim_date DATE NOT NULL, 
	amount NUMERIC(12, 2) NOT NULL, 
	reference_number VARCHAR(128) NOT NULL, 
	remarks TEXT, 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME NOT NULL, 
	updated_at DATETIME NOT NULL, 
	is_deleted BOOLEAN NOT NULL, 
	deleted_at DATETIME, 
	CONSTRAINT pk_pf_claims PRIMARY KEY (id), 
	CONSTRAINT fk_pf_claims_faculty_id_users FOREIGN KEY(faculty_id) REFERENCES users (id)
);

-- pf_configurations -------------------------------------------
CREATE TABLE pf_configurations (
	faculty_id VARCHAR(36) NOT NULL, 
	joining_date DATE NOT NULL, 
	pf_start_date DATE NOT NULL, 
	historical_opening_balance NUMERIC(12, 2) NOT NULL, 
	calculation_method VARCHAR(10) NOT NULL, 
	value NUMERIC(12, 2) NOT NULL, 
	based_on_earned_salary BOOLEAN NOT NULL, 
	basic_salary NUMERIC(12, 2) NOT NULL, 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME NOT NULL, 
	updated_at DATETIME NOT NULL, 
	is_deleted BOOLEAN NOT NULL, 
	deleted_at DATETIME, 
	CONSTRAINT pk_pf_configurations PRIMARY KEY (id), 
	CONSTRAINT fk_pf_configurations_faculty_id_users FOREIGN KEY(faculty_id) REFERENCES users (id), 
	CONSTRAINT uq_pf_configurations_faculty_id UNIQUE (faculty_id)
);

-- pf_contributions --------------------------------------------
CREATE TABLE pf_contributions (
	faculty_id VARCHAR(36) NOT NULL, 
	salary_id VARCHAR(36), 
	month INTEGER NOT NULL, 
	year INTEGER NOT NULL, 
	amount NUMERIC(12, 2) NOT NULL, 
	employer_amount NUMERIC(12, 2) NOT NULL, 
	is_historical BOOLEAN NOT NULL, 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME NOT NULL, 
	updated_at DATETIME NOT NULL, 
	is_deleted BOOLEAN NOT NULL, 
	deleted_at DATETIME, 
	CONSTRAINT pk_pf_contributions PRIMARY KEY (id), 
	CONSTRAINT fk_pf_contributions_faculty_id_users FOREIGN KEY(faculty_id) REFERENCES users (id), 
	CONSTRAINT fk_pf_contributions_salary_id_salary FOREIGN KEY(salary_id) REFERENCES salary (id)
);

-- pf_historical_periods ---------------------------------------
CREATE TABLE pf_historical_periods (
	faculty_id VARCHAR(36) NOT NULL, 
	from_date DATE NOT NULL, 
	to_date DATE NOT NULL, 
	amount_per_month NUMERIC(12, 2) NOT NULL, 
	months INTEGER NOT NULL, 
	total_amount NUMERIC(12, 2) NOT NULL, 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME NOT NULL, 
	updated_at DATETIME NOT NULL, 
	is_deleted BOOLEAN NOT NULL, 
	deleted_at DATETIME, 
	CONSTRAINT pk_pf_historical_periods PRIMARY KEY (id), 
	CONSTRAINT fk_pf_historical_periods_faculty_id_users FOREIGN KEY(faculty_id) REFERENCES users (id)
);

-- pf_leave_exclusions -----------------------------------------
CREATE TABLE pf_leave_exclusions (
	faculty_id VARCHAR(36) NOT NULL, 
	from_date DATE NOT NULL, 
	to_date DATE NOT NULL, 
	reason VARCHAR(255), 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME NOT NULL, 
	updated_at DATETIME NOT NULL, 
	is_deleted BOOLEAN NOT NULL, 
	deleted_at DATETIME, 
	CONSTRAINT pk_pf_leave_exclusions PRIMARY KEY (id), 
	CONSTRAINT fk_pf_leave_exclusions_faculty_id_users FOREIGN KEY(faculty_id) REFERENCES users (id)
);

-- publication_plans -------------------------------------------
CREATE TABLE publication_plans (
	faculty_id VARCHAR(36) NOT NULL, 
	title VARCHAR(255) NOT NULL, 
	journal_conference VARCHAR(255) NOT NULL, 
	target_date DATE NOT NULL, 
	status VARCHAR(32) NOT NULL, 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME NOT NULL, 
	updated_at DATETIME NOT NULL, 
	is_deleted BOOLEAN NOT NULL, 
	deleted_at DATETIME, research_area VARCHAR(255), publication_type VARCHAR(128), expected_publication_date DATE, academic_year VARCHAR(64), 
	CONSTRAINT pk_publication_plans PRIMARY KEY (id), 
	CONSTRAINT fk_publication_plans_faculty_id_users FOREIGN KEY(faculty_id) REFERENCES users (id)
);

-- publication_proofs ------------------------------------------
CREATE TABLE publication_proofs (
	plan_id VARCHAR(36) NOT NULL, 
	publication_date DATE NOT NULL, 
	journal_name VARCHAR(255) NOT NULL, 
	issn_isbn VARCHAR(64) NOT NULL, 
	doi_number VARCHAR(64), 
	publication_link VARCHAR(512), 
	proof_file_url VARCHAR(512) NOT NULL, 
	status VARCHAR(64) NOT NULL, 
	remarks VARCHAR(1024), 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME NOT NULL, 
	updated_at DATETIME NOT NULL, 
	is_deleted BOOLEAN NOT NULL, 
	deleted_at DATETIME, 
	CONSTRAINT pk_publication_proofs PRIMARY KEY (id), 
	CONSTRAINT fk_publication_proofs_plan_id_research_plans FOREIGN KEY(plan_id) REFERENCES research_plans (id)
);

-- regulations -------------------------------------------------
CREATE TABLE "regulations" (
	code VARCHAR(32) NOT NULL, 
	name VARCHAR(255) NOT NULL, 
	applicable_batch VARCHAR(128) NOT NULL, 
	program_level VARCHAR(32) NOT NULL, 
	duration_years INTEGER NOT NULL, 
	credit_pattern VARCHAR(255), 
	exam_formula VARCHAR(255), 
	passing_marks INTEGER NOT NULL, 
	grade_boundaries VARCHAR(2000), 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME NOT NULL, 
	updated_at DATETIME NOT NULL, 
	is_deleted BOOLEAN NOT NULL, 
	deleted_at DATETIME, 
	CONSTRAINT pk_regulations PRIMARY KEY (id), 
	CONSTRAINT uq_regulation_code_program_batch UNIQUE (code, program_level, applicable_batch)
);

-- research_compliance -----------------------------------------
CREATE TABLE research_compliance (
	faculty_id VARCHAR(36) NOT NULL, 
	requirement_name VARCHAR(255) NOT NULL, 
	deadline DATE NOT NULL, 
	status VARCHAR(32) NOT NULL, 
	submitted_at DATETIME, 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME NOT NULL, 
	updated_at DATETIME NOT NULL, 
	is_deleted BOOLEAN NOT NULL, 
	deleted_at DATETIME, 
	CONSTRAINT pk_research_compliance PRIMARY KEY (id), 
	CONSTRAINT fk_research_compliance_faculty_id_users FOREIGN KEY(faculty_id) REFERENCES users (id)
);

-- research_plans ----------------------------------------------
CREATE TABLE research_plans (
	faculty_id VARCHAR(36) NOT NULL, 
	title VARCHAR(255) NOT NULL, 
	area VARCHAR(128) NOT NULL, 
	target_journal_conference VARCHAR(255) NOT NULL, 
	type VARCHAR(128) NOT NULL, 
	start_date DATE NOT NULL, 
	expected_completion_date DATE NOT NULL, 
	objectives VARCHAR(1024) NOT NULL, 
	abstract_summary VARCHAR(2048) NOT NULL, 
	status VARCHAR(64) NOT NULL, 
	cycle_start_date DATE NOT NULL, 
	cycle_due_date DATE NOT NULL, 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME NOT NULL, 
	updated_at DATETIME NOT NULL, 
	is_deleted BOOLEAN NOT NULL, 
	deleted_at DATETIME, 
	CONSTRAINT pk_research_plans PRIMARY KEY (id), 
	CONSTRAINT fk_research_plans_faculty_id_users FOREIGN KEY(faculty_id) REFERENCES users (id)
);

-- research_progress_updates -----------------------------------
CREATE TABLE research_progress_updates (
	plan_id VARCHAR(36) NOT NULL, 
	progress_date DATE NOT NULL, 
	current_stage VARCHAR(128) NOT NULL, 
	percentage_completed INTEGER NOT NULL, 
	work_completed VARCHAR(1024) NOT NULL, 
	remarks VARCHAR(1024), 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME NOT NULL, 
	updated_at DATETIME NOT NULL, 
	is_deleted BOOLEAN NOT NULL, 
	deleted_at DATETIME, 
	CONSTRAINT pk_research_progress_updates PRIMARY KEY (id), 
	CONSTRAINT fk_research_progress_updates_plan_id_research_plans FOREIGN KEY(plan_id) REFERENCES research_plans (id)
);

-- research_verifications --------------------------------------
CREATE TABLE research_verifications (
	proof_id VARCHAR(36) NOT NULL, 
	verified_by VARCHAR(36) NOT NULL, 
	status VARCHAR(64) NOT NULL, 
	remarks VARCHAR(1024), 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME NOT NULL, 
	updated_at DATETIME NOT NULL, 
	is_deleted BOOLEAN NOT NULL, 
	deleted_at DATETIME, 
	CONSTRAINT pk_research_verifications PRIMARY KEY (id), 
	CONSTRAINT fk_research_verifications_proof_id_publication_proofs FOREIGN KEY(proof_id) REFERENCES publication_proofs (id), 
	CONSTRAINT fk_research_verifications_verified_by_users FOREIGN KEY(verified_by) REFERENCES users (id)
);

-- salary ------------------------------------------------------
CREATE TABLE salary (
	faculty_id VARCHAR(36) NOT NULL, 
	basic NUMERIC(12, 2) NOT NULL, 
	allowances NUMERIC(12, 2) NOT NULL, 
	gross NUMERIC(12, 2) NOT NULL, 
	month INTEGER NOT NULL, 
	year INTEGER NOT NULL, 
	total_working_days INTEGER DEFAULT '30' NOT NULL, 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME NOT NULL, 
	updated_at DATETIME NOT NULL, 
	is_deleted BOOLEAN NOT NULL, 
	deleted_at DATETIME, employee_id VARCHAR(64), designation VARCHAR(128), working_days INTEGER DEFAULT '30' NOT NULL, leave_days INTEGER DEFAULT '0' NOT NULL, net_salary NUMERIC(12, 2) DEFAULT '0.0' NOT NULL, leave_deduction NUMERIC(12, 2), pf_deduction NUMERIC(12, 2), joining_date DATE, 
	CONSTRAINT pk_salary PRIMARY KEY (id), 
	CONSTRAINT fk_salary_faculty_id_users FOREIGN KEY(faculty_id) REFERENCES users (id)
);

-- salary_slip_requests ----------------------------------------
CREATE TABLE salary_slip_requests (
	faculty_id VARCHAR(36) NOT NULL, 
	request_type VARCHAR(64) NOT NULL, 
	month INTEGER NOT NULL, 
	year INTEGER NOT NULL, 
	remarks VARCHAR(1024), 
	status VARCHAR(32) NOT NULL, 
	admin_remarks VARCHAR(1024), 
	salary_slip_id VARCHAR(36), 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME NOT NULL, 
	updated_at DATETIME NOT NULL, 
	is_deleted BOOLEAN NOT NULL, 
	deleted_at DATETIME, 
	CONSTRAINT pk_salary_slip_requests PRIMARY KEY (id), 
	CONSTRAINT fk_salary_slip_requests_faculty_id_users FOREIGN KEY(faculty_id) REFERENCES users (id), 
	CONSTRAINT fk_salary_slip_requests_salary_slip_id_salary_slips FOREIGN KEY(salary_slip_id) REFERENCES salary_slips (id)
);

-- salary_slips ------------------------------------------------
CREATE TABLE salary_slips (
	salary_id VARCHAR(36) NOT NULL, 
	pdf_url VARCHAR(512) NOT NULL, 
	generated_at DATETIME NOT NULL, 
	delivered_at DATETIME, 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME NOT NULL, 
	updated_at DATETIME NOT NULL, 
	is_deleted BOOLEAN NOT NULL, 
	deleted_at DATETIME, 
	CONSTRAINT pk_salary_slips PRIMARY KEY (id), 
	CONSTRAINT fk_salary_slips_salary_id_salary FOREIGN KEY(salary_id) REFERENCES salary (id)
);

-- saved_citations ---------------------------------------------
CREATE TABLE saved_citations (
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME DEFAULT (CURRENT_TIMESTAMP) NOT NULL, 
	updated_at DATETIME DEFAULT (CURRENT_TIMESTAMP) NOT NULL, 
	is_deleted BOOLEAN DEFAULT 'false' NOT NULL, 
	deleted_at DATETIME, 
	student_id VARCHAR(36) NOT NULL, 
	case_name VARCHAR(255) NOT NULL, 
	citation_text VARCHAR(255) NOT NULL, 
	note TEXT, 
	CONSTRAINT pk_saved_citations PRIMARY KEY (id), 
	CONSTRAINT fk_saved_citations_student_id_students FOREIGN KEY(student_id) REFERENCES students (id)
);

-- sections ----------------------------------------------------
CREATE TABLE sections (
	course_id VARCHAR(36) NOT NULL, 
	section_name VARCHAR(32) NOT NULL, 
	faculty_id VARCHAR(36), 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME NOT NULL, 
	updated_at DATETIME NOT NULL, 
	is_deleted BOOLEAN NOT NULL, 
	deleted_at DATETIME, 
	CONSTRAINT pk_sections PRIMARY KEY (id), 
	CONSTRAINT fk_sections_course_id_courses FOREIGN KEY(course_id) REFERENCES courses (id), 
	CONSTRAINT fk_sections_faculty_id_users FOREIGN KEY(faculty_id) REFERENCES users (id)
);

-- session_summaries -------------------------------------------
CREATE TABLE session_summaries (
	faculty_id VARCHAR(36) NOT NULL, 
	section_id VARCHAR(36) NOT NULL, 
	subject_code VARCHAR(32) NOT NULL, 
	topic_covered VARCHAR(255) NOT NULL, 
	subtopic_covered VARCHAR(255), 
	teaching_method VARCHAR(128) NOT NULL, 
	resources_used JSON, 
	remarks TEXT, 
	date DATE NOT NULL, 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME NOT NULL, 
	updated_at DATETIME NOT NULL, 
	is_deleted BOOLEAN NOT NULL, 
	deleted_at DATETIME, 
	CONSTRAINT pk_session_summaries PRIMARY KEY (id), 
	CONSTRAINT fk_session_summaries_faculty_id_users FOREIGN KEY(faculty_id) REFERENCES users (id), 
	CONSTRAINT fk_session_summaries_section_id_sections FOREIGN KEY(section_id) REFERENCES sections (id)
);

-- staff_attendance --------------------------------------------
CREATE TABLE staff_attendance (
	faculty_id VARCHAR(36) NOT NULL, 
	date DATE NOT NULL, 
	status VARCHAR(64) NOT NULL, 
	check_in VARCHAR(32), 
	check_out VARCHAR(32), 
	working_hours NUMERIC(5, 2), 
	source VARCHAR(64) NOT NULL, 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME NOT NULL, 
	updated_at DATETIME NOT NULL, 
	is_deleted BOOLEAN NOT NULL, 
	deleted_at DATETIME, 
	CONSTRAINT pk_staff_attendance PRIMARY KEY (id), 
	CONSTRAINT fk_staff_attendance_faculty_id_users FOREIGN KEY(faculty_id) REFERENCES users (id)
);

-- student_interactions ----------------------------------------
CREATE TABLE student_interactions (
	faculty_id VARCHAR(36) NOT NULL, 
	section_id VARCHAR(36) NOT NULL, 
	type VARCHAR(32) NOT NULL, 
	question_text TEXT NOT NULL, 
	options JSON, 
	responses_count INTEGER NOT NULL, 
	is_active BOOLEAN NOT NULL, 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME NOT NULL, 
	updated_at DATETIME NOT NULL, 
	is_deleted BOOLEAN NOT NULL, 
	deleted_at DATETIME, 
	CONSTRAINT pk_student_interactions PRIMARY KEY (id), 
	CONSTRAINT fk_student_interactions_faculty_id_users FOREIGN KEY(faculty_id) REFERENCES users (id), 
	CONSTRAINT fk_student_interactions_section_id_sections FOREIGN KEY(section_id) REFERENCES sections (id)
);

-- student_loans -----------------------------------------------
CREATE TABLE student_loans (
	student_id VARCHAR(36) NOT NULL, 
	bank VARCHAR(128) NOT NULL, 
	branch VARCHAR(128) NOT NULL, 
	sanctioned NUMERIC(12, 2) NOT NULL, 
	interest_rate NUMERIC(5, 2) NOT NULL, 
	emi NUMERIC(12, 2) NOT NULL, 
	outstanding NUMERIC(12, 2) NOT NULL, 
	status VARCHAR(32) DEFAULT 'ACTIVE' NOT NULL, 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME NOT NULL, 
	updated_at DATETIME NOT NULL, 
	is_deleted BOOLEAN NOT NULL, 
	deleted_at DATETIME, 
	CONSTRAINT pk_student_loans PRIMARY KEY (id), 
	CONSTRAINT fk_student_loans_student_id_students FOREIGN KEY(student_id) REFERENCES students (id)
);

-- students ----------------------------------------------------
CREATE TABLE "students" (
	user_id VARCHAR(36) NOT NULL, 
	roll_no VARCHAR(64) NOT NULL, 
	department_id VARCHAR(36) NOT NULL, 
	semester INTEGER NOT NULL, 
	batch_year INTEGER NOT NULL, 
	regulation_id VARCHAR(36), 
	quota VARCHAR(64), 
	certifications JSON, 
	internships JSON, 
	sports_records JSON, 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME NOT NULL, 
	updated_at DATETIME NOT NULL, 
	is_deleted BOOLEAN NOT NULL, 
	deleted_at DATETIME, scholarship_type_id VARCHAR(128), mentor_id VARCHAR(36), full_name VARCHAR(128), date_of_birth DATE, gender VARCHAR(16), blood_group VARCHAR(8), nationality VARCHAR(64), mobile_number VARCHAR(20), current_address VARCHAR(256), permanent_address VARCHAR(256), aadhaar_number VARCHAR(20), passport_number VARCHAR(20), community_category VARCHAR(64), religion VARCHAR(64), emergency_contact_name VARCHAR(128), emergency_contact_relationship VARCHAR(64), emergency_contact_number VARCHAR(20), father_name VARCHAR(128), father_occupation VARCHAR(128), father_mobile VARCHAR(20), father_email VARCHAR(128), father_office_address VARCHAR(256), mother_name VARCHAR(128), mother_occupation VARCHAR(128), mother_mobile VARCHAR(20), mother_email VARCHAR(128), mother_office_address VARCHAR(256), parent_annual_income VARCHAR(64), languages_known JSON, hobbies_interests JSON, special_skills JSON, medical_info VARCHAR(256), cgpa NUMERIC(3, 2), skills JSON, degree_id VARCHAR(36), moot_courts JSON, profile_photo_url VARCHAR(1000), section_id VARCHAR(36), verification_status VARCHAR(64) DEFAULT 'DRAFT', staff_remarks VARCHAR(500), hod_remarks VARCHAR(500), document_aadhaar_url VARCHAR(1000), document_community_url VARCHAR(1000), document_tc_url VARCHAR(1000), document_other_url VARCHAR(1000), edit_request_status VARCHAR(64), edit_request_reason VARCHAR(1000), 
	CONSTRAINT pk_students PRIMARY KEY (id), 
	CONSTRAINT uq_students_roll_no UNIQUE (roll_no), 
	CONSTRAINT fk_students_department_id_departments FOREIGN KEY(department_id) REFERENCES departments (id), 
	CONSTRAINT uq_students_user_id UNIQUE (user_id), 
	CONSTRAINT fk_students_user_id_users FOREIGN KEY(user_id) REFERENCES users (id), 
	CONSTRAINT fk_students_regulation_id_regulations FOREIGN KEY(regulation_id) REFERENCES regulations (id)
);

-- study_materials ---------------------------------------------
CREATE TABLE "study_materials" (
	section_id VARCHAR(36) NOT NULL, 
	faculty_id VARCHAR(36) NOT NULL, 
	title VARCHAR(255) NOT NULL, 
	type VARCHAR(64) NOT NULL, 
	file_url VARCHAR(512) NOT NULL, 
	is_verified BOOLEAN NOT NULL, 
	status VARCHAR(64) NOT NULL, 
	comments VARCHAR(512), 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME NOT NULL, 
	updated_at DATETIME NOT NULL, 
	is_deleted BOOLEAN NOT NULL, 
	deleted_at DATETIME, approved_by VARCHAR(36), approved_date VARCHAR(64), rejected_by VARCHAR(36), rejected_date VARCHAR(64), rejection_remarks VARCHAR(2048), 
	CONSTRAINT pk_study_materials PRIMARY KEY (id), 
	CONSTRAINT fk_study_materials_section_id_sections FOREIGN KEY(section_id) REFERENCES sections (id), 
	CONSTRAINT fk_study_materials_faculty_id_users FOREIGN KEY(faculty_id) REFERENCES users (id)
);

-- subject_allocations -----------------------------------------
CREATE TABLE subject_allocations (
	academic_year_id VARCHAR(36) NOT NULL, 
	course_id VARCHAR(36) NOT NULL, 
	section_id VARCHAR(36) NOT NULL, 
	faculty_id VARCHAR(36) NOT NULL, 
	department_id VARCHAR(36) NOT NULL, 
	semester INTEGER, 
	allocated_by VARCHAR(36), 
	is_active BOOLEAN NOT NULL, 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME NOT NULL, 
	updated_at DATETIME NOT NULL, 
	is_deleted BOOLEAN NOT NULL, 
	deleted_at DATETIME, 
	CONSTRAINT pk_subject_allocations PRIMARY KEY (id), 
	CONSTRAINT fk_subject_allocations_faculty_id_users FOREIGN KEY(faculty_id) REFERENCES users (id), 
	CONSTRAINT fk_subject_allocations_section_id_sections FOREIGN KEY(section_id) REFERENCES sections (id), 
	CONSTRAINT fk_subject_allocations_department_id_departments FOREIGN KEY(department_id) REFERENCES departments (id), 
	CONSTRAINT fk_subject_allocations_course_id_courses FOREIGN KEY(course_id) REFERENCES courses (id), 
	CONSTRAINT fk_subject_allocations_allocated_by_users FOREIGN KEY(allocated_by) REFERENCES users (id), 
	CONSTRAINT fk_subject_allocations_academic_year_id_academic_years FOREIGN KEY(academic_year_id) REFERENCES academic_years (id)
);

-- substitution_allocations ------------------------------------
CREATE TABLE substitution_allocations (
	absence_id VARCHAR(36) NOT NULL, 
	timetable_id VARCHAR(36) NOT NULL, 
	date DATE NOT NULL, 
	substitute_faculty_id VARCHAR(36), 
	status VARCHAR(19) NOT NULL, 
	allocation_method VARCHAR(9), 
	completed_at DATETIME, 
	remarks VARCHAR(1024), 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME NOT NULL, 
	updated_at DATETIME NOT NULL, 
	is_deleted BOOLEAN NOT NULL, 
	deleted_at DATETIME, 
	CONSTRAINT pk_substitution_allocations PRIMARY KEY (id), 
	CONSTRAINT fk_substitution_allocations_absence_id_faculty_absences FOREIGN KEY(absence_id) REFERENCES faculty_absences (id), 
	CONSTRAINT fk_substitution_allocations_substitute_faculty_id_users FOREIGN KEY(substitute_faculty_id) REFERENCES users (id), 
	CONSTRAINT fk_substitution_allocations_timetable_id_timetable FOREIGN KEY(timetable_id) REFERENCES timetable (id)
);

-- system_setting_history --------------------------------------
CREATE TABLE system_setting_history (
	setting_id VARCHAR(36) NOT NULL, 
	user_id VARCHAR(36), 
	field_name VARCHAR(128) NOT NULL, 
	old_value VARCHAR(4000), 
	new_value VARCHAR(4000), 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME NOT NULL, 
	updated_at DATETIME NOT NULL, 
	is_deleted BOOLEAN NOT NULL, 
	deleted_at DATETIME, 
	CONSTRAINT pk_system_setting_history PRIMARY KEY (id), 
	CONSTRAINT fk_system_setting_history_setting_id_system_settings FOREIGN KEY(setting_id) REFERENCES system_settings (id), 
	CONSTRAINT fk_system_setting_history_user_id_users FOREIGN KEY(user_id) REFERENCES users (id)
);

-- system_settings ---------------------------------------------
CREATE TABLE system_settings (
	college_name VARCHAR(255) NOT NULL, 
	logo_url VARCHAR(1000), 
	address VARCHAR(1000), 
	affiliation_number VARCHAR(128), 
	aicte_ugc_code VARCHAR(128), 
	accreditation_body VARCHAR(128), 
	bank_name VARCHAR(255), 
	bank_account_no VARCHAR(128), 
	bank_ifsc VARCHAR(64), 
	bank_branch VARCHAR(255), 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME NOT NULL, 
	updated_at DATETIME NOT NULL, 
	is_deleted BOOLEAN NOT NULL, 
	deleted_at DATETIME, 
	CONSTRAINT pk_system_settings PRIMARY KEY (id)
);

-- timetable ---------------------------------------------------
CREATE TABLE timetable (
	section_id VARCHAR(36) NOT NULL, 
	subject_id VARCHAR(36) NOT NULL, 
	faculty_id VARCHAR(36) NOT NULL, 
	room VARCHAR(64) NOT NULL, 
	weekday VARCHAR(9) NOT NULL, 
	start_time TIME NOT NULL, 
	end_time TIME NOT NULL, 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME NOT NULL, 
	updated_at DATETIME NOT NULL, 
	is_deleted BOOLEAN NOT NULL, 
	deleted_at DATETIME, 
	CONSTRAINT pk_timetable PRIMARY KEY (id), 
	CONSTRAINT fk_timetable_faculty_id_users FOREIGN KEY(faculty_id) REFERENCES users (id), 
	CONSTRAINT fk_timetable_section_id_sections FOREIGN KEY(section_id) REFERENCES sections (id), 
	CONSTRAINT fk_timetable_subject_id_courses FOREIGN KEY(subject_id) REFERENCES courses (id)
);

-- timetable_approvals -----------------------------------------
CREATE TABLE timetable_approvals (
	timetable_id VARCHAR(36) NOT NULL, 
	status VARCHAR(17) NOT NULL, 
	approved_by VARCHAR(36), 
	comments VARCHAR(1000), 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME NOT NULL, 
	updated_at DATETIME NOT NULL, 
	is_deleted BOOLEAN NOT NULL, 
	deleted_at DATETIME, rejection_remarks VARCHAR(2048), approved_date VARCHAR(64), rejected_by VARCHAR(36), rejected_date VARCHAR(64), 
	CONSTRAINT pk_timetable_approvals PRIMARY KEY (id), 
	CONSTRAINT fk_timetable_approvals_approved_by_users FOREIGN KEY(approved_by) REFERENCES users (id), 
	CONSTRAINT fk_timetable_approvals_timetable_id_timetable FOREIGN KEY(timetable_id) REFERENCES timetable (id)
);

-- transport_passes --------------------------------------------
CREATE TABLE transport_passes (
	route_id VARCHAR(36) NOT NULL, 
	student_id VARCHAR(36) NOT NULL, 
	pickup_point VARCHAR(255), 
	valid_from DATE NOT NULL, 
	valid_to DATE NOT NULL, 
	fare_paid NUMERIC(12, 2), 
	status VARCHAR(16) NOT NULL, 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, 
	updated_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, 
	is_deleted BOOLEAN DEFAULT 'false' NOT NULL, 
	deleted_at DATETIME, 
	CONSTRAINT pk_transport_passes PRIMARY KEY (id), 
	CONSTRAINT fk_transport_passes_route_id_transport_routes FOREIGN KEY(route_id) REFERENCES transport_routes (id), 
	CONSTRAINT fk_transport_passes_student_id_students FOREIGN KEY(student_id) REFERENCES students (id)
);

-- transport_routes --------------------------------------------
CREATE TABLE transport_routes (
	name VARCHAR(255) NOT NULL, 
	code VARCHAR(32) NOT NULL, 
	start_point VARCHAR(255) NOT NULL, 
	end_point VARCHAR(255) NOT NULL, 
	distance_km NUMERIC(8, 2), 
	fare NUMERIC(12, 2), 
	stops VARCHAR(2000), 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, 
	updated_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, 
	is_deleted BOOLEAN DEFAULT 'false' NOT NULL, 
	deleted_at DATETIME, 
	CONSTRAINT pk_transport_routes PRIMARY KEY (id), 
	CONSTRAINT uq_transport_route_code UNIQUE (code)
);

-- transport_vehicles ------------------------------------------
CREATE TABLE transport_vehicles (
	registration_no VARCHAR(32) NOT NULL, 
	vehicle_type VARCHAR(64), 
	capacity INTEGER NOT NULL, 
	driver_name VARCHAR(128), 
	driver_phone VARCHAR(20), 
	route_id VARCHAR(36), 
	status VARCHAR(16) NOT NULL, 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, 
	updated_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, 
	is_deleted BOOLEAN DEFAULT 'false' NOT NULL, 
	deleted_at DATETIME, 
	CONSTRAINT pk_transport_vehicles PRIMARY KEY (id), 
	CONSTRAINT fk_transport_vehicles_route_id_transport_routes FOREIGN KEY(route_id) REFERENCES transport_routes (id), 
	CONSTRAINT uq_transport_vehicle_reg UNIQUE (registration_no)
);

-- users -------------------------------------------------------
CREATE TABLE users (
	email VARCHAR(255) NOT NULL, 
	phone VARCHAR(32), 
	full_name VARCHAR(255) NOT NULL, 
	hashed_password VARCHAR(255) NOT NULL, 
	role VARCHAR(11) NOT NULL, 
	is_active BOOLEAN NOT NULL, 
	department_id VARCHAR(36), 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME NOT NULL, 
	updated_at DATETIME NOT NULL, 
	is_deleted BOOLEAN NOT NULL, 
	deleted_at DATETIME, pending_email VARCHAR(255), email_change_token VARCHAR(128), email_change_token_expires_at DATETIME, email_notifications_enabled BOOLEAN DEFAULT (1) NOT NULL, 
	CONSTRAINT pk_users PRIMARY KEY (id), 
	CONSTRAINT fk_users_department_id_departments FOREIGN KEY(department_id) REFERENCES departments (id), 
	CONSTRAINT uq_users_phone UNIQUE (phone)
);

-- working_day_config ------------------------------------------
CREATE TABLE working_day_config (
	month INTEGER NOT NULL, 
	year INTEGER NOT NULL, 
	total_working_days INTEGER NOT NULL, 
	overrides_json TEXT, 
	id VARCHAR(36) NOT NULL, 
	created_at DATETIME NOT NULL, 
	updated_at DATETIME NOT NULL, 
	is_deleted BOOLEAN NOT NULL, 
	deleted_at DATETIME, 
	CONSTRAINT pk_working_day_config PRIMARY KEY (id), 
	CONSTRAINT uq_working_day_config_month_year UNIQUE (month, year)
);


-- Indexes -------------------------------------------------------

CREATE INDEX ix_budget_expenses_line_item_id ON budget_expenses (line_item_id);
CREATE INDEX ix_budget_line_items_fiscal_year ON budget_line_items (fiscal_year);
CREATE INDEX ix_chat_messages_session_id ON chat_messages (session_id);
CREATE INDEX ix_chat_sessions_user_id ON chat_sessions (user_id);
CREATE INDEX ix_hostel_allocations_room_id ON hostel_allocations (room_id);
CREATE INDEX ix_hostel_allocations_status ON hostel_allocations (status);
CREATE INDEX ix_hostel_allocations_student_id ON hostel_allocations (student_id);
CREATE INDEX ix_hostel_blocks_code ON hostel_blocks (code);
CREATE INDEX ix_hostel_rooms_block_id ON hostel_rooms (block_id);
CREATE INDEX ix_inventory_items_category ON inventory_items (category);
CREATE INDEX ix_inventory_items_code ON inventory_items (code);
CREATE INDEX ix_inventory_transactions_item_id ON inventory_transactions (item_id);
CREATE INDEX ix_library_books_accession_no ON library_books (accession_no);
CREATE INDEX ix_library_books_category ON library_books (category);
CREATE INDEX ix_library_books_title ON library_books (title);
CREATE INDEX ix_library_issues_book_id ON library_issues (book_id);
CREATE INDEX ix_library_issues_member_id ON library_issues (member_id);
CREATE INDEX ix_library_issues_status ON library_issues (status);
CREATE UNIQUE INDEX ix_password_reset_tokens_token ON password_reset_tokens (token);
CREATE INDEX ix_transport_passes_route_id ON transport_passes (route_id);
CREATE INDEX ix_transport_passes_status ON transport_passes (status);
CREATE INDEX ix_transport_passes_student_id ON transport_passes (student_id);
CREATE INDEX ix_transport_routes_code ON transport_routes (code);
CREATE INDEX ix_transport_vehicles_registration_no ON transport_vehicles (registration_no);
CREATE INDEX ix_transport_vehicles_route_id ON transport_vehicles (route_id);
CREATE INDEX ix_transport_vehicles_status ON transport_vehicles (status);
CREATE UNIQUE INDEX ix_users_email ON users (email);
CREATE INDEX ix_users_email_change_token ON users (email_change_token);