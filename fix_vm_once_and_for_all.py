with open("app/src/main/java/com/example/features/student/providers/DashboardViewModel.kt", "r") as f:
    text = f.read()

import re
text = re.sub(r'\}\s*\}\s*class DashboardViewModelFactory', '        }\n    }\n}\n\nclass DashboardViewModelFactory', text)

with open("app/src/main/java/com/example/features/student/providers/DashboardViewModel.kt", "w") as f:
    f.write(text)

