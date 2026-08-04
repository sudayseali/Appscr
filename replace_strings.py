import re

with open('app/src/main/java/com/noxscreen/app/MainActivity.kt', 'r', encoding='utf-8') as f:
    content = f.read()

replacements = {
    '"ZENITH"': 'stringResource(R.string.app_name)',
    '"ECO SCREEN OPTIMIZER"': 'stringResource(R.string.eco_screen_optimizer)',
    '"Energy Saved"': 'stringResource(R.string.energy_saved)',
    '"Screen Off"': 'stringResource(R.string.screen_off)',
    '"Display Settings"': 'stringResource(R.string.display_settings)',
    '"Always-On Display"': 'stringResource(R.string.always_on_display)',
    '"OLED Pixel Shift"': 'stringResource(R.string.oled_pixel_shift)',
    '"Privacy Tint"': 'stringResource(R.string.privacy_tint)',
    '"Smart Triggers"': 'stringResource(R.string.smart_triggers)',
    '"Pocket Mode"': 'stringResource(R.string.pocket_mode)',
    '"Sleep Timer (Battery Saver)"': 'stringResource(R.string.sleep_timer)',
    '"Floating Action Button"': 'stringResource(R.string.floating_action_button)',
    '"Floating Lock Style"': 'stringResource(R.string.floating_lock_style)',
    '"Floating Lock Size"': 'stringResource(R.string.floating_lock_size)',
    '"Wake Gesture (Taps)"': 'stringResource(R.string.wake_gesture)',
    '"Security"': 'stringResource(R.string.security)',
    '"Enable Biometric Authentication"': 'stringResource(R.string.enable_biometric)',
    '"Usage Limits (Focus Mode)"': 'stringResource(R.string.usage_limits)',
    '"Overlay Permission Required"': 'stringResource(R.string.overlay_permission)',
    '"Tap to grant access for background functionality."': 'stringResource(R.string.tap_to_grant)',
}

# Need to import stringResource and R
import_stmt = "import androidx.compose.ui.res.stringResource\nimport com.noxscreen.app.R\n"
if "import androidx.compose.ui.res.stringResource" not in content:
    content = content.replace("import androidx.compose.ui.unit.sp", "import androidx.compose.ui.unit.sp\n" + import_stmt)

for old, new in replacements.items():
    content = content.replace(old, new)

with open('app/src/main/java/com/noxscreen/app/MainActivity.kt', 'w', encoding='utf-8') as f:
    f.write(content)

