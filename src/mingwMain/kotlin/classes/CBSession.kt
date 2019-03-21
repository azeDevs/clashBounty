package classes

import classes.LobbyStatus.IN_LOBBY
import classes.LobbyStatus.IN_MATCH
import classes.LobbyStatus.NONE
import classes.LobbyStatus.READYING
import kotlin.collections.Map.Entry

class ClashBountySession {
    val xrd: XrdDataManager = XrdDataManager()
    val playersData: MutableMap<Int, Player> = HashMap()
    var currentStatus: Byte = NONE
    var pendingStatus: Byte = NONE

    // TODO: Make this initiate the session's game loop
    fun start() {
        xrd.connect()
    }

    // TODO: Make this loop once xrd.isConnected()
    private fun refreshData() {
        var updatedData: Map<Int, Player> = xrd.getData()
        initProcessPlayers(updatedData)
        when (currentStatus) {
            IN_LOBBY -> processLobbyPlayers(updatedData)
            IN_MATCH -> processMatchPlayers(updatedData)
            READYING -> processReadyPlayers(updatedData)
            else -> { print("Xrd lobby not found.") }
        }
        postProcessPlayers(updatedData)
    }

    // Prepare playersData to be processed
    private fun initProcessPlayers(updatedData: Map<Int, Player>) {
        playersData.forEach { player -> if (updatedData.containsKey(player.key))
            player.value.presentInLobby = true
            else player.value.presentInLobby = false }
        updatedData.forEach { player -> if (!playersData.containsKey(player.key))
            playersData.put(player.key, player.value) }
    }

    // Check for Δ when on the LOBBY screen
    private fun processLobbyPlayers(updatedData: Map<Int, Player>) {
        updatedData.forEach { player -> if (loadingMatch(player))
            pendingStatus = READYING }
    }

    // Check for Δ when on the MATCH screen
    private fun processMatchPlayers(updatedData: Map<Int, Player>) {
        updatedData.forEach { player -> if (matchWasWon(player))
            pendingStatus = IN_LOBBY }
    }

    // Check for Δ when on the READYING screen
    private fun processReadyPlayers(updatedData: Map<Int, Player>) {
        var finishedReadying = true
        updatedData.forEach { player -> if (loadingMatch(player)) finishedReadying = false }
        if (finishedReadying) pendingStatus = IN_MATCH
    }

    // Update each players' score based on Δ in updatedData
    private fun postProcessPlayers(updatedData: Map<Int, Player>) {
        updatedData.forEach { player ->
            if (matchWasLost(player)) { getCurrentPlayer(player) /* and do stuff */ }
            else if (matchWasWon(player)) { getCurrentPlayer(player) /* and do stuff */ }
        }
    }

    // Utility functions for code readability
    private fun matchWasWon(player: Entry<Int, Player>) = player.value.wonMatch(getCurrentPlayer(player))
    private fun matchWasLost(player: Entry<Int, Player>) = player.value.lostMatch(getCurrentPlayer(player))
    private fun loadingMatch(player: Entry<Int, Player>) = player.value.isLoading()
    private fun getCurrentPlayer(player: Entry<Int, Player>) = playersData.get(player.key) ?: player.value

}

object LobbyStatus {
    const val NONE: Byte = 0x0
    const val IN_LOBBY: Byte = 0x1
    const val READYING: Byte = 0x2
    const val IN_MATCH: Byte = 0x3
}