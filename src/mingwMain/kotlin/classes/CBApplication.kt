package classes

import classes.Character.VE
import kotlinx.cinterop.*
import libui.ktx.*
import platform.posix.ctime
import platform.posix.time
import platform.posix.time_tVar

//lateinit var consoleText: TextArea
private var statusText: String = "DISCONNECTED ❌"
private var guiApi: MutableList<PlayerGui> = ArrayList()

private val fakeData = List(16) {
    Player(PlayerData("Foonameagain", 1L, VE, 0, 0, 0))
}

fun displayAppWindow() = appWindow("gearNet", 600, 400) {
    val session = Session()

    vbox { stretchy = true
        for (i in 0..7) {
            guiApi.add(PlayerGui())
            guiApi.get(i).playerForm = group("") {
                vbox { padded = false
                    hbox { padded = false
                        guiApi.get(i).playerCharacter = textfield { readonly = true; value = ""; enabled = false; padded = false  }
                        guiApi.get(i).playerRecord = textfield { readonly = true; value = ""; enabled = false; padded = false  }
                        guiApi.get(i).playerBounty = textfield { readonly = true; value = ""; enabled = false; padded = false }
                        guiApi.get(i).playerRating = textfield { readonly = true; value = ""; enabled = false; padded = false }
                        guiApi.get(i).playerLoading = textfield { readonly = true; value = ""; enabled = false; padded = false  }
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
                        if (!guiApi.get(i).playerForm.title.equals("${guiUpdate.get(i).getName()} [${guiUpdate.get(i).getId()}]")) guiApi.get(i).playerForm.title = "${guiUpdate.get(i).getName()} [${guiUpdate.get(i).getId()}]"
                        if (!guiApi.get(i).playerCharacter.value.equals("${guiUpdate.get(i).getCharacter()}")) guiApi.get(i).playerCharacter.value = "${guiUpdate.get(i).getCharacter()}"
                        if (!guiApi.get(i).playerRecord.value.equals("${guiUpdate.get(i).getChain()} C / ${guiUpdate.get(i).getMatchesWon()} W / ${guiUpdate.get(i).getMatchesPlayed()} M")) guiApi.get(i).playerRecord.value = "${guiUpdate.get(i).getChain()} C / ${guiUpdate.get(i).getMatchesWon()} W / ${guiUpdate.get(i).getMatchesPlayed()} M"
                        if (!guiApi.get(i).playerBounty.value.equals("${guiUpdate.get(i).getBounty()} W$")) guiApi.get(i).playerBounty.value = "${guiUpdate.get(i).getBounty()} W$"
                        if (!guiApi.get(i).playerRating.value.equals("Rating: ${guiUpdate.get(i).getRiskRating()}")) guiApi.get(i).playerRating.value = "Rating: ${guiUpdate.get(i).getRiskRating()}"
                        if (!guiApi.get(i).playerLoading.value.equals("${guiUpdate.get(i).getLoadPercent()}%")) guiApi.get(i).playerLoading.value = "${guiUpdate.get(i).getLoadPercent()}%"
//                        if (guiUpdate.get(i).inLobby) {
//                            guiApi.get(i).playerCharacter.enabled = true
//                            guiApi.get(i).playerRecord.enabled = true
//                            guiApi.get(i).playerBounty.enabled = true
//                            guiApi.get(i).playerRating.enabled = true
//                            guiApi.get(i).playerLoading.enabled = true
//                        } else {
//                            guiApi.get(i).playerCharacter.enabled = false
//                            guiApi.get(i).playerRecord.enabled = false
//                            guiApi.get(i).playerBounty.enabled = false
//                            guiApi.get(i).playerRating.enabled = false
//                            guiApi.get(i).playerLoading.enabled = false
//                        }
                    } else {
                        guiApi.get(i).playerForm.title = ""
                        guiApi.get(i).playerCharacter.value = "-"
                        guiApi.get(i).playerRecord.value = "- C / - W / - M"
                        guiApi.get(i).playerBounty.value = "- W$"
                        guiApi.get(i).playerRating.value = "Rating: -"
                        guiApi.get(i).playerLoading.value = "0%"

//                        guiApi.get(i).playerCharacter.enabled = false
//                        guiApi.get(i).playerRecord.enabled = false
//                        guiApi.get(i).playerBounty.enabled = false
//                        guiApi.get(i).playerRating.enabled = false
//                        guiApi.get(i).playerLoading.enabled = false
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
    lateinit var playerRecord: TextField
    lateinit var playerBounty: TextField
    lateinit var playerRating: TextField
    lateinit var playerLoading: TextField
}

private val IC_INFO = "> "
private val IC_BOOL = "? "
private val IC_FUNC = "ƒ "
private val IC_WARN = "\uD83D\uDCA3 "

fun logInfo(text: String) = addLog("\n$IC_INFO $text")
fun logFunc(text: String) = addLog("\n$IC_FUNC $text")
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