import re

with open('app/src/main/java/com/example/ui/screens/ImageStudioScreen.kt', 'r') as f:
    content = f.read()

# The user requested: "secondly when zoom in photo it is showing prompt too which u added yourself with out getting approvsl. zoom will only show generated picture."
# I will remove the text overlay that shows the prompt on the generated image preview (lines 985-1002 in ImageStudioScreen.kt)

prompt_overlay_pattern = r'Surface\(\s*modifier = Modifier\s*\.align\(Alignment\.BottomStart\)\s*\.padding\(8\.dp\),\s*shape = RoundedCornerShape\(8\.dp\),\s*color = Color\.Black\.copy\(alpha = 0\.75f\),\s*border = BorderStroke\(0\.5\.dp, GoldAccent\.copy\(alpha = 0\.4f\)\)\s*\)\s*\{\s*Text\(\s*text = res\.prompt,\s*modifier = Modifier\.padding\(horizontal = 8\.dp, vertical = 4\.dp\),\s*fontSize = 11\.sp,\s*color = GoldHighlight,\s*maxLines = 1,\s*overflow = TextOverflow\.Ellipsis\s*\)\s*\}'

new_content = re.sub(prompt_overlay_pattern, '', content)

with open('app/src/main/java/com/example/ui/screens/ImageStudioScreen.kt', 'w') as f:
    f.write(new_content)

