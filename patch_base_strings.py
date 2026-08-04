import sys

base_strings = {
    "app_name": "NoxScreen Pro",
    "eco_screen_optimizer": "ECO SCREEN OPTIMIZER",
    "energy_saved": "Energy Saved",
    "screen_off": "Screen Off",
    "display_settings": "Display Settings",
    "always_on_display": "Always-On Display",
    "oled_pixel_shift": "OLED Pixel Shift",
    "privacy_tint": "Privacy Tint",
    "smart_triggers": "Smart Triggers",
    "pocket_mode": "Pocket Mode",
    "sleep_timer": "Sleep Timer (Battery Saver)",
    "floating_action_button": "Floating Action Button",
    "floating_lock_style": "Floating Lock Style",
    "floating_lock_size": "Floating Lock Size",
    "wake_gesture": "Wake Gesture (Taps)",
    "security": "Security",
    "enable_biometric": "Enable Biometric Authentication",
    "usage_limits": "Usage Limits (Focus Mode)",
    "overlay_permission": "Overlay Permission Required",
    "tap_to_grant": "Tap to grant access for background functionality.",
    "start": "Start",
    "stop": "Stop"
}

with open("app/src/main/res/values/strings.xml", "w", encoding="utf-8") as f:
    f.write('<?xml version="1.0" encoding="utf-8"?>\n<resources>\n')
    for key, val in base_strings.items():
        f.write(f'    <string name="{key}">{val}</string>\n')
    f.write('</resources>\n')
