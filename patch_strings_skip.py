import os

new_strings = """
    <string name="skip_unlock_screen">Skip Unlock Screen</string>
"""

new_strings_so = """
    <string name="skip_unlock_screen">Toos u fur shaashada (Skip Unlock)</string>
"""

paths = [
    ("app/src/main/res/values/strings.xml", new_strings),
    ("app/src/main/res/values-so/strings.xml", new_strings_so)
]

for path, additional_strings in paths:
    if os.path.exists(path):
        with open(path, "r", encoding="utf-8") as f:
            content = f.read()

        if '<string name="skip_unlock_screen">' not in content:
            content = content.replace('</resources>', additional_strings + '</resources>')

        with open(path, "w", encoding="utf-8") as f:
            f.write(content)
