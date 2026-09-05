#!/bin/bash
cat << 'INNER_EOF' >> app/src/main/java/com/example/ui/components/LedgerSideMenuDrawer.kt
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "SECTIONS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = RoseQuartzTextMuted,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                        )
                        DrawerNavItem(
                            label = "Home Dashboard",
                            icon = Icons.Filled.Home,
                            isSelected = currentSection == LedgerSection.HOME,
                            onClick = {
                                onSectionSelected(LedgerSection.HOME)
                                onCloseDrawer()
                            }
                        )
                        DrawerNavItem(
                            label = "Tasks to Remember",
                            icon = Icons.Filled.CheckCircle,
                            isSelected = currentSection == LedgerSection.TASKS,
                            onClick = {
                                onSectionSelected(LedgerSection.TASKS)
                                onCloseDrawer()
                            }
                        )
                        DrawerNavItem(
                            label = "Event Dates",
                            icon = Icons.Filled.DateRange,
                            isSelected = currentSection == LedgerSection.EVENTS,
                            onClick = {
                                onSectionSelected(LedgerSection.EVENTS)
                                onCloseDrawer()
                            }
                        )
                        DrawerNavItem(
                            label = "Secure Vault",
                            icon = Icons.Filled.Lock,
                            isSelected = currentSection == LedgerSection.VAULT,
                            onClick = {
                                onSectionSelected(LedgerSection.VAULT)
                                onCloseDrawer()
                            }
                        )
                        DrawerNavItem(
                            label = "Image Studio",
                            icon = Icons.Filled.Image,
                            isSelected = currentSection == LedgerSection.IMAGES,
                            onClick = {
                                onSectionSelected(LedgerSection.IMAGES)
                                onCloseDrawer()
                            }
                        )
                    }
                }
            } // end LazyColumn
        } // end Column
    }
}
INNER_EOF
