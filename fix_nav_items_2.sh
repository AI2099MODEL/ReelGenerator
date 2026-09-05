#!/bin/bash
sed -i '/onSectionSelected(LedgerSection.IMAGES)/a\
                                onCloseDrawer()\
                            }\
                        )\
                        DrawerNavItem(\
                            label = "Privacy Policy",\
                            icon = Icons.Filled.Security,\
                            isSelected = false,\
                            onClick = {\
                                onCloseDrawer()\
                                showPrivacyPolicyDialog = true\
                            }\
                        )\
                        if (onOpenGlobalSettings != null) {\
                            DrawerNavItem(\
                                label = "Global Settings & Translation",\
                                icon = Icons.Filled.Settings,\
                                isSelected = false,\
                                onClick = {\
                                    onOpenGlobalSettings()\
                                    onCloseDrawer()\
                                }\
                            )\
                        }' app/src/main/java/com/example/ui/components/LedgerSideMenuDrawer.kt
