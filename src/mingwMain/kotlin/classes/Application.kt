package classes

import classes.session.writeLobbyFiles
import kotlinx.cinterop.memScoped
import libui.ktx.*

fun main() {
    displayAppWindow()
}

private var statusText: String = "DISCONNECTED ❌"
private var guiApi: MutableList<PlayerGui> = ArrayList()
private var dataFields: MutableList<DataGui> = ArrayList()

lateinit private var debugButton: Button
lateinit private var logScroll: TextArea
var showhud = true
var forcehud = false

fun displayAppWindow() = appWindow("gearNet", 600, 400) {
    val session = Session()
    hbox {
        vbox {
            debugButton = button("Scoreboard: AUTO") {
                action {
                    showhud = !showhud
                    forcehud = true
                    if (showhud) debugButton.text = "Scoreboard: ENABLED"
                    else debugButton.text = "Scoreboard: DISABLED"
                    writeLobbyFiles(session.getAll())
                }
            }
            for (i in 0..MemLibrary.size-1) {
                dataFields.add(DataGui())
                dataFields.get(i).dataSource = MemLibrary.get(i)
                dataFields.get(i).dataField = textfield { readonly = true; value = "${MemLibrary.get(i).description}:"; enabled = false; padded = false }
            }

            logScroll = textarea(true) { readonly = true; stretchy = true; }
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
                            guiApi.get(i).status = textfield { readonly = true; value = ""; enabled = false; padded = false }
                        }
                    }
                }
            }
        }

    }
    onTimer(256) {
        memScoped {
            if (session.connected()) {
                setAppStatus("CONNECTED \uD83D\uDCE1")

                if (session.updatePlayerData() || guiApi.get(0).playerGroup.title.equals("") || solveForShowHud()) {
                    writeLobbyFiles(session.getAll())
                    updateAppUi(session.getAll())
                }
            } else setAppStatus("DISCONNECTED ❌")
        }
        title = "gearNet - $statusText ${session.getGamesCount()} ${session.getUpdateCounter()}"
        true
    }
}

private fun solveForShowHud(): Boolean {
    var wasSwitched = false

    for (i in 0..MemLibrary.size-1) dataFields.get(i).dataField.value = "${dataFields.get(i).dataSource.description}: ${getMemData(dataFields.get(i).dataSource).data}"

    // Toggle scoreboard on when HP values are no longer valid
    val p1hp = getMemData(MemData("Player 1 HP", longArrayOf(0x01B18C78L, 0x9CCL), 4)).data
    val p2hp = getMemData(MemData("Player 2 HP", longArrayOf(0x01B18C7CL, 0x9CCL), 4)).data
    if (p1hp >= 0 && p1hp <= 420 && p2hp >= 0 && p2hp <= 420) {
        if (showhud && !forcehud) {
            wasSwitched = true
            showhud = false
        }
    } else {
        if (!showhud) {
            wasSwitched = true
            showhud = true
        }
        forcehud = false
        debugButton.text = "Scoreboard: AUTO"
    }
    if (wasSwitched) forcehud = false
    return wasSwitched
}

private fun updateAppUi(uiUpdate: List<Player>) {
    for (i in 0..7) {
        if (uiUpdate.size > i) {
            val player = uiUpdate.get(i)
            if (!guiApi.get(i).playerGroup.title.equals(player.getNameString())) guiApi.get(i).playerGroup.title = player.getNameString()
            if (!guiApi.get(i).character.value.equals(player.getCharacter(false))) guiApi.get(i).character.value = player.getCharacter(false)
            if (!guiApi.get(i).record.value.equals(player.getRecordString())) guiApi.get(i).record.value = player.getRecordString()
            if (!guiApi.get(i).bounty.value.equals(player.getBountyString())) guiApi.get(i).bounty.value = player.getBountyString()
            if (!guiApi.get(i).rating.value.equals(player.getRatingString())) guiApi.get(i).rating.value = player.getRatingString()
            if (!guiApi.get(i).status.value.equals(player.getStatusString())) guiApi.get(i).status.value = player.getStatusString()

            if (player.isScoreboardWorthy()) {
                guiApi.get(i).character.enabled = true
                guiApi.get(i).record.enabled = true
                guiApi.get(i).bounty.enabled = true
                guiApi.get(i).rating.enabled = true
                guiApi.get(i).status.enabled = true
            } else {
                guiApi.get(i).character.enabled = false
                guiApi.get(i).record.enabled = false
                guiApi.get(i).bounty.enabled = false
                guiApi.get(i).rating.enabled = false
                guiApi.get(i).status.enabled = false
            }
        } else {
            guiApi.get(i).playerGroup.title = ""
            guiApi.get(i).character.value = ""
            guiApi.get(i).record.value = ""
            guiApi.get(i).bounty.value = ""
            guiApi.get(i).rating.value = ""
            guiApi.get(i).status.value = ""
            guiApi.get(i).character.enabled = false
            guiApi.get(i).record.enabled = false
            guiApi.get(i).bounty.enabled = false
            guiApi.get(i).rating.enabled = false
            guiApi.get(i).status.enabled = false
        }
    }
}

private fun setAppStatus(text: String) { statusText = text }

class PlayerGui {
    lateinit var playerGroup: Group
    lateinit var character: TextField
    lateinit var record: TextField
    lateinit var bounty: TextField
    lateinit var rating: TextField
    lateinit var status: TextField
}
class DataGui {
    lateinit var dataSource: MemData
    lateinit var dataField: TextField
}

fun logInfo(text: String) = addLog("◆ $text")
fun logWarn(text: String) = addLog("▶ $text")
private fun addLog(logText: String) { logScroll.value = "${logScroll.value}\n${logText}" }