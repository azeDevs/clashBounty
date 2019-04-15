package classes

import kotlinx.cinterop.*
import libui.ktx.*
import platform.posix.*

//lateinit var consoleText: TextArea
private var statusText: String = "DISCONNECTED ❌"
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
                            guiApi.get(i).playerBounty = textfield { readonly = true; value = "- W$"; enabled = false }
                            hbox {
                                guiApi.get(i).playerMatchesPlayed = textfield { readonly = true; value = "- / -"; enabled = false; title = "W/M" }
                                guiApi.get(i).playerRating = textfield { readonly = true; value = "-"; enabled = false; title = "Rating" }
                                guiApi.get(i).playerChain = textfield { readonly = true; value = "-"; enabled = false; title = "Chain" }
                            }
//                            guiApi.get(i).playerLoadPercent = textfield { readonly = true; value = ""; enabled = false; }
                        }
                        hbox {
                            textarea { title = "testTitle Lorem"; enabled = true;  }
                            textarea { title = "testTitle Ipsum"; enabled = false;  }
                            textarea { title = "testTitle Dolor"; enabled = false;  }
//                            guiApi.get(i).playerMatchesPlayed = textfield { readonly = true; value = "- / -"; enabled = false }
//                            guiApi.get(i).playerRating = textfield { readonly = true; value = "Rating: -"; enabled = false }
//                            guiApi.get(i).playerChain = textfield { readonly = true; value = "Chain: -"; enabled = false }
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
                        if (!guiApi.get(i).playerBounty.value.equals("${guiUpdate.get(i).getBounty()} W$")) guiApi.get(i).playerBounty.value = "${guiUpdate.get(i).getBounty()} W$"
                        if (!guiApi.get(i).playerLoadPercent.value.equals("${guiUpdate.get(i).getLoadPercent()}%")) guiApi.get(i).playerLoadPercent.value = "${guiUpdate.get(i).getLoadPercent()}%"

                        if (!guiApi.get(i).playerMatchesPlayed.value.equals("${guiUpdate.get(i).getMatchesWon()} W / ${guiUpdate.get(i).getMatchesPlayed()} M")) guiApi.get(i).playerMatchesPlayed.value = "${guiUpdate.get(i).getMatchesWon()} W / ${guiUpdate.get(i).getMatchesPlayed()} M"
                        if (!guiApi.get(i).playerChain.value.equals("Chain: ${guiUpdate.get(i).getChain()}")) guiApi.get(i).playerChain.value = "Chain: ${guiUpdate.get(i).getChain()}"
                        if (!guiApi.get(i).playerRating.value.equals("Rating: ${guiUpdate.get(i).getRiskRating()}")) guiApi.get(i).playerRating.value = "Rating: ${guiUpdate.get(i).getRiskRating()}"

                        if (!guiApi.get(i).playerCharacter.isEnabled()) guiApi.get(i).playerCharacter.enabled = true
                        if (!guiApi.get(i).playerBounty.isEnabled()) guiApi.get(i).playerBounty.enabled = true
                        if (!guiApi.get(i).playerLoadPercent.isEnabled()) guiApi.get(i).playerLoadPercent.enabled = true

                        if (!guiApi.get(i).playerMatchesPlayed.isEnabled()) guiApi.get(i).playerMatchesPlayed.enabled = true
                        if (!guiApi.get(i).playerChain.isEnabled()) guiApi.get(i).playerChain.enabled = true
                        if (!guiApi.get(i).playerRating.isEnabled()) guiApi.get(i).playerRating.enabled = true
                    } else {
                        guiApi.get(i).playerForm.title = ""

                        guiApi.get(i).playerCharacter.value = ""
                        guiApi.get(i).playerBounty.value = "- W$"
                        guiApi.get(i).playerLoadPercent.value = ""

                        guiApi.get(i).playerMatchesPlayed.value = "- / -"
                        guiApi.get(i).playerChain.value = "Chain: -"
                        guiApi.get(i).playerRating.value = "Rating: -"

                        guiApi.get(i).playerCharacter.enabled = false
                        guiApi.get(i).playerBounty.enabled = false
                        guiApi.get(i).playerLoadPercent.enabled = false

                        guiApi.get(i).playerMatchesPlayed.enabled = false
                        guiApi.get(i).playerChain.enabled = false
                        guiApi.get(i).playerRating.enabled = false
                    }

                }
            } else {
                setAppStatus("DISCONNECTED ❌")
            }

        }
        title = "gearNet - $statusText ${session.getUpdateCounter()}"
        true
    }
}

private fun setAppStatus(text: String) {
    statusText = text
}

class PlayerGui {
    lateinit var playerForm: Group

    lateinit var playerCharacter: TextField
    lateinit var playerBounty: TextField
    lateinit var playerLoadPercent: TextField

    lateinit var playerMatchesPlayed: TextField
    lateinit var playerChain: TextField
    lateinit var playerRating: TextField
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
fun logBool(text: String, bool: Boolean): Boolean { addLog("\n$IC_BOOL $text: ${bool}");return bool }

private fun addLog(logText: String) {
    appendToTextFile("log", logText)
}

fun getFullscreen(): String {
    var out = ""
    for (i in 1..96) out += "█"
    return out
}