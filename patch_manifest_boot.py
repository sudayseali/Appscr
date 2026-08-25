import os

path = "/tmp/nox_hardening/app/src/main/AndroidManifest.xml"
with open(path, "r") as f:
    content = f.read()

perm_target = """    <uses-permission android:name="android.permission.WAKE_LOCK" />"""
perm_replace = """    <uses-permission android:name="android.permission.WAKE_LOCK" />
    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />"""

receiver_target = """        <receiver android:name=".WidgetActionReceiver" android:exported="false">
            <intent-filter>
                <action android:name="TOGGLE_NOX_SCREEN" />
            </intent-filter>
        </receiver>"""
receiver_replace = receiver_target + """
        <receiver android:name=".BootReceiver" android:exported="false">
            <intent-filter>
                <action android:name="android.intent.action.BOOT_COMPLETED" />
                <action android:name="android.intent.action.MY_PACKAGE_REPLACED" />
            </intent-filter>
        </receiver>"""

if perm_target in content and receiver_target in content:
    content = content.replace(perm_target, perm_replace)
    content = content.replace(receiver_target, receiver_replace)
    with open(path, "w") as f:
        f.write(content)
    print("Success")
else:
    print("Target not found")
