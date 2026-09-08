with open("app/src/main/java/com/example/ui/screens/VaultScreen.kt", "r") as f:
    text = f.read()

count = 0
for i, line in enumerate(text.split('\n')):
    count += line.count('{') - line.count('}')
    if i > 390 and i < 410:
        print(f"Line {i+1} count: {count} | {line}")
