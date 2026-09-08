with open("app/src/main/java/com/example/ui/screens/VaultScreen.kt", "r") as f:
    text = f.read()

count = 0
for i, line in enumerate(text.split('\n')):
    count += line.count('{') - line.count('}')
print(f"Final count: {count}")
