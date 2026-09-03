import re

with open("app/src/main/java/com/example/ui/screens/DealsScreen.kt", "r") as f:
    content = f.read()

bad_deals = """            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filterCategories = remember(deals) {
                    val dynamicCats = deals.map { it.category.trim() }.filter { it.isNotBlank() }.distinct()
                    (listOf("All") + dynamicCats).distinct()
                }
                items(filterCategories) { cat ->"""
good_deals = """            val filterCategories = remember(deals) {
                val dynamicCats = deals.map { it.category.trim() }.filter { it.isNotBlank() }.distinct()
                (listOf("All") + dynamicCats).distinct()
            }
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filterCategories) { cat ->"""

content = content.replace(bad_deals, good_deals)

with open("app/src/main/java/com/example/ui/screens/DealsScreen.kt", "w") as f:
    f.write(content)


with open("app/src/main/java/com/example/ui/screens/EventsScreen.kt", "r") as f:
    content2 = f.read()

bad_events = """        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val dynamicFilters = remember(events) {
                val dynamicCats = events.map { it.category.trim() }.filter { it.isNotBlank() }.distinct()
                (listOf("Upcoming") + dynamicCats).distinct()
            }
            items(dynamicFilters) { filter ->"""

good_events = """        val dynamicFilters = remember(events) {
            val dynamicCats = events.map { it.category.trim() }.filter { it.isNotBlank() }.distinct()
            (listOf("Upcoming") + dynamicCats).distinct()
        }
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(dynamicFilters) { filter ->"""

content2 = content2.replace(bad_events, good_events)

with open("app/src/main/java/com/example/ui/screens/EventsScreen.kt", "w") as f:
    f.write(content2)

