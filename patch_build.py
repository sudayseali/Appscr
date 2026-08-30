import os

path = "app/build.gradle.kts"
with open(path, "r") as f:
    content = f.read()

target = """testImplementation(libs.junit)"""
replacement = """testImplementation(libs.junit)
  testImplementation("org.mockito:mockito-core:4.11.0")
  testImplementation("org.mockito.kotlin:mockito-kotlin:4.1.0")"""

if target in content:
    content = content.replace(target, replacement)
    with open(path, "w") as f:
        f.write(content)
    print("Success")
else:
    print("Target not found")
