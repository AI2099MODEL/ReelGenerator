with open('app/src/main/java/com/example/ui/screens/RingtonesScreen.kt', 'r') as f:
    text = f.read()

# Fix the accidental addition of context = context, in FloatingMiniPlayerBar
bad_call = """                FloatingMiniPlayerBar(
                    context = context,
            track = currentlyPlayingTrack!!,"""

good_call = """                FloatingMiniPlayerBar(
                    track = currentlyPlayingTrack!!,"""

text = text.replace(bad_call, good_call)

with open('app/src/main/java/com/example/ui/screens/RingtonesScreen.kt', 'w') as f:
    f.write(text)
