with open('app/src/main/java/com/noxscreen/app/BlackScreenService.kt', 'r') as f:
    content = f.read()

target_move = """                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - initialTouchX).toInt()
                    val dy = (event.rawY - initialTouchY).toInt()
                    if (abs(dx) > 10 || abs(dy) > 10) {
                        isClick = false
                    }
                    params.x = initialX + dx
                    params.y = initialY + dy
                    windowManager.updateViewLayout(floatingView, params)
                    true
                }"""

replacement_move = """                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - initialTouchX).toInt()
                    val dy = (event.rawY - initialTouchY).toInt()
                    if (abs(dx) > 10 || abs(dy) > 10) {
                        isClick = false
                    }
                    
                    val metrics = resources.displayMetrics
                    val maxX = metrics.widthPixels - v.width
                    val maxY = metrics.heightPixels - v.height
                    
                    var newX = initialX + dx
                    var newY = initialY + dy
                    
                    if (newX < 0) newX = 0
                    if (newX > maxX) newX = maxX
                    if (newY < 0) newY = 0
                    if (newY > maxY) newY = maxY
                    
                    params.x = newX
                    params.y = newY
                    
                    windowManager.updateViewLayout(floatingView, params)
                    true
                }"""

target_update = """    private fun updateFloatingBubbleStyle() {
        val config = smartAutomationManager.settings.getConfig()
        val size = (150 * config.floatingLockSize).toInt()"""

replacement_update = """    private fun updateFloatingBubbleStyle() {
        val config = smartAutomationManager.settings.getConfig()
        if (config.hideFloatingButton) {
            floatingView?.visibility = View.GONE
        } else {
            floatingView?.visibility = View.VISIBLE
        }
        val size = (150 * config.floatingLockSize).toInt()"""

if target_move in content and target_update in content:
    content = content.replace(target_move, replacement_move)
    content = content.replace(target_update, replacement_update)
    with open('app/src/main/java/com/noxscreen/app/BlackScreenService.kt', 'w') as f:
        f.write(content)
    print("Patched BlackScreenService")
else:
    print("Targets not found")
