package pl.ejdev.zwojeIjPlugin.components

internal class PageBuilder(
    private val viewer: PDFViewer
) : Runnable {
    var value: Int = 0
    var timeout: Long = 0
    var anim: Thread? = null

    @Synchronized
    fun keyTyped(keyValue: Int) {
        value = value * 10 + keyValue
        timeout = System.currentTimeMillis() + TIMEOUT
        if (anim == null) {
            anim = Thread(this)
            anim!!.start()
        }
    }

    override fun run() {
        var now: Long
        var then: Long
        synchronized(this) {
            now = System.currentTimeMillis()
            then = timeout
        }
        while (now < then) {
            try {
                Thread.sleep(timeout - now)
            } catch (_: InterruptedException) {
            }
            synchronized(this) {
                now = System.currentTimeMillis()
                then = timeout
            }
        }
        synchronized(this) {
            viewer.gotoPage(value - 1)
            anim = null
            value = 0
        }
    }
}