import re

with open('app/src/main/AndroidManifest.xml', 'r', encoding='utf-8') as f:
    content = f.read()

tile_service = """        <service
            android:name=".NoxTileService"
            android:label="@string/app_name"
            android:icon="@mipmap/ic_launcher"
            android:permission="android.permission.BIND_QUICK_SETTINGS_TILE"
            android:exported="true">
            <intent-filter>
                <action android:name="android.service.quicksettings.action.QS_TILE" />
            </intent-filter>
        </service>"""

if 'NoxTileService' not in content:
    content = content.replace('</application>', tile_service + '\n    </application>')

with open('app/src/main/AndroidManifest.xml', 'w', encoding='utf-8') as f:
    f.write(content)
