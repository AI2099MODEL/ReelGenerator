import re

with open("app/src/main/java/com/example/ui/screens/VaultScreen.kt", "r") as f:
    content = f.read()

# Remove the JSON parsing block completely
json_parse_pattern = r'var parsedOk = false\s*try \{.*?\}\s*if \(\!parsedOk\) \{\s*docNotes = responseText \?\: ""\s*\}'
content = re.sub(json_parse_pattern, 'docNotes = responseText ?: ""', content, flags=re.DOTALL)

# Fix the missing '}' issue - let's check brackets using a simple stack or just append '}' if needed.
# Since we replaced the `if (showAddDialog)` block, we might have mismatched brackets.
# Let's count them:
open_br = content.count('{')
close_br = content.count('}')

if open_br > close_br:
    content += "\n" + "}" * (open_br - close_br) + "\n"
elif close_br > open_br:
    # Too many closing brackets, let's remove from the end
    for _ in range(close_br - open_br):
        content = content.rstrip().rsplit('}', 1)[0]

with open("app/src/main/java/com/example/ui/screens/VaultScreen.kt", "w") as f:
    f.write(content)
