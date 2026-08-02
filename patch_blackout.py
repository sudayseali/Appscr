import sys

with open('app/src/main/java/com/noxscreen/app/BlackoutActivity.kt', 'r') as f:
    content = f.read()

target = """    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable("""

replacement = """    val fadeAlpha = remember { androidx.compose.animation.core.Animatable(0f) }
    LaunchedEffect(Unit) {
        fadeAlpha.animateTo(
            targetValue = 1f,
            animationSpec = androidx.compose.animation.core.tween(
                durationMillis = 2000,
                easing = androidx.compose.animation.core.FastOutSlowInEasing
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = fadeAlpha.value))
            .clickable("""

content = content.replace(target, replacement)

with open('app/src/main/java/com/noxscreen/app/BlackoutActivity.kt', 'w') as f:
    f.write(content)

