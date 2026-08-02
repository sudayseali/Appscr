import sys

with open('app/src/main/java/com/noxscreen/app/MainActivity.kt', 'r') as f:
    content = f.read()

target = """                            onClick = {
                                val newBlockedApps = if (autoConfig.blockedApps.isEmpty()) {
                                    setOf("com.google.android.youtube", "com.android.chrome", "com.facebook.katana", "com.instagram.android", "com.zhiliaoapp.musically", "com.snapchat.android", "com.whatsapp", "com.twitter.android", "com.google.android.apps.photos", "com.sec.android.gallery3d", "com.android.gallery3d")
                                } else {
                                    emptySet<String>()
                                }
                                autoConfig = autoConfig.copy(blockedApps = newBlockedApps)
                                automationSettings.updateConfig(autoConfig)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ZenithSecondary.copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        ) {
                            Text(if (autoConfig.blockedApps.isEmpty()) "Block Common Apps" else "Unblock All Apps", color = ZenithAccent)
                        }
                        if (autoConfig.blockedApps.isNotEmpty()) {
                            Text("Blocked: ${autoConfig.blockedApps.size} apps", color = ZenithTextMuted, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                        }"""

replacement = """                            onClick = {
                                val newBlockedApps = if (autoConfig.blockedApps.isEmpty()) {
                                    setOf("com.google.android.youtube", "com.android.chrome", "com.facebook.katana", "com.instagram.android", "com.zhiliaoapp.musically", "com.snapchat.android", "com.whatsapp", "com.twitter.android", "com.google.android.apps.photos", "com.sec.android.gallery3d", "com.android.gallery3d")
                                } else {
                                    emptySet<String>()
                                }
                                autoConfig = autoConfig.copy(blockedApps = newBlockedApps)
                                automationSettings.updateConfig(autoConfig)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ZenithSecondary.copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        ) {
                            Text(if (autoConfig.blockedApps.isEmpty()) "Block Apps (Socials & Gallery)" else "Unblock All Apps", color = ZenithAccent)
                        }
                        if (autoConfig.blockedApps.isNotEmpty()) {
                            Text("Blocked: ${autoConfig.blockedApps.size} apps (Snapchat, Gallery, Socials)", color = ZenithTextMuted, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                        }"""

if target in content:
    content = content.replace(target, replacement)
    with open('app/src/main/java/com/noxscreen/app/MainActivity.kt', 'w') as f:
        f.write(content)
    print("Replaced!")
else:
    print("Not found")
