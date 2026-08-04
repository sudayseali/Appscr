import re

with open('app/src/main/AndroidManifest.xml', 'r', encoding='utf-8') as f:
    content = f.read()

widget_components = """        <receiver android:name=".NoxWidgetProvider" android:exported="true">
            <intent-filter>
                <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
            </intent-filter>
            <meta-data
                android:name="android.appwidget.provider"
                android:resource="@xml/nox_widget_info" />
        </receiver>

        <receiver android:name=".WidgetActionReceiver" android:exported="false">
            <intent-filter>
                <action android:name="TOGGLE_NOX_SCREEN" />
            </intent-filter>
        </receiver>"""

if 'NoxWidgetProvider' not in content:
    content = content.replace('</application>', widget_components + '\n    </application>')

with open('app/src/main/AndroidManifest.xml', 'w', encoding='utf-8') as f:
    f.write(content)
