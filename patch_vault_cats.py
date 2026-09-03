import re

with open("app/src/main/java/com/example/ui/screens/VaultScreen.kt", "r") as f:
    content = f.read()

old_items = "items(VAULT_CATEGORIES) { cat ->"
new_items = """val filterCategories = remember(documents) {
            val dynamicCats = documents.map { it.category.trim() }.filter { it.isNotBlank() }.distinct()
            (listOf("All") + dynamicCats).distinct()
        }
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filterCategories) { cat ->"""

content = content.replace("""        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(VAULT_CATEGORIES) { cat ->""", new_items)

with open("app/src/main/java/com/example/ui/screens/VaultScreen.kt", "w") as f:
    f.write(content)

