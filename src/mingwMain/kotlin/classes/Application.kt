package classes

import classes.session.writeLobbyFiles
import kotlinx.cinterop.*
import libui.ktx.*
import platform.posix.ctime
import platform.posix.time
import platform.posix.time_tVar

fun main() {
    displayAppWindow()
}

private var statusText: String = "DISCONNECTED ❌"
private var guiApi: MutableList<PlayerGui> = ArrayList()

lateinit private var debugScroll: TextArea
lateinit private var debugText0: TextField
lateinit private var debugText1: TextField

fun displayAppWindow() = appWindow("gearNet", 600, 400) {
    val session = Session()
    hbox {
        vbox {
            hbox { debugText0 = textfield { readonly = true; enabled = false; label("Player 1 HP") } }
            hbox { debugText1 = textfield { readonly = true; enabled = false; label("Player 2 HP") } }
            hbox { debugScroll = textarea { readonly = true; stretchy = true }
            }
        }
        vbox { stretchy = true
            for (i in 0..7) {
                guiApi.add(PlayerGui())
                guiApi.get(i).playerGroup = group("") {
                    vbox { padded = false
                        hbox { padded = false
                            guiApi.get(i).character = textfield { readonly = true; value = ""; enabled = false; padded = false  }
                            guiApi.get(i).record = textfield { readonly = true; value = ""; enabled = false; padded = false  }
                            guiApi.get(i).bounty = textfield { readonly = true; value = ""; enabled = false; padded = false }
                            guiApi.get(i).rating = textfield { readonly = true; value = ""; enabled = false; padded = false }
                            guiApi.get(i).status = textfield { readonly = true; value = ""; enabled = false; padded = false  }
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

            if (session.connected()) {
                setAppStatus("CONNECTED \uD83D\uDCE1")
                if (session.updatePlayerData() || guiApi.get(0).playerGroup.title.equals("")) {
                    writeLobbyFiles(session.getAll())
                    updateAppUi(session.getAll())
                }
            } else {
                setAppStatus("DISCONNECTED ❌")
            }

        }
        title = "gearNet - $statusText ${session.getUpdateCounter()}"
        true
    }
}

private fun updateAppUi(uiUpdate: List<Player>) {
    debugText0.value = getMemData(MemData(0x01B18C78L, 0x9CCL, 4)).dataInt.toString()
    debugText1.value = getMemData(MemData(0x01B18C7CL, 0x9CCL, 4)).dataInt.toString()
    for (i in 0..7) {
        if (uiUpdate.size > i) {
            val player = uiUpdate.get(i)
            if (!guiApi.get(i).playerGroup.title.equals(player.getNameString())) guiApi.get(i).playerGroup.title = player.getNameString()
            if (!guiApi.get(i).character.value.equals(player.getCharacter(false))) guiApi.get(i).character.value = player.getCharacter(false)
            if (!guiApi.get(i).record.value.equals(player.getRecordString())) guiApi.get(i).record.value = player.getRecordString()
            if (!guiApi.get(i).bounty.value.equals(player.getBountyString())) guiApi.get(i).bounty.value = player.getBountyString()
            if (!guiApi.get(i).rating.value.equals(player.getRatingString())) guiApi.get(i).rating.value = player.getRatingString()
            if (!guiApi.get(i).status.value.equals(player.getStatusString())) guiApi.get(i).status.value = player.getStatusString()
            guiApi.get(i).character.enabled = true
            guiApi.get(i).record.enabled = true
            guiApi.get(i).bounty.enabled = true
            guiApi.get(i).rating.enabled = true
            guiApi.get(i).status.enabled = true
        } else {
            guiApi.get(i).playerGroup.title = ""
            guiApi.get(i).character.value = "-"
            guiApi.get(i).record.value = "- C / - W / - M"
            guiApi.get(i).bounty.value = "- W$"
            guiApi.get(i).rating.value = "Rating: -"
            guiApi.get(i).status.value = "0%"
            guiApi.get(i).character.enabled = false
            guiApi.get(i).record.enabled = false
            guiApi.get(i).bounty.enabled = false
            guiApi.get(i).rating.enabled = false
            guiApi.get(i).status.enabled = false
        }
    }
}

private fun setAppStatus(text: String) {
    statusText = text
}

class PlayerGui {
    lateinit var playerGroup: Group
    lateinit var character: TextField
    lateinit var record: TextField
    lateinit var bounty: TextField
    lateinit var rating: TextField
    lateinit var status: TextField
}

private val IC_INFO = "◆ "
private val IC_PING = "◇ "
private val IC_BOOL = "◑ "
private val IC_FUNC = "▶ "
private val IC_WARN = "\uD83D\uDCA3 "

fun logInfo(text: String) = IC_INFO //addLog("\n$IC_INFO $text")
fun logFunc(text: String) = IC_FUNC //addLog("\n$IC_FUNC $text")
fun logWarn(text: String) = addLog("\n$IC_WARN $text")
fun logBool(text: String, bool: Boolean): Boolean {
//    addLog("\n$IC_BOOL $text: ${bool}");
    return bool }

private fun addLog(logText: String) {
    debugScroll.append(logText.trimMargin())
}