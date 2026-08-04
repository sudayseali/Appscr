with open('app/src/main/java/com/noxscreen/app/MainActivity.kt', 'r', encoding='utf-8') as f:
    content = f.read()

if 'import androidx.compose.material.icons.filled.EmojiEvents' not in content:
    content = content.replace('import androidx.compose.material.icons.Icons',
                              'import androidx.compose.material.icons.Icons\nimport androidx.compose.material.icons.filled.EmojiEvents\nimport androidx.compose.material.icons.filled.Analytics')

with open('app/src/main/java/com/noxscreen/app/MainActivity.kt', 'w', encoding='utf-8') as f:
    f.write(content)
