#!/bin/bash
sed -i 's/if (intent?.action == "BIOMETRIC_SUCCESS") {/if (false) {/g' app/src/main/java/com/noxscreen/app/BlackScreenService.kt
sed -i 's/else if (intent?.action == "BIOMETRIC_FAILED") {/else if (false) {/g' app/src/main/java/com/noxscreen/app/BlackScreenService.kt
