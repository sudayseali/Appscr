import sys

with open('app/src/main/java/com/noxscreen/app/BlackScreenService.kt', 'r') as f:
    content = f.read()

# Replace the catch block in showBlackoutInternal
target_catch_blackout = '''        } catch (e: Exception) {
            try {
                val intent = Intent(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION, android.net.Uri.parse("package:$packageName"))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            } catch (e2: Exception) {}
            Handler(Looper.getMainLooper()).post {
                android.widget.Toast.makeText(this, "Please grant overlay permission", android.widget.Toast.LENGTH_LONG).show()
            }
        }'''

replacement_catch_blackout = '''        } catch (e: Exception) {
            // Fallback to Activity if overlay is denied by AppOps
            try {
                val intent = Intent(this, BlackoutActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
            } catch (e2: Exception) {
                e2.printStackTrace()
            }
        }'''

content = content.replace(target_catch_blackout, replacement_catch_blackout)

target_catch_floating = '''        } catch (e: Exception) {
            try {
                val intent = Intent(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION, android.net.Uri.parse("package:$packageName"))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            } catch (e2: Exception) {}
            
            Handler(Looper.getMainLooper()).post {
                android.widget.Toast.makeText(this, "Please grant overlay permission", android.widget.Toast.LENGTH_LONG).show()
            }
        }'''

replacement_catch_floating = '''        } catch (e: Exception) {
            // Fallback: If floating button fails, start BlackoutActivity directly
            try {
                val intent = Intent(this, BlackoutActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
            } catch (e2: Exception) {
                e2.printStackTrace()
            }
        }'''

content = content.replace(target_catch_floating, replacement_catch_floating)

with open('app/src/main/java/com/noxscreen/app/BlackScreenService.kt', 'w') as f:
    f.write(content)
