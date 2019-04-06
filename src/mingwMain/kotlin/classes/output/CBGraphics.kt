package classes.output

import classes.session.*
import kotlinx.cinterop.CPointer
import libui.ktx.*
import platform.posix.*


fun displayAppWindow() = appWindow("gearNet: Clash Bounty", 320, 240) {
    vbox { lateinit var console: TextArea

        button("Connect to Guilty Gear Xrd") { action {
            ClashBountySession().start()
            console.value = consoleLogs.toString().trimMargin()
        } }

        console = textarea { readonly = true; stretchy = true; value = consoleLogs.toString().trimMargin() }

        button("REFRESH") { action {
            console.value = ""
            console.append(consoleLogs.toString().trimMargin()) } }
        hbox {

            button("Test") { action {
                logInfo("START setLobbyStats")
                setLobbyStats()
                logInfo("CEASE setLobbyStats")
            } }

            button("Add Player") { action {
                logInfo("START playersData.put")
                playersData.put(123, Player(PlayerData("testName", 123)))
                playersData.forEach { data ->
                    logInfo("name: ${data.value.getDisplayNameString()}")
                    logInfo("place: ${data.value.getPlacingString()}")
                    logInfo("bounty: ${data.value.getBountyScoreString()}")
                    logInfo("risk rating: ${data.value.getRiskRatingString()}")
                }
                logInfo("player added")
            } }

        }

    }
}

private fun updateUiText() {
    val fp: CPointer<FILE>? = fopen("cb_ui.txt" ,"a")
    fprintf(fp, getFullscreen())
    fclose(fp)
}

fun getFullscreen(): String {
    var out = ""
    for (i in 1..7) out += "█████████████████████"
    return out
}

//
//var autoRefresh = false
//lateinit var scroll: TextArea
//lateinit var btnNormRefresh: Button
//lateinit var btnAutoRefresh: Button
//
//fun refreshConsole() {
//    scroll.value = consoleLogs.toString().trimMargin()
//}
//
//fun displayAppWindow2() = appWindow("gearNet: Clash Bounty", 320, 240) {
//    vbox {
//        button("Connect to Guilty Gear Xrd") {
//            action { ClashBountySession().start() }
//        }
//
//        scroll = textarea { readonly = true; stretchy = true; refreshConsole() }
//
//        btnNormRefresh = button("REFRESH") { action { refreshConsole() } }
//
//        btnAutoRefresh = button("Autorefresh: DISABLED") {
//            action {
//                if (autoRefresh) {
//                    btnAutoRefresh.text = "Autorefresh: DISABLED"
//                    btnNormRefresh.enabled = true
//                    autoRefresh = false
//                } else {
//                    btnAutoRefresh.text = "Autorefresh: ENABLED"
//                    btnNormRefresh.enabled = false
//                    autoRefresh = true
//                }
//            }
//        }
//    }
//}
//
//
//
//
