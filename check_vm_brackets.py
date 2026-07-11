with open("app/src/main/java/com/example/features/student/providers/DashboardViewModel.kt") as f:
    text = f.read()

count = 0
for i, c in enumerate(text):
    if c == '{': count += 1
    elif c == '}':
        count -= 1
        if count < 0:
            print(f"Negative count at {i}")
print(f"Final count: {count}")
