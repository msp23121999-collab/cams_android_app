import os
import re
import shutil

disabled_dir = "app/disabled_dao"
dao_dir = "app/src/main/java/com/example/core/database/dao"
entity_dir = "app/src/main/java/com/example/core/database/entities"
cams_db_path = "app/src/main/java/com/example/core/database/CamsDatabase.kt"
app_container_path = "app/src/main/java/com/example/core/di/AppContainer.kt"

os.makedirs(dao_dir, exist_ok=True)
os.makedirs(entity_dir, exist_ok=True)

dao_files = [f for f in os.listdir(disabled_dir) if f.endswith(".kt")]

daos_info = []

for dao_file in dao_files:
    dao_path = os.path.join(disabled_dir, dao_file)
    with open(dao_path, "r") as f:
        dao_content = f.read()
    
    dao_name = dao_file.replace(".kt", "")
    entity_name = dao_name.replace("Dao", "Entity")
    
    # Try to find table name
    match = re.search(r'SELECT \* FROM ([a-zA-Z_0-9]+)', dao_content)
    if match:
        table_name = match.group(1)
    else:
        # Fallback to snake_case of entity_name without Entity
        name_no_entity = dao_name.replace("Dao", "")
        table_name = re.sub(r'(?<!^)(?=[A-Z])', '_', name_no_entity).lower()
        
    daos_info.append({
        "dao_name": dao_name,
        "entity_name": entity_name,
        "table_name": table_name,
        "func_name": dao_name[0].lower() + dao_name[1:]
    })

    # Move DAO
    shutil.move(dao_path, os.path.join(dao_dir, dao_file))

    # Create Entity if not exists
    entity_path = os.path.join(entity_dir, f"{entity_name}.kt")
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

# Fix CamsDatabase.kt
with open(cams_db_path, "r") as f:
    db_content = f.read()

for info in daos_info:
    entity = info["entity_name"]
    func = info["func_name"]
    dao = info["dao_name"]
    # Uncomment entity
    db_content = db_content.replace(f"// {entity}::class", f"{entity}::class")
    db_content = db_content.replace(f"// {entity}", f"{entity}")
    # Uncomment func
    db_content = db_content.replace(f"// abstract fun {func}(): {dao}", f"abstract fun {func}(): {dao}")

with open(cams_db_path, "w") as f:
    f.write(db_content)

# Now AppContainer.kt
with open(app_container_path, "r") as f:
    container_content = f.read()

# Collect missing ones
missing_interface_props = []
missing_impl_props = []

# All DAOs we know about (including ones that were not disabled but are in the db)
import glob
all_daos = [os.path.basename(f).replace(".kt", "") for f in glob.glob(os.path.join(dao_dir, "*.kt"))]

for dao_name in all_daos:
    func_name = dao_name[0].lower() + dao_name[1:]
    # Check if already in interface
    if f"val {func_name}: {dao_name}" not in container_content:
        missing_interface_props.append(f"    val {func_name}: {dao_name}")
    
    # Check if already in impl
    if f"override val {func_name}: {dao_name}" not in container_content:
        missing_impl_props.append(f"    override val {func_name}: {dao_name} by lazy {{ database.{func_name}() }}")

# Insert into interface AppContainer
interface_insert_pos = container_content.find("fun provideAdminUserViewModelFactory")
if interface_insert_pos != -1:
    container_content = container_content[:interface_insert_pos] + "\n".join(missing_interface_props) + "\n    " + container_content[interface_insert_pos:]

# Insert into class DefaultAppContainer
impl_insert_pos = container_content.find("override fun provideAdminUserViewModelFactory")
if impl_insert_pos != -1:
    container_content = container_content[:impl_insert_pos] + "\n".join(missing_impl_props) + "\n    " + container_content[impl_insert_pos:]

# Add imports for DAOs
import_statements = "\n".join([f"import com.example.core.database.dao.{dao}" for dao in all_daos if f"import com.example.core.database.dao.{dao}" not in container_content])
import_pos = container_content.find("interface AppContainer")
if import_pos != -1:
    container_content = container_content[:import_pos] + import_statements + "\n\n" + container_content[import_pos:]

with open(app_container_path, "w") as f:
    f.write(container_content)

print("Done")
