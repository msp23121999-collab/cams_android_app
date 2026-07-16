with open("app/src/main/java/com/example/features/admin/screens/AdminDashboardScreen.kt", "r") as f:
    text = f.read()

replacement = """    AdminBaseScreen(
        title = "Admin Portal Dashboard",
        subtitle = "Manage academic configurations, fee trackers, and user enrollment benchmarks.",
        currentRoute = AppRoutes.ADMIN_DASHBOARD,
        onNavigate = onNavigate
    ) {
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.error != null) {
            com.example.core.ui.NetworkErrorView(
                message = uiState.error,
                onRetry = { viewModel.fetchDashboardData() }
            )
        } else {
            Text("Campus Overview", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CamsTextSecondary)"""

text = text.replace("""    AdminBaseScreen(
        title = "Admin Portal Dashboard",
        subtitle = "Manage academic configurations, fee trackers, and user enrollment benchmarks.",
        currentRoute = AppRoutes.ADMIN_DASHBOARD,
        onNavigate = onNavigate
    ) {
        Text("Campus Overview", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CamsTextSecondary)""", replacement)

# We also need to add a closing brace for the new `else {` block, which means we wrap the rest.
# Let's find the closing brace for AdminBaseScreen's content.
# AdminDashboardScreen is short, let's just do it manually.
