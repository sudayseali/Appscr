import sys

with open('app/src/main/java/com/noxscreen/app/BlackScreenService.kt', 'r') as f:
    content = f.read()

target = """import android.content.Context
import android.content.Intent"""

replacement = """import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter"""

content = content.replace(target, replacement)

with open('app/src/main/java/com/noxscreen/app/BlackScreenService.kt', 'w') as f:
    f.write(content)
