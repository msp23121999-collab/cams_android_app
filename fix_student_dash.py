with open("app/src/main/java/com/example/features/student/screens/StudentDashboardScreen.kt", "r") as f:
    text = f.read()

banner = """            if (uiState.isOfflineMode) {
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
text = text.replace("        Scaffold(\n            topBar = {\n                Box(", "        Scaffold(\n            topBar = {\n                Column { " + banner + """
                Box(""")
text = text.replace("                        }\n                    }\n                }\n            },\n            containerColor = MaterialTheme.colorScheme.background", "                        }\n                    }\n                }\n                }\n            },\n            containerColor = MaterialTheme.colorScheme.background")

with open("app/src/main/java/com/example/features/student/screens/StudentDashboardScreen.kt", "w") as f:
    f.write(text)
