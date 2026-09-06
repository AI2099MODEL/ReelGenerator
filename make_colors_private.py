with open('app/src/main/java/com/example/ui/screens/VaultScreen.kt', 'r') as f:
    content = f.read()

content = content.replace('val GoldPrimary', 'private val GoldPrimary')
content = content.replace('val GoldHighlight', 'private val GoldHighlight')
content = content.replace('val GoldAccent', 'private val GoldAccent')
content = content.replace('val GoldLight', 'private val GoldLight')
content = content.replace('val MetallicGoldBrush', 'private val MetallicGoldBrush')
content = content.replace('val SunsetPhoneWallpaperGradient', 'private val SunsetPhoneWallpaperGradient')

with open('app/src/main/java/com/example/ui/screens/VaultScreen.kt', 'w') as f:
    f.write(content)
