const fs = require('fs');
let content = fs.readFileSync('app/src/main/java/com/example/ui/components/BinderNavigation.kt', 'utf8');

content = content.replace(/Color\(0xFF81C784\)/g, 'Color(0xFF4FC3F7)');
content = content.replace(/Color\(0xFF4FC3F7\)/g, 'Color(0xFF81D4FA)');

fs.writeFileSync('app/src/main/java/com/example/ui/components/BinderNavigation.kt', content);
