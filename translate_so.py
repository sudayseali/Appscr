import os

so_strings = {
    "app_name": "NoxScreen Pro",
    "eco_screen_optimizer": "WANAAGJIYAHA MURAAYADA ECO",
    "energy_saved": "Tamarta La Keydiyay",
    "screen_off": "Muraayada Dansan",
    "display_settings": "Dejinta Muuqaalka",
    "always_on_display": "Muuqaalka Had iyo jeer-Daaran",
    "oled_pixel_shift": "Wareejinta Pixel ee OLED",
    "privacy_tint": "Shaandheynta Khaaska ah",
    "smart_triggers": "Ficilada Casriga ah",
    "pocket_mode": "Habka Jeebka", "sleep_timer": "Waqtiga Hurdada",
    "floating_action_button": "Batoonka Ficilka Sabeynaya",
    "floating_lock_style": "Qaabka Qufulka Sabeynaya",
    "floating_lock_size": "Xajmiga Qufulka Sabeynaya",
    "wake_gesture": "Tilmaamaha Toosinta",
    "security": "Amniga",
    "enable_biometric": "Daar Aqoonsiga Nafleyda",
    "usage_limits": "Xaddidaadda Isticmaalka",
    "overlay_permission": "Ogolaanshaha Dusha ayaa Loo Baahan Yahay",
    "tap_to_grant": "Taabo si aad u siiso marin u helka asalka.",
    "start": "Bilow",
    "stop": "Jooji"
}

os.makedirs("app/src/main/res/values-so", exist_ok=True)
with open("app/src/main/res/values-so/strings.xml", "w", encoding="utf-8") as f:
    f.write('<?xml version="1.0" encoding="utf-8"?>\n<resources>\n')
    for key, val in so_strings.items():
        val = val.replace("'", "\\'")
        f.write(f'    <string name="{key}">{val}</string>\n')
    f.write('</resources>\n')
