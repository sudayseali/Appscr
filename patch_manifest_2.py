import sys

with open('app/src/main/AndroidManifest.xml', 'r') as f:
    content = f.read()

target = """        <activity
            android:name=".AdLauncherActivity"
            android:exported="false"
            android:theme="@style/Theme.MyApplication" />"""

if target in content:
    content = content.replace(target, "")
    with open('app/src/main/AndroidManifest.xml', 'w') as f:
        f.write(content)
    print("Removed AdLauncherActivity from manifest!")
else:
    print("AdLauncherActivity not found in manifest")
