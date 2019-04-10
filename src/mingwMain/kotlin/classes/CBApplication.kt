package classes

import libui.ktx.*

lateinit var statusText: TextField
lateinit var connectBtn: Button
lateinit var consoleText: TextArea
lateinit var refreshBtn: Button

fun displayAppWindow() = appWindow("gearNet", 640, 480) {
    var session = Session()
    var playerGuis: MutableList<PlayerGui> = ArrayList()

    vbox {
        hbox {
            connectBtn = button(text = "Connect to GGXrd") {
                action {
                    statusText.value = "Xrd is not connected."
                    if (!session.updatePlayerData().isEmpty()) {
                        statusText.value = "Xrd Connected!"
                        refreshBtn.enabled = true
                    }
                }
            }
            statusText = textfield { readonly = true; value = "..." }
        }
        hbox {
            vbox {
                consoleText = textarea { readonly = true; stretchy = true; value = ""; }
                refreshBtn = button(text = "Get lobby update") {
                    action {
                        updatePlayerGuis(playerGuis, session.updatePlayerData())
                    }
                }
                refreshBtn.enabled = false
            }
            vbox {
                for (i in 0..7) {
                    playerGuis.add(PlayerGui())
                    playerGuis.get(i).playerForm = group("") {
                        vbox {
                            hbox {
                                playerGuis.get(i).playerCharacter = textfield { readonly = true; value = ""; enabled = false }
                                playerGuis.get(i).playerMatchesPlayed =
                                    textfield { readonly = true; value = "- / -"; enabled = false }
                            }
                            playerGuis.get(i).playerLoadPercent = progressbar { value = 0; enabled = false; visible = false }
                        }
                    }
                }
            }
        }
    }
}


private fun updatePlayerGuis(playerGui:MutableList<PlayerGui>, xrdData: List<Player>) {
    logFunc("updatePlayerGui")
    if (!xrdData.isEmpty()) {
        logInfo("LOBBY UPDATED ${xrdData.size}")
        for (i in 0..7) {
            if (xrdData.size > i) {
                playerGui.get(i).playerForm.title = "${xrdData.get(i).getName()}"
                playerGui.get(i).playerCharacter.value = "${xrdData.get(i).getCharacter()}"
                playerGui.get(i).playerMatchesPlayed.value = "${xrdData.get(i).getMatchesPlayed()} / ${xrdData.get(i).getMatchesWon()}"
                playerGui.get(i).playerLoadPercent.value = xrdData.get(i).getLoadPercent()
                playerGui.get(i).playerCharacter.enabled = true
                playerGui.get(i).playerMatchesPlayed.enabled = true
                playerGui.get(i).playerLoadPercent.enabled = true
            } else {
                playerGui.get(i).playerForm.title = ""
                playerGui.get(i).playerCharacter.value = ""
                playerGui.get(i).playerMatchesPlayed.value = ""
                playerGui.get(i).playerLoadPercent.value = 0
                playerGui.get(i).playerCharacter.enabled = false
                playerGui.get(i).playerMatchesPlayed.enabled = false
                playerGui.get(i).playerLoadPercent.enabled = false
            }
        }

    }
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
    consoleText.append(logText)
    consoleText.show()
}

class PlayerGui {
    lateinit var playerForm: Group
    lateinit var playerCharacter: TextField
    lateinit var playerMatchesPlayed: TextField
    lateinit var playerLoadPercent: ProgressBar
}