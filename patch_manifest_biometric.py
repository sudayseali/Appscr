import sys

with open('app/src/main/AndroidManifest.xml', 'r') as f:
    content = f.read()

target = """    </application>"""

replacement = """        <activity
            android:name=".BiometricAuthActivity"
            android:exported="false"
            android:theme="@style/Theme.MyApplication" />
    </application>"""

content = content.replace(target, replacement)

with open('app/src/main/AndroidManifest.xml', 'w') as f:
    f.write(content)
