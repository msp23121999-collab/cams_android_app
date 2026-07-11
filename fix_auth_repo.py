with open("app/src/main/java/com/example/core/repository/AuthRepository.kt") as f:
    text = f.read()

text = text.replace("authManager.saveToken(body.token)", "authManager.saveAuth(body.token, body.user.role)")
text = text.replace("authManager.saveRole(body.user.role)", "")

with open("app/src/main/java/com/example/core/repository/AuthRepository.kt", "w") as f:
    f.write(text)
