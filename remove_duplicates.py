with open('app/src/main/java/com/example/ui/screens/RingtonesScreen.kt', 'r') as f:
    text = f.read()

import re

text = re.sub(r'enum class MusicSortOption .*?\}\n', '', text, flags=re.DOTALL)
text = re.sub(r'fun countWords\(text: String\): Int \{.*?\}\n', '', text, flags=re.DOTALL)

with open('app/src/main/java/com/example/ui/screens/RingtonesScreen.kt', 'w') as f:
    f.write(text)
