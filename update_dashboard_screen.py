import re

target = 'app/src/main/java/com/example/features/student/screens/StudentDashboardScreen.kt'
with open(target, 'r') as f:
    content = f.read()

# We replace the error block to instead render the dashboard (since FallbackData is loaded)
# and add a yellow banner inside the LazyColumn or just below the top bar.

# Find the block:
# } else if (uiState.error != null) {
#     com.example.core.ui.NetworkErrorView(
#         message = uiState.error!!,
#         onRetry = { viewModel.fetchDashboardData() },
#         modifier = Modifier.align(Alignment.Center)
#     )
# } else {

# We just remove this block and rely on the offline data. Wait, we need to show the banner.
replacement = """                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        if (uiState.isOfflineMode) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFEAB308))
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Showing offline data. Connect to the server for live updates.", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }"""

content = re.sub(r'\} else if \(uiState\.error != null\) \{.*?\}.*?\} else \{', replacement, content, flags=re.DOTALL)

with open(target, 'w') as f:
    f.write(content)

