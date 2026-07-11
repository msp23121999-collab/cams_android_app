target = 'app/src/main/java/com/example/features/auth/LoginScreen.kt'
with open(target, 'r') as f:
    content = f.read()

state_code = """
    var tapCount by remember { mutableStateOf(0) }
    var lastTapTime by remember { mutableStateOf(0L) }
"""
content = content.replace('var password by remember { mutableStateOf("password") }', 'var password by remember { mutableStateOf("password") }\n' + state_code)

with open(target, 'w') as f:
    f.write(content)
