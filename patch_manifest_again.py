import os

path = "app/src/main/AndroidManifest.xml"
with open(path, "r") as f:
    content = f.read()

target1 = """    <uses-permission android:name="android.permission.WAKE_LOCK" />"""
replacement1 = """    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />"""

target2 = """        <receiver android:name=".WidgetActionReceiver" android:exported="false">
            <intent-filter>
                <action android:name="TOGGLE_NOX_SCREEN" />
            </intent-filter>
        </receiver>"""
replacement2 = """        <receiver android:name=".WidgetActionReceiver" android:exported="false">
            <intent-filter>
                <action android:name="TOGGLE_NOX_SCREEN" />
            </intent-filter>
        </receiver>
        <receiver android:name=".BootReceiver" android:exported="false">
            <intent-filter>
                <action android:name="android.intent.action.BOOT_COMPLETED" />
                <action android:name="android.intent.action.MY_PACKAGE_REPLACED" />
            </intent-filter>
        </receiver>"""

if target1 in content and target2 in content:
    content = content.replace(target1, replacement1)
    content = content.replace(target2, replacement2)
    with open(path, "w") as f:
        f.write(content)
    print("Success")
else:
    print("Target not found")
