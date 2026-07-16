import re

target = 'app/src/main/java/com/example/features/auth/LoginScreen.kt'
with open(target, 'r') as f:
    content = f.read()

# Add a parameter onDebugTap: () -> Unit = {} to LoginScreen
content = content.replace('fun LoginScreen(', 'fun LoginScreen(\n    onDebugTap: () -> Unit = {},')

# Find the header column and add a tap counter. 
# We need to add tap logic in the compose function.
state_code = """
    var tapCount by remember { mutableStateOf(0) }
    var lastTapTime by remember { mutableStateOf(0L) }
"""
content = content.replace('var password by remember { mutableStateOf("") }', 'var password by remember { mutableStateOf("") }' + state_code)

# Replace the text "$role Login" with a Row containing an Icon and the text, with clickable on the Icon.
header_replacement = """                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier
                                .size(36.dp)
                                .clickable {
                                    val now = System.currentTimeMillis()
                                    if (now - lastTapTime > 500) {
                                        tapCount = 1
                                    } else {
                                        tapCount++
                                    }
                                    lastTapTime = now
                                    if (tapCount >= 5) {
                                        tapCount = 0
                                        onDebugTap()
                                    }
                                },
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.AccountBalance, contentDescription = "App Logo", tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "$role Login",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 32.sp
                            )
                        )
                    }"""
content = re.sub(r'Text\(\s*"[^"]*Login",\s*style = MaterialTheme\.typography\.headlineLarge\.copy\([^\)]+\),\s*modifier = Modifier\.padding\(horizontal = 16\.dp\)\s*\)', header_replacement, content)

with open(target, 'w') as f:
    f.write(content)

