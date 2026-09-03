const fs = require('fs');
let content = fs.readFileSync('app/src/main/java/com/example/ui/components/BinderNavigation.kt', 'utf8');

// Find the Column block inside LedgerBinderNavRail and add a closing bracket for the Box
const target = `
        }
    }
}

@Composable
private fun BinderTabItem(
`;
if (!content.includes(`
        }
        }
    }
}

@Composable
private fun BinderTabItem(`)) {
    content = content.replace(target, `
        }
        }
    }
}

@Composable
private fun BinderTabItem(
`);
}

fs.writeFileSync('app/src/main/java/com/example/ui/components/BinderNavigation.kt', content);
