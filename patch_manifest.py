import re

with open('app/src/main/AndroidManifest.xml', 'r', encoding='utf-8') as f:
    content = f.read()

# Add attribution tag
if '<attribution' not in content:
    content = content.replace('<application', '    <attribution android:tag="noxscreen" android:label="@string/app_name" />\n\n    <application')

with open('app/src/main/AndroidManifest.xml', 'w', encoding='utf-8') as f:
    f.write(content)
