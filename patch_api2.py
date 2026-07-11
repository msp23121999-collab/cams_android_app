import re

with open("app/src/main/java/com/example/core/network/ApiClient.kt", "r") as f:
    text = f.read()

text = text.replace("okhttp3.HttpUrl.parse(AppConfig.BASE_URL)", "okhttp3.HttpUrl.parse(AppConfig.BASE_URL) // deprecated but we will fix below")
text = text.replace("okhttp3.HttpUrl.parse(AppConfig.BASE_URL) // deprecated but we will fix below", "okhttp3.HttpUrl.Companion.toHttpUrlOrNull(AppConfig.BASE_URL)")

text = text.replace("request.url().newBuilder()", "request.url.newBuilder()")
text = text.replace("newBaseUrl.scheme()", "newBaseUrl.scheme")
text = text.replace("newBaseUrl.host()", "newBaseUrl.host")
text = text.replace("newBaseUrl.port()", "newBaseUrl.port")

with open("app/src/main/java/com/example/core/network/ApiClient.kt", "w") as f:
    f.write(text)
