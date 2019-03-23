package classes

import kotlinx.cinterop.*
import libui.ktx.*
import platform.posix.*

import platform.windows.*


object Host {
    const val STUB_ClashBountyImpl: Boolean = true
    const val DISPLAY_NAME: String = "Labryz"
    const val STEAM_ID: Long = 76561198128284333L

//    override fun isXrdRunning(): Boolean =false
//    override fun connectToXrd(steamUserId: Long, displayName: String): Boolean =false
//    override fun isXrdConnected(): Boolean =false
//    override fun getXrdData(): Set<PlayerData> =HashSet()
}

fun main() {
    ClashBountySession().start()
    writeOut("Some text to be written...\nBOUNTY: $100")
//    tryLibUi()
//    tryWinUi()
}


fun tryWinUi() {
    val message = StringBuilder()
    memScoped {
        val buffer = allocArray<UShortVar>(MAX_PATH)
        GetModuleFileNameW(null, buffer, MAX_PATH)
        val path = buffer.toKString().split("\\").dropLast(1).joinToString("\\")
        message.append("appended text $path\n")
    }
    MessageBoxW(null, "message:\nsent to a box!\n$message",
        "watup~", (MB_OK or MB_ICONASTERISK).convert())
}

fun tryLibUi() = appWindow(title = "Hello", width = 320, height = 240) {
    vbox { lateinit var scroll: TextArea

        button("hey you: click me!") { action {
            scroll.append("""
            |Hello, World!  Ciao, mondo!
            |Привет, мир!  你好，世界！
            |
            |""".trimMargin())
        } }
        scroll = textarea {
            readonly = true
            stretchy = true
        }
    }
}

