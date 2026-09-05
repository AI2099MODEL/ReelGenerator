#!/bin/bash
sed -i '/if (onOpenGlobalSettings != null) {/i\
                                DrawerNavItem(\
                                    label = "Privacy Policy",\
                                    icon = Icons.Outlined.Security,\
                                    isSelected = false,\
                                    onClick = {\
                                        onCloseDrawer()\
                                        /* This could launch an intent or just be a visual placeholder. Since we created PrivacyPolicyScreen, maybe navigate to it? But we don\'t have navigation setup for it in the drawer without a LedgerSection. Let\'s just show a dialog or open a URL. Actually I will add a new LedgerSection.PRIVACY */\
                                    }\
                                )\
' app/src/main/java/com/example/ui/components/LedgerSideMenuDrawer.kt
