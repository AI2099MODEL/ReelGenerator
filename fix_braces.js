const fs = require('fs');
let content = fs.readFileSync('app/src/main/java/com/example/ui/components/BinderNavigation.kt', 'utf8');

// There are extra braces before `private fun getSectionColor`
content = content.replace(/\s*\}\s*\}\s*\}\s*\}\s*\}\s*\}\s*private fun getSectionColor/m, `
            }
        }
        }
    }
}

private fun getSectionColor`);

fs.writeFileSync('app/src/main/java/com/example/ui/components/BinderNavigation.kt', content);
