import os

path = "/tmp/nox_hardening/app/src/main/AndroidManifest.xml"
with open(path, "r") as f:
    content = f.read()

perm_target = """    <uses-permission android:name="android.permission.WAKE_LOCK" />\n"""
perm_replace = """"""

if perm_target in content:
    content = content.replace(perm_target, perm_replace)
    with open(path, "w") as f:
        f.write(content)
    print("Success")
else:
    print("Target not found")
