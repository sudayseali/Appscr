import sys

with open('app/src/main/java/com/noxscreen/app/BiometricAuthActivity.kt', 'r') as f:
    content = f.read()

content = content.replace("androidx.appcompat.app.AppCompatActivity", "androidx.fragment.app.FragmentActivity")
content = content.replace("class BiometricAuthActivity : AppCompatActivity()", "class BiometricAuthActivity : FragmentActivity()")

with open('app/src/main/java/com/noxscreen/app/BiometricAuthActivity.kt', 'w') as f:
    f.write(content)
