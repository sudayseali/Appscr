with open('app/src/main/AndroidManifest.xml', 'r') as f:
    content = f.read()

target = """        <activity
            android:name=".BiometricAuthActivity"
            android:exported="false"
            android:theme="@style/Theme.Transparent" />"""

replacement = """        <activity
            android:name=".BiometricAuthActivity"
            android:exported="false"
            android:taskAffinity=""
            android:excludeFromRecents="true"
            android:theme="@style/Theme.Transparent" />"""

if target in content:
    content = content.replace(target, replacement)
    with open('app/src/main/AndroidManifest.xml', 'w') as f:
        f.write(content)
    print("Patched manifest")
else:
    print("Target not found")
