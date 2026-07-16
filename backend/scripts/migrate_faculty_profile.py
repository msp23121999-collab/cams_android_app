import asyncio
from sqlalchemy.ext.asyncio import create_async_engine
from sqlalchemy import text

async def main():
    engine = create_async_engine('postgresql+asyncpg://cams:cams%402026@43.205.177.13:5432/lawcollege')
    async with engine.connect() as conn:
        print("Running migrations on table: faculty_profiles...")
        
        # We will add columns if they don't exist
        queries = [
            "ALTER TABLE faculty_profiles ADD COLUMN IF NOT EXISTS employee_code VARCHAR(64)",
            "ALTER TABLE faculty_profiles ADD COLUMN IF NOT EXISTS gender VARCHAR(16)",
            "ALTER TABLE faculty_profiles ADD COLUMN IF NOT EXISTS date_of_birth DATE",
            "ALTER TABLE faculty_profiles ADD COLUMN IF NOT EXISTS blood_group VARCHAR(16)",
            "ALTER TABLE faculty_profiles ADD COLUMN IF NOT EXISTS marital_status VARCHAR(32)",
            "ALTER TABLE faculty_profiles ADD COLUMN IF NOT EXISTS nationality VARCHAR(64)",
            "ALTER TABLE faculty_profiles ADD COLUMN IF NOT EXISTS alternate_phone VARCHAR(32)",
            "ALTER TABLE faculty_profiles ADD COLUMN IF NOT EXISTS personal_email VARCHAR(255)",
            "ALTER TABLE faculty_profiles ADD COLUMN IF NOT EXISTS current_address VARCHAR(512)",
            "ALTER TABLE faculty_profiles ADD COLUMN IF NOT EXISTS permanent_address VARCHAR(512)",
            "ALTER TABLE faculty_profiles ADD COLUMN IF NOT EXISTS city VARCHAR(128)",
            "ALTER TABLE faculty_profiles ADD COLUMN IF NOT EXISTS state VARCHAR(128)",
            "ALTER TABLE faculty_profiles ADD COLUMN IF NOT EXISTS pincode VARCHAR(16)",
            "ALTER TABLE faculty_profiles ADD COLUMN IF NOT EXISTS profile_photo_url VARCHAR(512)",
            "ALTER TABLE faculty_profiles ADD COLUMN IF NOT EXISTS faculty_type VARCHAR(64)",
            "ALTER TABLE faculty_profiles ADD COLUMN IF NOT EXISTS employment_category VARCHAR(64)",
            "ALTER TABLE faculty_profiles ADD COLUMN IF NOT EXISTS date_of_joining DATE",
            "ALTER TABLE faculty_profiles ADD COLUMN IF NOT EXISTS employment_status VARCHAR(64) DEFAULT 'Active'",
            "ALTER TABLE faculty_profiles ADD COLUMN IF NOT EXISTS approval_status VARCHAR(64) DEFAULT 'PENDING_PRINCIPAL'",
            "ALTER TABLE faculty_profiles ADD COLUMN IF NOT EXISTS reporting_hod_id VARCHAR(36)",
            "ALTER TABLE faculty_profiles ADD COLUMN IF NOT EXISTS reporting_principal_id VARCHAR(36)",
            "ALTER TABLE faculty_profiles ADD COLUMN IF NOT EXISTS confirmation_date DATE",
            "ALTER TABLE faculty_profiles ADD COLUMN IF NOT EXISTS educational_qualifications JSON",
            "ALTER TABLE faculty_profiles ADD COLUMN IF NOT EXISTS experience_details JSON",
            "ALTER TABLE faculty_profiles ADD COLUMN IF NOT EXISTS academic_responsibilities JSON",
            "ALTER TABLE faculty_profiles ADD COLUMN IF NOT EXISTS certifications_achievements JSON",
            "ALTER TABLE faculty_profiles ADD COLUMN IF NOT EXISTS promotion_history JSON",
            "ALTER TABLE faculty_profiles ADD COLUMN IF NOT EXISTS increment_history JSON",
            "ALTER TABLE faculty_profiles ADD COLUMN IF NOT EXISTS documents_repository JSON",
            "ALTER TABLE faculty_profiles ADD COLUMN IF NOT EXISTS notification_preferences JSON",
            
            # Faculty Research additions
            "ALTER TABLE faculty_research ADD COLUMN IF NOT EXISTS publisher VARCHAR(255)",
            "ALTER TABLE faculty_research ADD COLUMN IF NOT EXISTS publication_date DATE",
            "ALTER TABLE faculty_research ADD COLUMN IF NOT EXISTS isbn_issn VARCHAR(64)",
            "ALTER TABLE faculty_research ADD COLUMN IF NOT EXISTS research_type VARCHAR(64)"
        ]
        
        for q in queries:
            try:
                await conn.execute(text(q))
                print(f"Successfully executed: {q[:50]}...")
            except Exception as e:
                print(f"Error executing query: {q}\nException: {e}")
        
        await conn.commit()
        print("Migration completed successfully!")

if __name__ == '__main__':
    asyncio.run(main())
