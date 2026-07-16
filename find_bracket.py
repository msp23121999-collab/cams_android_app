with open("app/src/main/java/com/example/core/network/CamsApiService.kt") as f:
    text = f.read()
import re
match = re.search(r'interface CamsApiService \{.*?(?=\n(?:data class|class) )', text, re.DOTALL)
if match:
    print(match.group(0)[-500:])
