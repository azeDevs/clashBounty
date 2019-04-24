package classes

import kotlinx.cinterop.objcPtr

class Session {
    private val xrdApi: XrdApi = MemReader()
    private var playerSessions: MutableMap<Long, Player> = HashMap()
    private var playerLegacies: MutableMap<Long, Legacy> = HashMap()
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
            readFromFile("legacy").split("!").forEach { dataStr ->
                val legacy = Legacy(dataStr)
                playerLegacies.put(legacy.steamId, legacy)
            }
            playerLegacies.values.forEach { legacy ->
                logInfo("Found ID ${legacy.steamId}\n" +
                        "bounty won: ${legacy.bountyWon}\n" +
                        "bounty total: ${legacy.bountyTotal}\n" +
                        "matches won: ${legacy.matchesWon}\n" +
                        "matches total: ${legacy.matchesTotal}")
            }
        }

        var loserChange = 0
        var winnerChange = 0
        // reset all data to be updated
        xrdApi.getXrdData().forEach { data ->
            // Add new player if they didn't previously exist
            if (!playerSessions.containsKey(data.steamUserId)) {
                playerSessions.put(data.steamUserId, Player(data))
                if (playerLegacies.containsKey(data.steamUserId)) {
                    playerLegacies.getValue(data.steamUserId).matchesWon += data.matchesWon
                    playerLegacies.getValue(data.steamUserId).matchesWon += data.matchesTotal
                }
                else {
                    playerLegacies.put(data.steamUserId, Legacy("ID${data.steamUserId}Bw${0}Bt${0}Mw${data.matchesWon}Mt${data.matchesTotal}"))
                    logInfo("Found ID ${playerLegacies.get(data.steamUserId)!!.steamId}\n" +
                            "bounty won: ${playerLegacies.get(data.steamUserId)!!.bountyWon}\n" +
                            "bounty total: ${playerLegacies.get(data.steamUserId)!!.bountyTotal}\n" +
                            "matches won: ${playerLegacies.get(data.steamUserId)!!.matchesWon}\n" +
                            "matches total: ${playerLegacies.get(data.steamUserId)!!.matchesTotal}")
                }
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

            // Resolve changes to the winner and loser
            playerSessions.values.forEach { s ->
                if (s.getSteamId() == data.steamUserId) {
                    if (s.justWon()) {
                        gamesCount++
                        s.changeChain(1)
                        winnerChange += (s.getChain()*s.getChain()) * (s.getData().matchesTotal + s.getData().matchesWon) * 100
                        s.changeBounty(winnerChange)

                    } else if (s.justLost()) {
                        s.changeChain(-1)
                        loserChange += (s.getChain()*s.getChain()) * (s.getData().matchesTotal + s.getData().matchesWon) * 10
                        s.changeBounty(loserChange)
                    }
                }
            }
        }

        // Check if "change of turn" occured and update everything
        var legacyStr = ""
        val redrawList = playerSessions.values.filter { s -> s.shouldRedraw() }.toList()
        if (redrawList.size > 0) {
            playerSessions.values.forEach { s ->
                if (!s.justPlayed()) {
                    s.changeBounty(s.getChain() * (s.getData().matchesTotal + s.getData().matchesWon) * 10)
                    if (s.isIdle() && s.getChain() != 0) s.changeChain(-1)
                }
            }
//            playerLegacies.values.forEach { l -> legacyStr += l.getDataStr() }
        }
//        writeToFile("legacy", legacyStr)
        return redrawList.size > 0
    }
}

