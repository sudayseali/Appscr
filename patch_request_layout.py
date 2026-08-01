import sys

with open('app/src/main/java/com/noxscreen/app/BlackScreenService.kt', 'r') as f:
    content = f.read()

target = """            }
            setImageResource(iconRes)
        }
    }"""

replacement = """            }
            setImageResource(iconRes)
            requestLayout()
        }
    }"""

content = content.replace(target, replacement)

with open('app/src/main/java/com/noxscreen/app/BlackScreenService.kt', 'w') as f:
    f.write(content)

