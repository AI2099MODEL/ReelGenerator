import re

with open("app/src/main/java/com/example/ui/screens/EventsScreen.kt", "r") as f:
    content = f.read()

old_items = "items(EVENT_FILTERS) { filter ->"
new_items = """val dynamicFilters = remember(events) {
                val dynamicCats = events.map { it.category.trim() }.filter { it.isNotBlank() }.distinct()
                (listOf("Upcoming") + dynamicCats).distinct()
            }
            items(dynamicFilters) { filter ->"""

content = content.replace("items(EVENT_FILTERS) { filter ->", new_items)

with open("app/src/main/java/com/example/ui/screens/EventsScreen.kt", "w") as f:
    f.write(content)

