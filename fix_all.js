const fs = require('fs');
let content = fs.readFileSync('app/src/main/java/com/example/ui/components/BinderNavigation.kt', 'utf8');

// The lines before getSectionColor:
//                         )
//                     }
//                 }
//             }
//             }
//         }
//         }
//     }
// }
// 
// private fun getSectionColor
// There should be exactly 5 braces from the end of the forEach section to getSectionColor.
// Surface { Column { Icon Spacer Text } } (closes Surface)
// } (closes forEach)
// } (closes outer Column)
// } (closes Box)
// } (closes outer Surface)
// } (closes LedgerBinderNavRail)
content = content.replace(/\}\s*\}\s*\}\s*\}\s*\}\s*\}\s*\}\s*private fun getSectionColor/m, `
                }
            }
        }
    }
}
private fun getSectionColor`);

fs.writeFileSync('app/src/main/java/com/example/ui/components/BinderNavigation.kt', content);
