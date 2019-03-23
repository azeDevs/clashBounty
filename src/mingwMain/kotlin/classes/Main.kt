package classes

import kotlinx.cinterop.*
import platform.windows.*


object Host {
    const val STUB_ClashBountyImpl: Boolean = true
    const val DISPLAY_NAME: String = "Labryz"
    const val STEAM_ID: Long = 76561198128284333L
}

fun main() {
//    trylibui()
    ClashBountySession().start()
}


//fun messageBox() {
//    val message = StringBuilder()
//    memScoped {
//        val buffer = allocArray<UShortVar>(MAX_PATH)
//        GetModuleFileNameW(null, buffer, MAX_PATH)
//        val path = buffer.toKString().split("\\").dropLast(1).joinToString("\\")
//        message.append("appended text $path\n")
//    }
//    MessageBoxW(null, "message:\nsent to a box!\n$message",
//        "watup~", (MB_OK or MB_ICONASTERISK).convert())
//}
//
//
//
//fun trylibui() = appWindow(title = "Hello", width = 320, height = 240) {
//    vbox { lateinit var scroll: TextArea
//
//        button("libui говорит: click me!") { action {
//            scroll.append("""
//            |Hello, World!  Ciao, mondo!
//            |Привет, мир!  你好，世界！
//            |
//            |""".trimMargin())
//        } }
//        scroll = textarea {
//            readonly = true
//            stretchy = true
//        }
//    }
//}