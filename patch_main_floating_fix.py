import sys

with open('app/src/main/java/com/noxscreen/app/MainActivity.kt', 'r') as f:
    content = f.read()

target = """                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val styles = listOf("lock" to androidx.compose.ui.res.painterResource(R.drawable.ic_lock),
                        "moon" to androidx.compose.ui.res.painterResource(R.drawable.ic_moon),
                        "circle" to androidx.compose.ui.res.painterResource(R.drawable.ic_circle),
                        "double_circle" to androidx.compose.ui.res.painterResource(R.drawable.ic_double_circle),
                        "key" to androidx.compose.ui.res.painterResource(R.drawable.ic_key),
                        "eye_off" to androidx.compose.ui.res.painterResource(R.drawable.ic_eye_off))
                        
                    items(styles.size) { index ->"""

replacement = """                
                val styles = listOf("lock" to androidx.compose.ui.res.painterResource(R.drawable.ic_lock),
                    "moon" to androidx.compose.ui.res.painterResource(R.drawable.ic_moon),
                    "circle" to androidx.compose.ui.res.painterResource(R.drawable.ic_circle),
                    "double_circle" to androidx.compose.ui.res.painterResource(R.drawable.ic_double_circle),
                    "key" to androidx.compose.ui.res.painterResource(R.drawable.ic_key),
                    "eye_off" to androidx.compose.ui.res.painterResource(R.drawable.ic_eye_off))
                        
                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(styles.size) { index ->"""

content = content.replace(target, replacement)

with open('app/src/main/java/com/noxscreen/app/MainActivity.kt', 'w') as f:
    f.write(content)
