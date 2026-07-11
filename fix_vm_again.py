with open("app/src/main/java/com/example/features/student/providers/DashboardViewModel.kt", "r") as f:
    lines = f.readlines()

with open("app/src/main/java/com/example/features/student/providers/DashboardViewModel.kt", "w") as f:
    for i, line in enumerate(lines):
        if i not in [75, 76]:
            f.write(line)

