import sys

with open('app/src/main/java/com/noxscreen/app/MainActivity.kt', 'r') as f:
    content = f.read()

target = """            ExpandableConfigSection(
                title = "Usage Limits (Focus Mode)",
                icon = Icons.Default.HealthAndSafety,
                isExpanded = false
            ) {
                var hasUsageStatsPermission by remember { 
                    mutableStateOf(
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as android.app.AppOpsManager
                            val mode = appOps.checkOpNoThrow(
                                android.app.AppOpsManager.OPSTR_GET_USAGE_STATS, 
                                android.os.Process.myUid(), 
                                context.packageName
                            )
                            mode == android.app.AppOpsManager.MODE_ALLOWED
                        } else {
                            true
                        }
                    ) 
                }

                if (!hasUsageStatsPermission) {"""

replacement = """            ExpandableConfigSection(
                title = "Usage Limits (Focus Mode)",
                icon = Icons.Default.HealthAndSafety,
                isExpanded = false
            ) {
                val lifecycleOwner = LocalLifecycleOwner.current
                var hasUsageStatsPermission by remember { mutableStateOf(false) }

                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_RESUME) {
                            hasUsageStatsPermission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                                val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as android.app.AppOpsManager
                                val mode = appOps.checkOpNoThrow(
                                    android.app.AppOpsManager.OPSTR_GET_USAGE_STATS, 
                                    android.os.Process.myUid(), 
                                    context.packageName
                                )
                                mode == android.app.AppOpsManager.MODE_ALLOWED
                            } else {
                                true
                            }
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                    }
                }

                if (!hasUsageStatsPermission) {"""

if target in content:
    content = content.replace(target, replacement)
    with open('app/src/main/java/com/noxscreen/app/MainActivity.kt', 'w') as f:
        f.write(content)
    print("Replaced!")
else:
    print("Not found")
