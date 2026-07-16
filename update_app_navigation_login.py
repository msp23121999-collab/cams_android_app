import re

target = 'app/src/main/java/com/example/core/navigation/AppNavigation.kt'
with open(target, 'r') as f:
    content = f.read()

# Modify LoginScreen call
replacement = """            com.example.features.auth.LoginScreen(
                role = role,
                authViewModel = authViewModel,
                onLoginSuccess = { 
                    val targetRoute = RoleGuard.getDashboardRoute(role)
                    navController.navigate(targetRoute) {
                        popUpTo(AppRoutes.LOGIN) { inclusive = true }
                        popUpTo(AppRoutes.ROLE_SELECTION) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() },
                onDebugTap = { navController.navigate(AppRoutes.DEBUG) }
            )"""

content = re.sub(r'com\.example\.features\.auth\.LoginScreen\([^)]*\)', replacement, content, flags=re.DOTALL)

with open(target, 'w') as f:
    f.write(content)

