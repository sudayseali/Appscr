import re

with open('app/src/main/java/com/noxscreen/app/MainActivity.kt', 'r') as f:
    content = f.read()

secure_ui_code = """
        setContent {
            MyApplicationTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF020612)
                ) {
                    var isAuthenticated by androidx.compose.runtime.remember { 
                        androidx.compose.runtime.mutableStateOf(com.noxscreen.app.security.AuthenticationManager.isAuthenticated(this@MainActivity)) 
                    }
                    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
                    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
                        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
                            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                                isAuthenticated = com.noxscreen.app.security.AuthenticationManager.isAuthenticated(this@MainActivity)
                            }
                        }
                        lifecycleOwner.lifecycle.addObserver(observer)
                        onDispose {
                            lifecycleOwner.lifecycle.removeObserver(observer)
                        }
                    }

                    if (!isAuthenticated) {
                        androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize().background(Color.Black))
                        return@Surface
                    }

                    var showSplash by remember { mutableStateOf(true) }"""

content = re.sub(
    r"""        setContent \{\s*MyApplicationTheme\(darkTheme = true\) \{\s*Surface\(\s*modifier = Modifier\.fillMaxSize\(\),\s*color = Color\(0xFF020612\)\s*\) \{\s*var showSplash by remember \{ mutableStateOf\(true\) \}""",
    secure_ui_code,
    content
)

with open('app/src/main/java/com/noxscreen/app/MainActivity.kt', 'w') as f:
    f.write(content)
