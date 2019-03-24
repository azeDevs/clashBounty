package classes.session

import classes.getCBImpl
import classes.output.logInfo
import classes.output.logWarn

class XrdDataManager {
    val xrdApi: ClashBountyApi = getCBImpl()

    fun connect() {
        if (!xrdApi.isXrdRunning()) logWarn("Xrd process is not running")
        else if (!xrdApi.isXrdConnected()) xrdApi.connectToXrd()
        else logInfo("Xrd process is already connected")
    }

    fun getData(): Map<Int, Player> {
        var updatedData: MutableMap<Int, Player> = HashMap()
        if (xrdApi.isXrdConnected()) xrdApi.getXrdData().forEach {
                data -> updatedData.put(data.steamUserId, Player(data))
        } else logWarn("Xrd process is not yet connected")
        return updatedData
    }

}

class Player(val data: PlayerData) {
    // Clash Bounty game stats
    var presentInLobby: Boolean = true
    var bountyScore: Int = 0
    var riskRating: Int = 0

    // Utility functions for detecting Δ game state
    fun isLoading() = data.loadingPct > 0x0 && data.loadingPct < 0x64
    fun lostMatch(player: Player) = matchesIncremented(player) && winsIncremented(player)
    fun wonMatch(player: Player) = winsIncremented(player)

    // Utility functions for code readability
    private fun winsIncremented(player: Player) = data.matchesWon == player.data.matchesWon
    private fun matchesIncremented(player: Player) = data.matchesPlayed > player.data.matchesPlayed

}