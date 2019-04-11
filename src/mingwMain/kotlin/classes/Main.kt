package classes

import classes.output.append
import classes.output.libuiDrawFont
import classes.output.makeAttributedString
import classes.session.*
import classes.session.Character.getCharacterName
import kotlinx.cinterop.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import libui.*
import libui.ktx.*
import libui.ktx.draw.*
import platform.posix.*
import kotlin.native.concurrent.Future
import kotlin.native.concurrent.TransferMode
import kotlin.native.concurrent.Worker

val xrdApi: ClashBountyApi = ClashBountyImpl()
var guiApi: MutableList<PlayerGui> = ArrayList()

class PlayerGui {
    lateinit var playerForm: Group
    lateinit var playerCharacter: TextField
    lateinit var playerMatchesPlayed: TextField
    lateinit var playerLoadPercent: ProgressBar
}

lateinit var statusText: TextField
lateinit var connectBtn: Button
lateinit var consoleText: TextArea
lateinit var refreshBtn: Button

var currPlayers: MutableMap<Long, Player> = HashMap()

fun main() {
    displayAppWindow()
}

private fun displayAppWindow() = appWindow("gearNet", 640, 480) {
    val xrdApi: ClashBountyApi = ClashBountyImpl()
    vbox {
        hbox {
            connectBtn = button(text = "Connect to GGXrd") {
                visible = false
                action {
                    statusText.value = "Xrd is not connected."
                    if (xrdApi.connectToXrd()) {
                        statusText.value = "Xrd Connected!"
                        refreshBtn.enabled = true
                    }
                }
            }
            statusText = textfield { readonly = true; value = "..." }
        }
        hbox {
            vbox {
                consoleText = textarea { readonly = true; stretchy = true; value = ""; visible = false }
                refreshBtn = button(text = "Get lobby data") { visible = false
                    action {
                        logXrdData()
                    }
                }
                refreshBtn.enabled = false
            }
            vbox {
                for (i in 0..7) {
                    guiApi.add(PlayerGui())
                    guiApi.get(i).playerForm = group("") {
                        vbox {
                            hbox {
                                guiApi.get(i).playerCharacter = textfield { readonly = true; value = ""; enabled = false }
                                guiApi.get(i).playerMatchesPlayed = textfield { readonly = true; value = "- / -"; enabled = false }
                                textfield { readonly = true; value = "- W$"; enabled = false }
                            }
                            hbox {
                                guiApi.get(i).playerLoadPercent = progressbar { value = 0; enabled = false; visible = true }
                                drawarea {
                                    val str = makeAttributedString("?")
                                    val value = 1
                                    val font = fontbutton() { visible = false }
                                    draw { text(str, font.value, it.AreaWidth, value.convert(), 0.0, 0.0) }
                                    stretchy = true
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    onTimer(1000) {
        memScoped {
            val now = alloc<time_tVar>().apply { value = time(null) }
            val str = ctime(now.ptr)!!.toKString()
            logInfo("UI UPDATE: ${str}")

            if (classes.xrdApi.isXrdRunning()) {
                setAppStatus("Xrd is running")
                connectBtn.enabled = true
                if (classes.xrdApi.connectToXrd()) {
                    setAppStatus("Xrd is connected")
                    refreshBtn.enabled = true
                    logXrdData()
                }

            } else {
                setAppStatus("Xrd not found")
                connectBtn.enabled = false
            }
        }
        true
    }
}


private fun logXrdData() {
    logFunc("logXrdData")
    if (xrdApi.connectToXrd()) {
        val xrdData = xrdApi.getXrdData().toList()

        logInfo("LOBBY UPDATED ${xrdData.size}")
        for (i in 0..7) {
            if (xrdData.size > i) {
                guiApi.get(i).playerForm.title = "${xrdData.get(i).displayName}"
                guiApi.get(i).playerCharacter.value = "${getCharacterName(xrdData.get(i).characterId)}"
                guiApi.get(i).playerMatchesPlayed.value = "${xrdData.get(i).matchesWon} / ${xrdData.get(i).matchesPlayed}"
                guiApi.get(i).playerLoadPercent.value = xrdData.get(i).loadingPct.toInt()
                guiApi.get(i).playerCharacter.enabled = true
                guiApi.get(i).playerMatchesPlayed.enabled = true
                guiApi.get(i).playerLoadPercent.enabled = true
            } else {
                guiApi.get(i).playerForm.title = ""
                guiApi.get(i).playerCharacter.value = ""
                guiApi.get(i).playerMatchesPlayed.value = "- / -"
                guiApi.get(i).playerLoadPercent.value = 0
                guiApi.get(i).playerCharacter.enabled = false
                guiApi.get(i).playerMatchesPlayed.enabled = false
                guiApi.get(i).playerLoadPercent.enabled = false
            }
        }
        updateOverlayState(xrdData)
        statusText.value = getOverlayStatus()
    }
}

private fun getOverlayStatus(): String {
    logFunc("getOverlayStatus")
    when (currentOverlay) {
        IN_LOBBY -> return "Overlay: IN_LOBBY"
        READYING -> return "Overlay: READYING"
        IN_MATCH -> return "Overlay: IN_MATCH"
        else -> return "Overlay: NONE"
    }
}

private fun setAppStatus(text: String) {
    statusText.value = text
}

fun appendToConsole(text: String) {
    consoleText.append(text)
}


