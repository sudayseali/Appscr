import os

new_strings = """
    <string name="flip_to_sleep">Flip to Sleep</string>
    <string name="shake_to_wake">Shake to Wake</string>
"""

new_strings_so = """
    <string name="flip_to_sleep">Geddi si uu u seexdo (Flip to Sleep)</string>
    <string name="shake_to_wake">Rux si uu u tooso (Shake to Wake)</string>
"""

paths = [
    ("app/src/main/res/values/strings.xml", new_strings),
    ("app/src/main/res/values-so/strings.xml", new_strings_so)
]

for path, additional_strings in paths:
    if os.path.exists(path):
        with open(path, "r", encoding="utf-8") as f:
            content = f.read()

        if '<string name="flip_to_sleep">' not in content:
            content = content.replace('</resources>', additional_strings + '</resources>')

        with open(path, "w", encoding="utf-8") as f:
            f.write(content)
