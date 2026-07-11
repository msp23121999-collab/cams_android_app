import os

entities_dir = "app/src/main/java/com/example/core/database/entities"
daos_dir = "app/src/main/java/com/example/core/database/dao"

entities = [
    ("LeavesEntity", "leaves"),
    ("NoticesEntity", "notices"),
    ("GrievancesEntity", "grievances"),
    ("MarksEntity", "marks"),
    ("StudyMaterialsEntity", "study_materials"),
    ("SalaryEntity", "salary"),
    ("StaffAttendanceEntity", "staff_attendance"),
    ("NotificationsEntity", "notifications")
]

for entity_name, table_name in entities:
    # Create Entity
    entity_path = os.path.join(entities_dir, f"{entity_name}.kt")
    if not os.path.exists(entity_path):
        with open(entity_path, "w") as f:
            f.write(f"""package com.example.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "{table_name}")
data class {entity_name}(
    @PrimaryKey
    val id: String,
    val dummyData: String = ""
)
""")
    
    # Create DAO
    dao_name = entity_name.replace("Entity", "Dao")
    dao_path = os.path.join(daos_dir, f"{dao_name}.kt")
    if not os.path.exists(dao_path):
        with open(dao_path, "w") as f:
            f.write(f"""package com.example.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.core.database.entities.{entity_name}

@Dao
interface {dao_name} {{
    @Query("SELECT * FROM {table_name}")
    suspend fun getAll(): List<{entity_name}>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: {entity_name})
}}
""")

print("Done")
