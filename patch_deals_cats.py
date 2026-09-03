import re

with open("app/src/main/java/com/example/ui/screens/DealsScreen.kt", "r") as f:
    content = f.read()

old_items = "items(categories) { cat ->"
new_items = """val filterCategories = remember(deals) {
                    val dynamicCats = deals.map { it.category.trim() }.filter { it.isNotBlank() }.distinct()
                    (listOf("All") + dynamicCats).distinct()
                }
                items(filterCategories) { cat ->"""

content = content.replace("items(categories) { cat ->", new_items, 1) # Only replace the first one which is the LazyRow

with open("app/src/main/java/com/example/ui/screens/DealsScreen.kt", "w") as f:
    f.write(content)

