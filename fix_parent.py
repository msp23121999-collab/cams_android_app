with open("app/src/main/java/com/example/features/parent/screens/ParentDashboardScreen.kt", "r") as f:
    text = f.read()

import re
replacement = """        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CamsNavy)
            }
        } else if (uiState.error != null) {
            com.example.core.ui.NetworkErrorView(
                message = uiState.error,
                onRetry = { viewModel.loadData() }
            )
        } else {"""
text = text.replace('        if (uiState.isLoading) {\n            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {\n                CircularProgressIndicator(color = CamsNavy)\n            }\n        } else {', replacement)

with open("app/src/main/java/com/example/features/parent/screens/ParentDashboardScreen.kt", "w") as f:
    f.write(text)
