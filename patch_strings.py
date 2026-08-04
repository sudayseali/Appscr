with open("app/src/main/res/values/strings.xml", "r", encoding="utf-8") as f:
    content = f.read()

if '<string name="language">' not in content:
    content = content.replace('</resources>', '    <string name="language">Language</string>\n    <string name="select_language">Select language</string>\n</resources>')

with open("app/src/main/res/values/strings.xml", "w", encoding="utf-8") as f:
    f.write(content)
