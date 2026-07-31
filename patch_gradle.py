import sys

with open('app/build.gradle.kts', 'r') as f:
    content = f.read()

content = content.replace("dependencies {", "dependencies {\n  implementation(\"androidx.biometric:biometric:1.1.0\")")

with open('app/build.gradle.kts', 'w') as f:
    f.write(content)
