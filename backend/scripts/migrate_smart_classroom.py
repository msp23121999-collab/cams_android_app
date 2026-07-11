import asyncio
from sqlalchemy.ext.asyncio import create_async_engine
from sqlalchemy import text

async def main():
    engine = create_async_engine('postgresql+asyncpg://cams:cams%402026@43.205.177.13:5432/lawcollege')
    async with engine.connect() as conn:
        print("Creating tables for Smart Classroom Module...")
        
        queries = [
            # 1. classroom_activities
            """
            CREATE TABLE IF NOT EXISTS classroom_activities (
                id VARCHAR(36) PRIMARY KEY,
                created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                is_deleted BOOLEAN DEFAULT FALSE,
                deleted_at TIMESTAMP WITH TIME ZONE,
                faculty_id VARCHAR(36) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                section_id VARCHAR(36) NOT NULL REFERENCES sections(id) ON DELETE CASCADE,
                activity_type VARCHAR(64) NOT NULL,
                topic VARCHAR(255) NOT NULL,
                duration_minutes INTEGER NOT NULL,
                remarks TEXT
            );
            """,
            # 2. student_interactions
            """
            CREATE TABLE IF NOT EXISTS student_interactions (
                id VARCHAR(36) PRIMARY KEY,
                created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                is_deleted BOOLEAN DEFAULT FALSE,
                deleted_at TIMESTAMP WITH TIME ZONE,
                faculty_id VARCHAR(36) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                section_id VARCHAR(36) NOT NULL REFERENCES sections(id) ON DELETE CASCADE,
                type VARCHAR(32) NOT NULL,
                question_text TEXT NOT NULL,
                options JSON,
                responses_count INTEGER DEFAULT 0 NOT NULL,
                is_active BOOLEAN DEFAULT TRUE NOT NULL
            );
            """,
            # 3. session_summaries
            """
            CREATE TABLE IF NOT EXISTS session_summaries (
                id VARCHAR(36) PRIMARY KEY,
                created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                is_deleted BOOLEAN DEFAULT FALSE,
                deleted_at TIMESTAMP WITH TIME ZONE,
                faculty_id VARCHAR(36) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                section_id VARCHAR(36) NOT NULL REFERENCES sections(id) ON DELETE CASCADE,
                subject_code VARCHAR(32) NOT NULL,
                topic_covered VARCHAR(255) NOT NULL,
                subtopic_covered VARCHAR(255),
                teaching_method VARCHAR(128) NOT NULL,
                resources_used JSON,
                remarks TEXT,
                date DATE NOT NULL
            );
            """
        ]
        
        for q in queries:
            try:
                await conn.execute(text(q))
                print("Executed statement successfully.")
            except Exception as e:
                print(f"Error executing statement: {e}")
                
        await conn.commit()
        print("Smart Classroom migrations completed successfully!")

if __name__ == '__main__':
    asyncio.run(main())
