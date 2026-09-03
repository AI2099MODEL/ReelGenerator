const fs = require('fs');

let content = fs.readFileSync('app/src/main/java/com/example/ui/components/BinderNavigation.kt', 'utf8');

content = content.replace(/LedgerSection\.TASKS -> NavTasksViolet/, `LedgerSection.TASKS -> NavTasksViolet
        LedgerSection.SOCIAL -> NavSocialOrange`);

content = content.replace(/LedgerSection\.TASKS -> if \(isSelected\) Icons\.Filled\.CheckCircle else Icons\.Outlined\.CheckCircle/, `LedgerSection.TASKS -> if (isSelected) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle
        LedgerSection.SOCIAL -> if (isSelected) Icons.Filled.Share else Icons.Outlined.Share`);

fs.writeFileSync('app/src/main/java/com/example/ui/components/BinderNavigation.kt', content);
