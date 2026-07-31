import sys

with open('app/src/main/java/com/noxscreen/app/BlackScreenService.kt', 'r') as f:
    content = f.read()

content = content.replace('''        } catch (e: Exception) {
            val intent = Intent(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION, android.net.Uri.parse("package:$packageName"))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            
            Handler(Looper.getMainLooper()).post {
                android.widget.Toast.makeText(this, "Please grant overlay permission", android.widget.Toast.LENGTH_LONG).show()
            }
        }''', '''        } catch (e: Exception) {
            try {
                val intent = Intent(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION, android.net.Uri.parse("package:$packageName"))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            } catch (e2: Exception) {}
            
            Handler(Looper.getMainLooper()).post {
                android.widget.Toast.makeText(this, "Please grant overlay permission", android.widget.Toast.LENGTH_LONG).show()
            }
        }''')

content = content.replace('''        } catch (e: Exception) {
            val intent = Intent(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION, android.net.Uri.parse("package:$packageName"))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            Handler(Looper.getMainLooper()).post {
                android.widget.Toast.makeText(this, "Please grant overlay permission", android.widget.Toast.LENGTH_LONG).show()
            }
        }''', '''        } catch (e: Exception) {
            try {
                val intent = Intent(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION, android.net.Uri.parse("package:$packageName"))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            } catch (e2: Exception) {}
            Handler(Looper.getMainLooper()).post {
                android.widget.Toast.makeText(this, "Please grant overlay permission", android.widget.Toast.LENGTH_LONG).show()
            }
        }''')

with open('app/src/main/java/com/noxscreen/app/BlackScreenService.kt', 'w') as f:
    f.write(content)
