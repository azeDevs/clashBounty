package classes

class XrdDataManager {
    val xrd: XrdMemoryExtractorStub = XrdMemoryExtractorStub()

    // TODO: Make this loop frequently until finished or timed out
    fun connect() {
        if (!xrd.isXrdRunning()) println("Xrd process is not running")
        else if (!xrd.isXrdConnected()) xrd.connectToXrd(Host.STEAM_ID, Host.DISPLAY_NAME)
        else println("Xrd process is already connected")
    }

    fun getData(): Map<Int, Player> {
        var updatedData: MutableMap<Int, Player> = HashMap()
        if (xrd.isXrdConnected()) xrd.getXrdData().forEach {
                data -> updatedData.put(data.steamUserId, Player(data))
        } else println("Xrd process is not yet connected")
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