const fs = require('fs');

let content = fs.readFileSync('app/src/main/java/com/example/ui/screens/MainLedgerScreen.kt', 'utf8');

content = content.replace(/LedgerSection\.TASKS -> \{[\s\S]*?\}\s*\}/, `LedgerSection.TASKS -> {
                TasksScreen(
                    tasks = tasks,
                    onAddTask = { title, desc, timestamp, priority, notify ->
                        viewModel.addTask(title, desc, timestamp, priority, notify)
                    },
                    onToggleComplete = { viewModel.toggleTaskComplete(it) },
                    onDeleteTask = { viewModel.deleteTask(it) }
                )
            }
            LedgerSection.SOCIAL -> {
                SocialScreen()
            }
        }`);

fs.writeFileSync('app/src/main/java/com/example/ui/screens/MainLedgerScreen.kt', content);
