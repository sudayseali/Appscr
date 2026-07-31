import sys

with open('app/src/main/AndroidManifest.xml', 'r') as f:
    content = f.read()

if 'BlackoutActivity' not in content:
    content = content.replace('</application>', '''        <activity
            android:name=".BlackoutActivity"
            android:exported="false"
            android:theme="@style/Theme.MyApplication" />
    </application>''')
    
    with open('app/src/main/AndroidManifest.xml', 'w') as f:
        f.write(content)
