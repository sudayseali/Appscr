import sys

with open('app/src/main/java/com/noxscreen/app/BlackScreenService.kt', 'r') as f:
    content = f.read()

target = """    private fun showFloatingBubbleInternal() {
        smartAutomationManager.stopSleepTimer()
        sleepTimerTextView?.visibility = View.GONE"""

replacement = """    private fun showFloatingBubbleInternal() {
        handler.removeCallbacks(timeUpdater)
        smartAutomationManager.stopSleepTimer()
        sleepTimerTextView?.visibility = View.GONE"""

content = content.replace(target, replacement)

with open('app/src/main/java/com/noxscreen/app/BlackScreenService.kt', 'w') as f:
    f.write(content)
