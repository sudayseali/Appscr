with open('app/src/main/java/com/noxscreen/app/BlackScreenService.kt', 'r') as f:
    content = f.read()

target_failure = """                onFailure = {
                    blackoutView?.visibility = android.view.View.VISIBLE
                    isUnlockScreenVisible = false
                    aodContainer?.visibility = android.view.View.GONE
                    unlockButton?.visibility = android.view.View.GONE
                    handler.removeCallbacks(resetToBlackRunnable)
                    handler.postDelayed(resetToBlackRunnable, 10000)
                }"""

replacement_failure = """                onFailure = {
                    blackoutView?.visibility = android.view.View.VISIBLE
                    isUnlockScreenVisible = false
                    aodContainer?.visibility = android.view.View.GONE
                    unlockButton?.visibility = android.view.View.GONE
                    
                    showErrorShakeAnimation()
                    
                    handler.removeCallbacks(resetToBlackRunnable)
                    handler.postDelayed(resetToBlackRunnable, 10000)
                }"""

target_setup = """    @android.annotation.SuppressLint("ClickableViewAccessibility")
    private fun setupBlackoutView() {"""

replacement_setup = """    private var errorLockIcon: ImageView? = null

    private fun showErrorShakeAnimation() {
        errorLockIcon?.visibility = View.VISIBLE
        val shake = android.view.animation.TranslateAnimation(0f, 20f, 0f, 0f)
        shake.duration = 50
        shake.repeatMode = android.view.animation.Animation.REVERSE
        shake.repeatCount = 5
        shake.setAnimationListener(object : android.view.animation.Animation.AnimationListener {
            override fun onAnimationStart(animation: android.view.animation.Animation?) {}
            override fun onAnimationRepeat(animation: android.view.animation.Animation?) {}
            override fun onAnimationEnd(animation: android.view.animation.Animation?) {
                errorLockIcon?.visibility = View.GONE
            }
        })
        errorLockIcon?.startAnimation(shake)
    }

    @android.annotation.SuppressLint("ClickableViewAccessibility")
    private fun setupBlackoutView() {"""

target_addview = """            addView(unlockButton, FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, 
                FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
                gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                bottomMargin = 150
            })
        }"""

replacement_addview = """            addView(unlockButton, FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, 
                FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
                gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                bottomMargin = 150
            })
            
            errorLockIcon = ImageView(this@BlackScreenService).apply {
                setImageResource(R.drawable.ic_lock)
                setColorFilter(Color.RED)
                visibility = View.GONE
            }
            addView(errorLockIcon, FrameLayout.LayoutParams(120, 120).apply {
                gravity = Gravity.CENTER
            })
        }"""

if target_failure in content and target_setup in content and target_addview in content:
    content = content.replace(target_failure, replacement_failure)
    content = content.replace(target_setup, replacement_setup)
    content = content.replace(target_addview, replacement_addview)
    with open('app/src/main/java/com/noxscreen/app/BlackScreenService.kt', 'w') as f:
        f.write(content)
    print("Patched BlackScreenService")
else:
    print("Targets not found")
