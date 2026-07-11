with open("app/src/main/java/com/example/core/ui/CamsLayout.kt", "r") as f:
    text = f.read()

import re

# Add isOfflineMode parameter
text = text.replace("    onRetry: (() -> Unit)? = null,\n    content: @Composable ColumnScope.() -> Unit", "    onRetry: (() -> Unit)? = null,\n    isOfflineMode: Boolean = false,\n    content: @Composable ColumnScope.() -> Unit")

# Add the banner
banner = """            if (isOfflineMode) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE65100))
                        .padding(vertical = 8.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Offline Mode. Changes will be synced later.",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
"""
text = text.replace("            Box(modifier = Modifier.fillMaxSize().weight(1f, fill = false)) {", banner + "            Box(modifier = Modifier.fillMaxSize().weight(1f, fill = false)) {")

with open("app/src/main/java/com/example/core/ui/CamsLayout.kt", "w") as f:
    f.write(text)
