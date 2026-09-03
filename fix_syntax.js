const fs = require('fs');
let content = fs.readFileSync('app/src/main/java/com/example/ui/components/BinderNavigation.kt', 'utf8');

// Fix LedgerBinderBottomBar syntax
content = content.replace(/LedgerSection\.values\(\)\.forEach \{ section ->[\s\S]*?\}\s*\}\s*\}\s*\}\s*\}\s*\}[\s\S]*?@Composable\nprivate fun BinderTabItem/m, (match) => {
    // We just want to make sure it is closed exactly properly
    return `LedgerSection.values().forEach { section ->
                val isSelected = currentSection == section
                BinderTabItem(
                    section = section,
                    isSelected = isSelected,
                    onClick = { onSectionSelected(section) }
                )
            }
        }
        }
    }
}

@Composable
private fun BinderTabItem`;
});

// Fix LedgerBinderNavRail syntax
content = content.replace(/LedgerSection\.values\(\)\.forEachIndexed \{ index, section ->[\s\S]*?\}\s*\}\s*\}\s*\}\s*\nprivate fun getSectionColor/m, (match) => {
    // The previous text goes up to the closing column bracket. We added a Box which needs to be closed before the Surface is closed.
    return match.replace(/\}\s*\}\s*\nprivate fun getSectionColor/, `}
        }
        }
    }
}

private fun getSectionColor`);
});

fs.writeFileSync('app/src/main/java/com/example/ui/components/BinderNavigation.kt', content);
