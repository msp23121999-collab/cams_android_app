import psycopg2
import uuid

def main():
    conn = psycopg2.connect("postgresql://cams:cams%402026@43.205.177.13:5432/lawcollege")
    cur = conn.cursor()
    try:
        # 1. Get LLB (L1) and BA LLB (L2) Department IDs
        cur.execute("SELECT id FROM departments WHERE code = 'L1';")
        llb_dept_id = cur.fetchone()[0]
        cur.execute("SELECT id FROM departments WHERE code = 'L2';")
        ba_llb_dept_id = cur.fetchone()[0]
        print(f"LLB ID: {llb_dept_id}, BA LLB ID: {ba_llb_dept_id}")

        # 2. Get LL.B. configurations (to copy config)
        cur.execute("SELECT credit_pattern, exam_formula, grade_boundaries FROM degrees WHERE code = 'LL.B.' LIMIT 1;")
        llb_row = cur.fetchone()
        llb_credit = llb_row[0]
        llb_formula = llb_row[1]
        llb_grades = llb_row[2]

        # Get B.A. LL.B. configurations
        cur.execute("SELECT credit_pattern, exam_formula, grade_boundaries FROM degrees WHERE code = 'B.A. LL.B.' LIMIT 1;")
        ba_row = cur.fetchone()
        ba_credit = ba_row[0]
        ba_formula = ba_row[1]
        ba_grades = ba_row[2]

        # 3. Modify Template B.A. LL.B. (c7f571f8-0000-4d56-85b4-6d68fc9c6789)
        cur.execute("""
            UPDATE degrees 
            SET name = 'B.A. LL.B. (Integrated)', applicable_batch = 'All', program_level = 'INTEGRATED', duration_years = 5
            WHERE id = 'c7f571f8-0000-4d56-85b4-6d68fc9c6789';
        """)
        print("Updated template B.A. LL.B.")

        # 4. Update the 2023 batch degree (31b29971-b254-4488-96c6-5638fa323d72) to be B.A. LL.B. (2026-2031)
        cur.execute("""
            UPDATE degrees 
            SET name = 'B.A. LL.B. (2026-2031)', applicable_batch = '2026-2031', program_level = 'INTEGRATED', duration_years = 5
            WHERE id = '31b29971-b254-4488-96c6-5638fa323d72';
        """)
        print("Updated 2023 batch degree to B.A. LL.B. (2026-2031)")

        # 5. Insert B.A. LL.B. (2025-2030) batch profile if not exists
        cur.execute("SELECT id FROM degrees WHERE code = 'B.A. LL.B.' AND applicable_batch = '2025-2030';")
        ba_2025_row = cur.fetchone()
        if not ba_2025_row:
            ba_2025_id = str(uuid.uuid4())
            cur.execute("""
                INSERT INTO degrees (id, name, code, applicable_batch, program_level, duration_years, dept_id, credit_pattern, exam_formula, grade_boundaries, passing_marks, created_at, updated_at, is_deleted)
                VALUES (%s, 'B.A. LL.B. (2025-2030)', 'B.A. LL.B.', '2025-2030', 'INTEGRATED', 5, %s, %s, %s, %s, 40, NOW(), NOW(), false);
            """, (ba_2025_id, ba_llb_dept_id, ba_credit, ba_formula, ba_grades))
            print(f"Created B.A. LL.B. (2025-2030) batch profile: {ba_2025_id}")
        else:
            ba_2025_id = ba_2025_row[0]
            print(f"B.A. LL.B. (2025-2030) already exists: {ba_2025_id}")

        # 6. Insert LL.B. (2026-2029) batch profile if not exists
        cur.execute("SELECT id FROM degrees WHERE code = 'LL.B.' AND applicable_batch = '2026-2029';")
        llb_2026_row = cur.fetchone()
        if not llb_2026_row:
            llb_2026_id = str(uuid.uuid4())
            cur.execute("""
                INSERT INTO degrees (id, name, code, applicable_batch, program_level, duration_years, dept_id, credit_pattern, exam_formula, grade_boundaries, passing_marks, created_at, updated_at, is_deleted)
                VALUES (%s, 'LL.B. (2026-2029)', 'LL.B.', '2026-2029', 'UG', 3, %s, %s, %s, %s, 40, NOW(), NOW(), false);
            """, (llb_2026_id, llb_dept_id, llb_credit, llb_formula, llb_grades))
            print(f"Created LL.B. (2026-2029) batch profile: {llb_2026_id}")
        else:
            llb_2026_id = llb_2026_row[0]
            print(f"LL.B. (2026-2029) already exists: {llb_2026_id}")

        # Get LL.B. 2025-2028 ID
        cur.execute("SELECT id FROM degrees WHERE code = 'LL.B.' AND applicable_batch = '2025-2028';")
        llb_2025_id = cur.fetchone()[0]

        # B.A. LL.B. 2026-2031 ID
        ba_2026_id = '31b29971-b254-4488-96c6-5638fa323d72'

        # 7. Clean up Academic Years
        print("\nCleaning and re-creating Academic Years...")
        cur.execute("DELETE FROM class_advisors;")
        cur.execute("DELETE FROM subject_allocations;")
        cur.execute("DELETE FROM academic_years;")
        
        # Insert 4 clean academic years
        ay_records = [
            (str(uuid.uuid4()), '2025-2028', '2025-06-01', '2028-05-31', llb_2025_id, '2025-2028'),
            (str(uuid.uuid4()), '2026-2029', '2026-06-01', '2029-05-31', llb_2026_id, '2026-2029'),
            (str(uuid.uuid4()), '2025-2030', '2025-06-01', '2030-05-31', ba_2025_id, '2025-2030'),
            (str(uuid.uuid4()), '2026-2031', '2026-06-01', '2031-05-31', ba_2026_id, '2026-2031'),
        ]
        for aid, name, start_d, end_d, deg_id, batch in ay_records:
            cur.execute("""
                INSERT INTO academic_years (id, name, start_date, end_date, degree_id, batch, current_semester, is_semester_open, is_exam_period, is_active, created_at, updated_at, is_deleted)
                VALUES (%s, %s, %s, %s, %s, %s, 1, true, false, true, NOW(), NOW(), false);
            """, (aid, name, start_d, end_d, deg_id, batch))
            print(f"Created academic year: {name}")

        # 8. Re-map Students
        print("\nRe-mapping students to correct degree batches...")
        student_mappings = [
            ('LAW-2026-002', llb_2026_id, 2026), # Ramesh Kumar (LL.B. 2026-2029)
            ('LAW-2026-004', llb_2026_id, 2026), # Bala Subramanien (LL.B. 2026-2029)
            ('LAW-2025-002', llb_2025_id, 2025), # Nishanth Raghavan (LL.B. 2025-2028)
            
            ('LAW-2026-003', ba_2026_id, 2026),  # Gokul Sharma (B.A. LL.B. 2026-2031)
            ('LAW-2026-001', ba_2026_id, 2026),  # Priya Lakshmi (B.A. LL.B. 2026-2031)
            ('76', ba_2026_id, 2026),            # Mohan (B.A. LL.B. 2026-2031)
            ('LAW-2025-001', ba_2025_id, 2025),  # Harini Sekar (B.A. LL.B. 2025-2030)
        ]
        for roll, deg_id, batch_yr in student_mappings:
            cur.execute("""
                UPDATE students 
                SET degree_id = %s, batch_year = %s 
                WHERE roll_no = %s;
            """, (deg_id, batch_yr, roll))
            print(f"Mapped student roll '{roll}' to degree {deg_id} and batch year {batch_yr}")

        conn.commit()
        print("\nAll database corrections committed successfully!")
    except Exception as e:
        conn.rollback()
        print(f"Transaction failed, rolled back: {e}")
        raise e
    finally:
        cur.close()
        conn.close()

if __name__ == '__main__':
    main()
