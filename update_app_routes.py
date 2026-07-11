import re

target = 'app/src/main/java/com/example/core/navigation/AppNavigation.kt'
with open(target, 'r') as f:
    content = f.read()

# Add AppRoutes.DEBUG
content = content.replace('object AppRoutes {', 'object AppRoutes {\n    const val DEBUG = "/debug"')

# Add composable(AppRoutes.DEBUG) { DebugScreen { navController.popBackStack() } }
composable_code = """        composable(AppRoutes.DEBUG) {
            com.example.features.debug.DebugScreen(onBack = { navController.popBackStack() })
        }"""

content = content.replace('composable(AppRoutes.SPLASH) {', composable_code + '\n        composable(AppRoutes.SPLASH) {')

with open(target, 'w') as f:
    f.write(content)

