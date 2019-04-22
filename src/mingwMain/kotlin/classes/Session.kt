package classes

class Session {
    private val xrdApi: XrdApi = MemReader()
    private var playerSessions: MutableMap<Long, Player> = HashMap()
    private var updateCount: Int = 0
    private var gamesCount: Int = 0

    fun getUpdateCounter(): String {
        when (updateCount++) {
            0 -> return ""
            1 -> return "."
            2 -> return ".."
            else -> { updateCount = 0; return "..." }
        }
    }

    fun connected() = xrdApi.isXrdRunning() && xrdApi.connectToXrd()

    fun getAll() = playerSessions.values.toList()
        .sortedByDescending { item -> item.getRating() }
        .sortedByDescending { item -> item.getBounty() }

    fun updatePlayerData():Boolean {
        var loserChange = 0
        var winnerChange = 0
        // reset all data to be updated
        xrdApi.getXrdData().forEach { data ->
            // Add new player if they didn't previously exist
            if (!playerSessions.containsKey(data.steamUserId)) {
                playerSessions.put(data.steamUserId, Player(data))
            }
            // Find the loser to solve for changes to bounties
            playerSessions.values.forEach { s ->
                if (s.getSteamId() == data.steamUserId) {
                    s.swapDataWithLatest(data)
                    if (s.justLost() && s.getChain() > 0) {
                        loserChange = -(s.getBounty().div(2))
                        winnerChange = (s.getBounty().div(2))
                    } else if (s.justLost() && s.getChain() <= 0) {
                        loserChange = -s.getBounty()
                        winnerChange = s.getBounty()
                    }
                }
            }

            playerSessions.values.forEach { s ->
                if (s.getSteamId() == data.steamUserId) {
                    if (s.justWon()) {
                        s.changeChain(1)
                        winnerChange += (s.getChain()*s.getChain()) * (s.getData().matchesTotal + s.getData().matchesWon) * 100
                        s.changeBounty(winnerChange)
                        gamesCount++
                    } else if (s.justLost()) {
                        s.changeChain(-2)
                        loserChange += (s.getChain()*s.getChain()) * (s.getData().matchesTotal + s.getData().matchesWon) * 10
                        s.changeBounty(loserChange)
                    }
                }
            }
        }
        val someRedraw = playerSessions.values.filter { s -> s.shouldRedraw() }.toList().size > 0
        if (someRedraw) playerSessions.values.forEach { s -> if (!s.justPlayed()) s.changeBounty(0) }
        return someRedraw
    }
}

