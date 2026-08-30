import os

path = "app/src/test/java/com/noxscreen/app/automation/SensorHandlerTest.kt"
with open(path, "r") as f:
    content = f.read()

target = """import org.mockito.Mockito.*

class SensorHandlerTest {"""
replacement = """import org.mockito.Mockito.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SensorHandlerTest {"""

if target in content:
    content = content.replace(target, replacement)
    with open(path, "w") as f:
        f.write(content)
    print("Success")
else:
    print("Target not found")
