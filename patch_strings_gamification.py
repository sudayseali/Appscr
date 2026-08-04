with open("app/src/main/res/values/strings.xml", "r", encoding="utf-8") as f:
    content = f.read()

new_strings = """
    <string name="battery_analytics">Battery Analytics</string>
    <string name="gamification">Achievements</string>
    <string name="level">Level</string>
    <string name="hours_saved">Hours Saved</string>
    <string name="next_level">Next Level</string>
"""

if 'battery_analytics' not in content:
    content = content.replace('</resources>', new_strings + '</resources>')

with open("app/src/main/res/values/strings.xml", "w", encoding="utf-8") as f:
    f.write(content)
