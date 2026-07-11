import re

target = 'app/src/main/java/com/example/features/auth/LoginScreen.kt'
with open(target, 'r') as f:
    content = f.read()

imports_to_add = """
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.AccountBalance
"""

content = content.replace('import androidx.compose.foundation.background', 'import androidx.compose.foundation.background' + imports_to_add)

with open(target, 'w') as f:
    f.write(content)

