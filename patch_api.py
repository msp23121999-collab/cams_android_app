import re

with open("app/src/main/java/com/example/core/network/ApiClient.kt", "r") as f:
    text = f.read()

interceptor = """class BaseUrlInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        val newBaseUrl = okhttp3.HttpUrl.parse(AppConfig.BASE_URL)
        if (newBaseUrl != null) {
            val newUrl = request.url().newBuilder()
                .scheme(newBaseUrl.scheme())
                .host(newBaseUrl.host())
                .port(newBaseUrl.port())
                .build()
            request = request.newBuilder().url(newUrl).build()
        }
        return chain.proceed(request)
    }
}
"""

if "BaseUrlInterceptor" not in text:
    text = text.replace("class NetworkErrorInterceptor", interceptor + "\nclass NetworkErrorInterceptor")
    
    text = text.replace(".addInterceptor(NetworkErrorInterceptor())", ".addInterceptor(BaseUrlInterceptor())\n            .addInterceptor(NetworkErrorInterceptor())")
    text = text.replace("val BASE_URL = AppConfig.BASE_URL", "var BASE_URL = AppConfig.BASE_URL")

with open("app/src/main/java/com/example/core/network/ApiClient.kt", "w") as f:
    f.write(text)
