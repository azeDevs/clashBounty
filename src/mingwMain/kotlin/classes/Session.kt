package classes

class Session {
    private val xrdApi: XrdApi = MemReader()
    private var playerSessions: MutableMap<Long, Player> = HashMap()
    private var playerLegacies: MutableMap<Long, Player.Legacy> = HashMap()
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
        //update player legacies
        if (playerLegacies.isEmpty()) {
            logInfo(readFromFile("test"))
            logInfo("legacy")
            playerLegacies.put(1234L, Player.Legacy(1234L))
        }

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
                    }
                }
            }

            playerSessions.values.forEach { s ->
                if (s.getSteamId() == data.steamUserId) {
                    if (s.justWon()) {
                        s.idle = 0
                        s.changeChain(1)
                        winnerChange += (s.getChain()*s.getChain()) * (s.getData().matchesTotal + s.getData().matchesWon) * 100
                        s.changeBounty(winnerChange)
                        gamesCount++
                    } else if (s.justLost()) {
                        s.idle = 0
                        s.changeChain(-2)
                        loserChange += (s.getChain()*s.getChain()) * (s.getData().matchesTotal + s.getData().matchesWon) * 10
                        s.changeBounty(loserChange)
                    }
                }
            }
        }

        // Should Redraw / Update after every game
        val redrawList = playerSessions.values.filter { s -> s.shouldRedraw() }.toList()

        if (redrawList.size > 0) playerSessions.values.forEach { s ->
            if (!s.justPlayed()) {
                s.changeBounty(s.getChain() * (s.getData().matchesTotal + s.getData().matchesWon) * 10)
                s.idle++
            }
//            var legacyCurr = Player.Legacy(s.getSteamId())
        }
        return redrawList.size > 0
    }
}

