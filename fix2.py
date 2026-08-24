import re
with open("app/src/main/java/com/noxscreen/app/automation/UsageLimitMonitor.kt", "r") as f:
    content = f.read()

content = re.sub(r'package com\.noxscreen\.app\.automation(.*?)(class UsageLimitMonitor)', r'package com.noxscreen.app.automation\n\nimport kotlinx.coroutines.launch\nimport kotlinx.coroutines.Dispatchers\nimport kotlinx.coroutines.withContext\nimport android.app.usage.UsageEvents\nimport android.app.usage.UsageStatsManager\nimport android.content.Context\nimport android.content.Intent\nimport android.media.AudioManager\nimport android.os.Handler\nimport android.os.Looper\nimport android.widget.Toast\nimport java.util.Calendar\n\n\2', content, flags=re.DOTALL)

with open("app/src/main/java/com/noxscreen/app/automation/UsageLimitMonitor.kt", "w") as f:
    f.write(content)
