with open("app/src/main/java/com/noxscreen/app/automation/UsageLimitMonitor.kt", "r") as f:
    content = f.read()

content = content.replace("package com.noxscreen.app.automationimport", "package com.noxscreen.app.automation\nimport")
content = content.replace("import kotlinx", "\nimport kotlinx")
content = content.replace("import android", "\nimport android")
content = content.replace("import java", "\nimport java")
content = content.replace("class UsageLimitMonitor", "\nclass UsageLimitMonitor")

with open("app/src/main/java/com/noxscreen/app/automation/UsageLimitMonitor.kt", "w") as f:
    f.write(content)
