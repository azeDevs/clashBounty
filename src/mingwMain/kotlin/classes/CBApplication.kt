package classes

import kotlinx.cinterop.*
import libui.ktx.*
import libui.ktx.draw.text
import platform.posix.*

//lateinit var consoleText: TextArea
private var statusText: String = "❌"
private var guiApi: MutableList<PlayerGui> = ArrayList()

fun displayAppWindow() = appWindow("gearNet", 640, 480) {
    val session = Session()

    hbox {
        vbox {
//            consoleText = textarea { readonly = true; stretchy = true; value = ""; visible = true }
        }
        vbox {
            for (i in 0..7) {
                guiApi.add(PlayerGui())
                guiApi.get(i).playerForm = group("") {
                    vbox {
                        hbox {
                            guiApi.get(i).playerCharacter = textfield { readonly = true; value = ""; enabled = false }
                            guiApi.get(i).playerMatchesPlayed = textfield { readonly = true; value = "- / -"; enabled = false }
                            guiApi.get(i).playerBounty = textfield { readonly = true; value = "- W$"; enabled = false }
                        }
                        hbox {
                            guiApi.get(i).playerLoadPercent = progressbar { value = 0; enabled = false; visible = true }
                            guiApi.get(i).playerRiskRating = drawarea {
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
    onTimer(256) {
        memScoped {
            val now = alloc<time_tVar>().apply { value = time(null) }
            val str = ctime(now.ptr)!!.toKString()
            logInfo("UI UPDATE: ${str}")

            if (session.connected()) {
                setAppStatus("CONNECTED \uD83D\uDCE1")
                session.updatePlayerData()
                val guiUpdate = session.getAll()
                for (i in 0..7) {
                    if (guiUpdate.size > i) {
                        if (!guiApi.get(i).playerForm.title.equals("${guiUpdate.get(i).getName()}")) guiApi.get(i).playerForm.title = "${guiUpdate.get(i).getName()}"
                        if (!guiApi.get(i).playerCharacter.value.equals("${guiUpdate.get(i).getCharacter()}")) guiApi.get(i).playerCharacter.value = "${guiUpdate.get(i).getCharacter()}"
                        if (!guiApi.get(i).playerMatchesPlayed.value.equals("${guiUpdate.get(i).getMatchesWon()} / ${guiUpdate.get(i).getMatchesPlayed()}")) guiApi.get(i).playerMatchesPlayed.value = "${guiUpdate.get(i).getMatchesWon()} / ${guiUpdate.get(i).getMatchesPlayed()}"
                        if (guiUpdate.get(i).getLoadPercent() != 100) guiApi.get(i).playerLoadPercent.value = guiUpdate.get(i).getLoadPercent()
                        else guiApi.get(i).playerLoadPercent.value = 0
                        if (!guiApi.get(i).playerCharacter.isEnabled()) guiApi.get(i).playerCharacter.enabled = true
                        if (!guiApi.get(i).playerMatchesPlayed.isEnabled()) guiApi.get(i).playerMatchesPlayed.enabled = true
                        if (!guiApi.get(i).playerLoadPercent.isEnabled()) guiApi.get(i).playerLoadPercent.enabled = true
                        if (!guiApi.get(i).playerBounty.value.equals("${guiUpdate.get(i).getBounty()} W$")) guiApi.get(i).playerBounty.value = "${guiUpdate.get(i).getBounty()} W$"
                        if (!guiApi.get(i).playerRiskRating.isEnabled()) guiApi.get(i).playerRiskRating.enabled = true
                        if (!guiApi.get(i).playerBounty.isEnabled()) guiApi.get(i).playerBounty.enabled = true

                        guiApi.get(i).playerBounty.enabled = true
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
            } else {
                setAppStatus("❌")
            }

        }
        title = "gearNet - $statusText ${session.getUpdateCounter()} / ${session.getAll().size}"
        true
    }
}

private fun setAppStatus(text: String) {
    statusText = text
}

class PlayerGui {
    lateinit var playerForm: Group
    lateinit var playerCharacter: TextField
    lateinit var playerMatchesPlayed: TextField
    lateinit var playerLoadPercent: ProgressBar
    lateinit var playerBounty: TextField
    lateinit var playerRiskRating: DrawArea
}

private val IC_INFO = "> "
private val IC_BOOL = "? "
private val IC_FUNC = "ƒ "
private val IC_STEP = "… "
private val IC_WARN = "\uD83D\uDCA3 "

fun logInfo(text: String) = addLog("\n$IC_INFO $text")
fun logFunc(text: String) = addLog("\n$IC_FUNC $text")
fun logStep(text: String) = addLog("\n$IC_STEP $text")
fun logWarn(text: String) = addLog("\n$IC_WARN $text")
fun logBool(text: String, bool: Boolean): Boolean {
    addLog("\n$IC_BOOL $text: ${bool}");return bool }

private fun addLog(logText: String) {
    appendToTextFile("log", logText)
//    if (!consoleText.disposed) consoleText.append(text)
}

fun getFullscreen(): String {
    var out = ""
    for (i in 1..96) out += "█"
    return out
}