import re

with open("app/src/main/java/com/noxscreen/app/MainActivity.kt", "r") as f:
    content = f.read()

# ImpactCard
target_impact_card = r'fun ImpactCard\(.*?\}'
replacement_impact_card = """fun ImpactCard(title: String, value: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(ZenithCard, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(title, color = ZenithTextMuted, fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(value, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        // Mock graph
        Row(modifier = Modifier.fillMaxWidth().height(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
            for (i in 0..15) {
                val h = if (i % 3 == 0) 12 else if (i % 2 == 0) 8 else 4
                Box(modifier = Modifier.width(3.dp).height(h.dp).background(color.copy(alpha = 0.7f), RoundedCornerShape(1.dp)))
            }
        }
    }
}"""
content = re.sub(r'fun ImpactCard\(title: String, value: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier\) \{.*?\n\}', replacement_impact_card, content, flags=re.DOTALL)

# ExpandableConfigSection header redesign
target_expandable = r'fun ExpandableConfigSection\(title: String, icon: ImageVector, isExpanded: Boolean, content: @Composable ColumnScope\.\(\) -> Unit\) \{.*?Row\(\s*modifier = Modifier\s*\.fillMaxWidth\(\)\s*\.clickable \{ expanded = !expanded \}\s*\.padding\(horizontal = 20\.dp, vertical = 12\.dp\),\s*verticalAlignment = Alignment\.CenterVertically\s*\) \{.*?Text\(title, color = Color\.White, fontSize = 16\.sp, fontWeight = FontWeight\.Bold, modifier = Modifier\.weight\(1f\)\).*?\n\s*\}\n\s*AnimatedVisibility'

# I will write a custom replace using str.replace to make it simple.
