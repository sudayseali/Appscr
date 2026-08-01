import sys

with open('app/src/main/java/com/noxscreen/app/MainActivity.kt', 'r') as f:
    content = f.read()

target = """                val styles = listOf("lock" to androidx.compose.ui.res.painterResource(R.drawable.ic_lock),
                    "moon" to androidx.compose.ui.res.painterResource(R.drawable.ic_moon),
                    "circle" to androidx.compose.ui.res.painterResource(R.drawable.ic_circle),
                    "double_circle" to androidx.compose.ui.res.painterResource(R.drawable.ic_double_circle),
                    "key" to androidx.compose.ui.res.painterResource(R.drawable.ic_key),
                    "eye_off" to androidx.compose.ui.res.painterResource(R.drawable.ic_eye_off))"""

replacement = """                val styles = listOf("lock" to androidx.compose.ui.res.painterResource(R.drawable.ic_lock),
                    "moon" to androidx.compose.ui.res.painterResource(R.drawable.ic_moon),
                    "circle" to androidx.compose.ui.res.painterResource(R.drawable.ic_circle),
                    "double_circle" to androidx.compose.ui.res.painterResource(R.drawable.ic_double_circle),
                    "key" to androidx.compose.ui.res.painterResource(R.drawable.ic_key),
                    "eye_off" to androidx.compose.ui.res.painterResource(R.drawable.ic_eye_off),
                    "shield" to androidx.compose.ui.res.painterResource(R.drawable.ic_shield),
                    "fingerprint" to androidx.compose.ui.res.painterResource(R.drawable.ic_fingerprint),
                    "power" to androidx.compose.ui.res.painterResource(R.drawable.ic_power),
                    "bolt" to androidx.compose.ui.res.painterResource(R.drawable.ic_bolt),
                    "favorite" to androidx.compose.ui.res.painterResource(R.drawable.ic_favorite))"""

content = content.replace(target, replacement)

with open('app/src/main/java/com/noxscreen/app/MainActivity.kt', 'w') as f:
    f.write(content)

with open('app/src/main/java/com/noxscreen/app/BlackScreenService.kt', 'r') as f:
    content = f.read()

target2 = """            val iconRes = when (config.floatingLockStyle) {
                "lock" -> R.drawable.ic_lock
                "moon" -> R.drawable.ic_moon
                "circle" -> R.drawable.ic_circle
                "double_circle" -> R.drawable.ic_double_circle
                "key" -> R.drawable.ic_key
                "eye_off" -> R.drawable.ic_eye_off
                else -> R.drawable.ic_moon
            }"""

replacement2 = """            val iconRes = when (config.floatingLockStyle) {
                "lock" -> R.drawable.ic_lock
                "moon" -> R.drawable.ic_moon
                "circle" -> R.drawable.ic_circle
                "double_circle" -> R.drawable.ic_double_circle
                "key" -> R.drawable.ic_key
                "eye_off" -> R.drawable.ic_eye_off
                "shield" -> R.drawable.ic_shield
                "fingerprint" -> R.drawable.ic_fingerprint
                "power" -> R.drawable.ic_power
                "bolt" -> R.drawable.ic_bolt
                "favorite" -> R.drawable.ic_favorite
                else -> R.drawable.ic_moon
            }"""
            
content = content.replace(target2, replacement2)

with open('app/src/main/java/com/noxscreen/app/BlackScreenService.kt', 'w') as f:
    f.write(content)

