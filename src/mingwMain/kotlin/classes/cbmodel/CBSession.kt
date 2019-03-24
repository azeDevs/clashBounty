package classes.cbmodel

import classes.cbmodel.LobbyStatus.IN_LOBBY
import classes.cbmodel.LobbyStatus.IN_MATCH
import classes.cbmodel.LobbyStatus.NONE
import classes.cbmodel.LobbyStatus.READYING
import classes.output.logInfo
import kotlin.collections.Map.Entry



class ClashBountySession {
    val xrd: XrdDataManager = XrdDataManager()
    val playersData: MutableMap<Int, Player> = HashMap()
    var currentStatus: Byte = NONE
    var pendingStatus: Byte = NONE

    // TODO: Make this initiate the session's game loop
    fun start() {
        logInfo("Starting Clash Bounty Session...")
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

/**
 * Character ID constants
 * based on training mode quick selection order
 * to be used within [PlayerData] objects
 */
object Character {
    const val SO: Byte = 0x00
    const val KY: Byte = 0x01
    const val MA: Byte = 0x02
    const val MI: Byte = 0x03
    const val ZA: Byte = 0x04
    const val PO: Byte = 0x05
    const val CH: Byte = 0x06
    const val FA: Byte = 0x07
    const val AX: Byte = 0x08
    const val VE: Byte = 0x09
    const val SL: Byte = 0x0A
    const val IN: Byte = 0x0B
    const val BE: Byte = 0x0C
    const val RA: Byte = 0x0D
    const val SI: Byte = 0x0E
    const val EL: Byte = 0x0F
    const val LE: Byte = 0x10
    const val JO: Byte = 0x11
    const val JC: Byte = 0x12
    const val JM: Byte = 0x13
    const val KU: Byte = 0x14
    const val RV: Byte = 0x15
    const val DI: Byte = 0x16
    const val BA: Byte = 0x17
    const val AN: Byte = 0x18
}