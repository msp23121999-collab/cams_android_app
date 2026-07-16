with open("app/src/main/java/com/example/features/student/widgets/StudentBaseScreen.kt", "r") as f:
    text = f.read()

text = text.replace("    floatingActionButton: @Composable (() -> Unit)? = null,\n    scrollable: Boolean = true,\n    content: @Composable ColumnScope.() -> Unit", "    floatingActionButton: @Composable (() -> Unit)? = null,\n    scrollable: Boolean = true,\n    isOfflineMode: Boolean = false,\n    content: @Composable ColumnScope.() -> Unit")
text = text.replace("            scrollable = scrollable,\n            verticalArrangement = Arrangement.spacedBy(16.dp),", "            scrollable = scrollable,\n            isOfflineMode = isOfflineMode,\n            verticalArrangement = Arrangement.spacedBy(16.dp),")

with open("app/src/main/java/com/example/features/student/widgets/StudentBaseScreen.kt", "w") as f:
    f.write(text)
