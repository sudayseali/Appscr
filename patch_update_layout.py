import sys

with open('app/src/main/java/com/noxscreen/app/BlackScreenService.kt', 'r') as f:
    content = f.read()

target = """            }
            setImageResource(iconRes)
            requestLayout()
        }
    }"""
    
replacement = """            }
            setImageResource(iconRes)
            requestLayout()
        }
        try {
            if (floatingView?.parent != null) {
                windowManager.updateViewLayout(floatingView, floatingLayoutParams)
            }
        } catch (e: Exception) {}
    }"""

content = content.replace(target, replacement)

with open('app/src/main/java/com/noxscreen/app/BlackScreenService.kt', 'w') as f:
    f.write(content)

