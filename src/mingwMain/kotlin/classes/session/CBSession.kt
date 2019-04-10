package classes.session

import classes.currPlayers
import kotlin.collections.Map.Entry

val NONE: Byte = 0x0
val IN_LOBBY: Byte = 0x1
val READYING: Byte = 0x2
val IN_MATCH: Byte = 0x3
var currentOverlay: Byte = NONE

fun updateOverlayState(xrdData: List<PlayerData>) {
    logFunc("updateOverlayState")
    var pendingOverlay: Byte = NONE
    when (currentOverlay) {
        NONE -> if (!xrdData.isEmpty()) pendingOverlay = IN_LOBBY
        IN_LOBBY -> xrdData.forEach { player -> if (Player(player).isLoading()) pendingOverlay = READYING }
        READYING -> { if (xrdData.filter { player -> Player(player).isLoading() }.size == 0) pendingOverlay = IN_MATCH }
        IN_MATCH -> xrdData.forEach { player -> if (Player(player).wonMatch()) pendingOverlay = IN_LOBBY }
        else -> { logWarn("Xrd lobby not found") }
    }
    currentOverlay = pendingOverlay

//    logStep("process player wins/losses")
//    currPlayers.forEach { player ->
//        if (player.value.lostMatch()) {
//            logStep("${player.value.data.displayName}: ${player.value.data.matchesWon.toInt()} / ${player.value.data.matchesPlayed.toInt()} (WON)")
//            player /* and do stuff for losing */
//        } else if (player.value.wonMatch()) {
//            logStep("${player.value.data.displayName}: ${player.value.data.matchesWon.toInt()} / ${player.value.data.matchesPlayed.toInt()} (LOST)")
//            player /* and do stuff for winning */
//        }
//    }

}
