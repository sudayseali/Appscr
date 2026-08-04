import os
path = "app/src/main/res/values-so/strings.xml"
if os.path.exists(path):
    with open(path, "r", encoding="utf-8") as f:
        content = f.read()

    if '<string name="language">' not in content:
        content = content.replace('</resources>', '    <string name="language">Luuqadda</string>\n    <string name="select_language">Dooro luuqad</string>\n</resources>')

    with open(path, "w", encoding="utf-8") as f:
        f.write(content)
