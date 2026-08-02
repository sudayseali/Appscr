import sys

with open('app/src/main/AndroidManifest.xml', 'r') as f:
    content = f.read()

target = """    <uses-permission android:name="android.permission.WAKE_LOCK" />"""
replacement = """    <uses-permission android:name="android.permission.WAKE_LOCK" />
    <uses-permission android:name="android.permission.PACKAGE_USAGE_STATS" tools:ignore="ProtectedPermissions" />"""

content = content.replace(target, replacement)

with open('app/src/main/AndroidManifest.xml', 'w') as f:
    f.write(content)
