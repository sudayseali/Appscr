import sys

with open('app/src/main/java/com/noxscreen/app/BlackScreenService.kt', 'r') as f:
    content = f.read()

target = """        } catch (e: Exception) {
            android.util.Log.e("BlackScreenService", "Error adding floating view", e)
            // Fallback: If floating button fails, start BlackoutActivity directly
            try {
                val intent = Intent(this, BlackoutActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
            } catch (e2: Exception) {
                e2.printStackTrace()
            }
        }"""

replacement = """        } catch (e: Exception) {
            android.util.Log.e("BlackScreenService", "Error adding floating view", e)
        }"""

content = content.replace(target, replacement)

with open('app/src/main/java/com/noxscreen/app/BlackScreenService.kt', 'w') as f:
    f.write(content)

