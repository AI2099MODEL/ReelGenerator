with open('app/src/main/java/com/example/ui/screens/RingtonesScreen.kt', 'r') as f:
    text = f.read()

import re

# Remove countWords
text = re.sub(r'fun countWords\(text: String\): Int \{.*?\}\n', '', text, flags=re.DOTALL)

# Fix the call to FullScorePlayerBottomSheet
text = text.replace(
'''            if (showPlayerSheet && trackToPlay != null) {
                FullScorePlayerBottomSheet(
                    track = trackToPlay!!,''',
'''            if (showPlayerSheet && trackToPlay != null) {
                FullScorePlayerBottomSheet(
                    context = context,
                    track = trackToPlay!!,''')

with open('app/src/main/java/com/example/ui/screens/RingtonesScreen.kt', 'w') as f:
    f.write(text)
