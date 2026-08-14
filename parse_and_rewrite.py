import re

with open('app/src/main/java/com/noxscreen/app/MainActivity.kt', 'r') as f:
    content = f.read()

# We want to replace from "// Expandable Settings Cards" up to "ExpandableConfigSection(\n                title = \"Focus Mode\","

start_marker = "            // Expandable Settings Cards"
end_marker = "            ExpandableConfigSection(\n                title = \"Focus Mode\","

start_idx = content.find(start_marker)
end_idx = content.find(end_marker)

if start_idx == -1 or end_idx == -1:
    print("Markers not found")
    exit(1)

old_section = content[start_idx:end_idx]

# Extract specific components using string splitting or regex from old_section
def extract_block(start_str, end_str_after):
    s = old_section.find(start_str)
    e = old_section.find(end_str_after, s)
    if s == -1 or e == -1:
        print(f"Failed to find block: {start_str}")
        return ""
    return old_section[s:e]

aod_block = extract_block("SmartTriggerCard(\n                    title = stringResource(R.string.always_on_display),", "                SmartTriggerCard(\n                    title = stringResource(R.string.oled_pixel_shift),")

oled_block = extract_block("SmartTriggerCard(\n                    title = stringResource(R.string.oled_pixel_shift),", "                SmartTriggerCard(\n                    title = stringResource(R.string.skip_unlock_screen),")

skip_unlock_block = extract_block("SmartTriggerCard(\n                    title = stringResource(R.string.skip_unlock_screen),", "            }\n\n            Spacer(modifier = Modifier.height(16.dp))\n\n            ExpandableConfigSection(\n                title = stringResource(R.string.smart_triggers),")

pocket_mode_block = extract_block("SmartTriggerCard(\n                    title = stringResource(R.string.pocket_mode),", "                SmartTriggerCard(\n                    title = stringResource(R.string.shake_to_wake),")

shake_to_wake_block = extract_block("SmartTriggerCard(\n                    title = stringResource(R.string.shake_to_wake),", "                SmartTriggerCard(\n                    title = stringResource(R.string.floating_action_button),")

floating_action_block = extract_block("SmartTriggerCard(\n                    title = stringResource(R.string.floating_action_button),", "                Text(\n                    text = stringResource(R.string.floating_lock_style),")

floating_lock_style_block = extract_block("Text(\n                    text = stringResource(R.string.floating_lock_style),", "                Text(\n                    text = stringResource(R.string.floating_lock_size),")

floating_lock_size_block = extract_block("Text(\n                    text = stringResource(R.string.floating_lock_size),", "                Text(\n                    text = stringResource(R.string.wake_gesture),")

taps_to_wake_block = extract_block("Text(\n                    text = stringResource(R.string.wake_gesture),", "            }\n\n            Spacer(modifier = Modifier.height(16.dp))\n\n            ExpandableConfigSection(\n                title = stringResource(R.string.security),")

biometric_block = extract_block("ZenithSwitchRow(\n                    title = stringResource(R.string.enable_biometric),", "            }\n\n            Spacer(modifier = Modifier.height(16.dp))")

# Update Biometric block text
biometric_block = biometric_block.replace(
    "subtitle = \"Use fingerprint or face recognition to access app settings\",",
    "subtitle = \"Require fingerprint to unlock screen and stop service\","
)

language_block = "                LanguageCard()\n"

new_section = f"""            // Expandable Settings Cards
            ExpandableConfigSection(
                title = "Screen & Display",
                subtitle = "Clock, always-on display & pixel shift",
                icon = Icons.Default.DisplaySettings,
                iconColor = ZenithAccent,
                badgeText = "Display",
                badgeColor = ZenithAccent,
                isExpanded = false
            ) {{
{aod_block}
{oled_block}
            }}

            Spacer(modifier = Modifier.height(16.dp))

            ExpandableConfigSection(
                title = "Controls & Overlays",
                subtitle = "Unlock methods & floating buttons",
                icon = Icons.Default.TouchApp,
                iconColor = Color(0xFFAB47BC),
                badgeText = "Controls",
                badgeColor = Color(0xFFAB47BC),
                isExpanded = false
            ) {{
{skip_unlock_block}
{floating_action_block}
{floating_lock_style_block}
{floating_lock_size_block}
            }}

            Spacer(modifier = Modifier.height(16.dp))

            ExpandableConfigSection(
                title = "Automation (Sensors)",
                subtitle = "Pocket mode & shake gestures",
                icon = Icons.Default.Sensors,
                iconColor = ZenithSecondary,
                badgeText = "Sensors",
                badgeColor = ZenithSecondary,
                isExpanded = false
            ) {{
{pocket_mode_block}
{shake_to_wake_block}
{taps_to_wake_block}
            }}

            Spacer(modifier = Modifier.height(16.dp))

            ExpandableConfigSection(
                title = stringResource(R.string.security),
                subtitle = "Protect app access & privacy controls",
                icon = Icons.Default.Security,
                iconColor = ZenithCyan,
                badgeText = "Biometric",
                badgeColor = ZenithCyan,
                isExpanded = false
            ) {{
{biometric_block}
            }}

            Spacer(modifier = Modifier.height(16.dp))

            ExpandableConfigSection(
                title = "General",
                subtitle = "App language and preferences",
                icon = Icons.Default.Settings,
                iconColor = Color.Gray,
                badgeText = "General",
                badgeColor = Color.Gray,
                isExpanded = false
            ) {{
{language_block}
            }}

            Spacer(modifier = Modifier.height(16.dp))

"""

new_content = content[:start_idx] + new_section + content[end_idx:]

with open('app/src/main/java/com/noxscreen/app/MainActivity.tmp.kt', 'w') as f:
    f.write(new_content)

print("Rewrote MainActivity successfully.")
