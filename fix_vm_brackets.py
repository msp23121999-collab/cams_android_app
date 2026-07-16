with open("app/src/main/java/com/example/features/student/providers/DashboardViewModel.kt", "r") as f:
    content = f.read()

# Let's just fix it by ensuring exactly one `}` before `class DashboardViewModelFactory`
import re
content = re.sub(r'\s*class DashboardViewModelFactory', '\n    }\n}\n\nclass DashboardViewModelFactory', content)
# Now format correctly:
with open("app/src/main/java/com/example/features/student/providers/DashboardViewModel.kt", "w") as f:
    f.write(content)

