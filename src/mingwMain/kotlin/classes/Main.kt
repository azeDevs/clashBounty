package classes

import classes.memscan.ClashBountyImplStub
import classes.output.*
import classes.session.ClashBountyApi
import kotlinx.coroutines.*
import kotlinx.coroutines.NonCancellable.isActive
import libui.uiNewWindow
import kotlin.coroutines.CoroutineContext


object Host {
    const val DISPLAY_NAME: String = "Labryz"
    const val STEAM_ID: Long = 76561198128284333L
}

fun getCBImpl(): ClashBountyApi = ClashBountyImplStub()

fun main() {
    logInfo("start")
    displayAppWindow()

//    GlobalScope.launch {
//        delay(16)
//        if (autoRefresh && scroll.visible) refreshConsole()
//    }
}