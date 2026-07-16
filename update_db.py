with open("app/src/main/java/com/example/core/database/CamsDatabase.kt") as f:
    text = f.read()

uncomment_list = [
    "LeavesEntity", "NoticesEntity", "GrievancesEntity", 
    "MarksEntity", "StudyMaterialsEntity", "SalaryEntity", 
    "StaffAttendanceEntity", "NotificationsEntity"
]

for entity in uncomment_list:
    text = text.replace(f"// {entity}::class", f"{entity}::class")
    
    dao = entity.replace("Entity", "Dao")
    text = text.replace(f"// abstract fun {dao[:1].lower() + dao[1:]}()", f"abstract fun {dao[:1].lower() + dao[1:]}()")

with open("app/src/main/java/com/example/core/database/CamsDatabase.kt", "w") as f:
    f.write(text)
print("Updated CamsDatabase")
