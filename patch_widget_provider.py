with open("app/src/main/java/com/noxscreen/app/NoxWidgetProvider.kt", "r", encoding="utf-8") as f:
    content = f.read()

target = """            val views = RemoteViews(context.packageName, R.layout.widget_layout)

            val intent = Intent(context, WidgetActionReceiver::class.java)"""

replacement = """            val views = RemoteViews(context.packageName, R.layout.widget_layout)
            
            val isRunning = BlackScreenService.isRunning
            if (isRunning) {
                views.setImageViewResource(R.id.widget_button, R.drawable.ic_power)
            } else {
                views.setImageViewResource(R.id.widget_button, R.drawable.ic_power_inactive)
            }

            val intent = Intent(context, WidgetActionReceiver::class.java)"""

if 'setImageViewResource' not in content:
    content = content.replace(target, replacement)

target2 = """    companion object {
        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {"""

replacement2 = """    companion object {
        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = android.content.ComponentName(context, NoxWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }

        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {"""

if 'updateAllWidgets' not in content:
    content = content.replace(target2, replacement2)

with open("app/src/main/java/com/noxscreen/app/NoxWidgetProvider.kt", "w", encoding="utf-8") as f:
    f.write(content)
