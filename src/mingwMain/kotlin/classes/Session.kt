package classes

import kotlinx.cinterop.objcPtr

class Session {
    private val xrdApi: XrdApi = MemReader()
    private var playerSessions: MutableMap<Long, Player> = HashMap()
    private var playerLegacies: MutableMap<Long, Legacy> = HashMap()
    private var updateCount: Int = 0
    private var gamesCount: Int = 0

    fun getGamesCount() = gamesCount

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
                        "bounty: ${legacy.bountyWon} / ${legacy.bountyTotal}\n" +
                        "matches: ${legacy.matchesWon} / ${legacy.matchesTotal}")
            }
        }

        var loserChange = 0
        // reset all data to be updated
        xrdApi.getXrdData().forEach { data ->
            // Add new player if they didn't previously exist
            if (!playerSessions.containsKey(data.steamUserId)) {
                playerSessions.put(data.steamUserId, Player(data))
                if (playerLegacies.containsKey(data.steamUserId)) {
                    playerLegacies.getValue(data.steamUserId).matchesWon += data.matchesWon
                    playerLegacies.getValue(data.steamUserId).matchesTotal += data.matchesTotal
                    playerSessions.getValue(data.steamUserId).changeBounty(playerLegacies.getValue(data.steamUserId).bountyWon.toInt())
                }
                else {
                    playerLegacies.put(data.steamUserId, Legacy("ID${data.steamUserId}Bw${0}Bt${0}Mw${data.matchesWon}Mt${data.matchesTotal}"))
                    logInfo("Added ID ${playerLegacies.get(data.steamUserId)!!.steamId}\n" +
                            "bounty: ${playerLegacies.get(data.steamUserId)!!.bountyWon} / ${playerLegacies.get(data.steamUserId)!!.bountyTotal}\n" +
                            "matches: ${playerLegacies.get(data.steamUserId)!!.matchesWon} / ${playerLegacies.get(data.steamUserId)!!.matchesTotal}")
                }
            }

            // Find the loser to solve for changes to bounties
            playerSessions.values.forEach { s ->
                if (s.getSteamId() == data.steamUserId) {
                    s.swapDataWithLatest(data)
                    if (s.justLost() && s.getBounty() > 0) loserChange = s.getBounty().div(2)
                }
            }

            // Resolve changes to the winner and loser
            playerSessions.values.forEach { s ->
                if (s.getSteamId() == data.steamUserId) {
                    if (s.justWon()) {
                        gamesCount++
                        s.changeChain(1)
                        s.changeBounty((s.getChain() * s.getChain() * 100))
                        s.changeBounty(loserChange)
                        playerLegacies.getValue(s.getSteamId()).bountyTotal += loserChange + (s.getChain() * s.getChain() * 100)
                        logInfo("WON ${playerLegacies.getValue(s.getSteamId()).steamId}\n" +
                                "Change: ${loserChange} + ${(s.getChain() * s.getChain() * 100)}" +
                                "Result: ${loserChange + (s.getChain() * s.getChain() * 100)} W$")
                    } else if (s.justLost()) {
                        s.changeChain(-1)
                        s.changeBounty((s.getChain() * s.getChain() * 10))
                        s.changeBounty(-loserChange)
                        playerLegacies.getValue(s.getSteamId()).bountyWon = s.getBounty().toLong()
                        if (loserChange > 0) playerLegacies.getValue(s.getSteamId()).bountyTotal += loserChange
                        logInfo("LOST ${playerLegacies.getValue(s.getSteamId()).steamId}\n" +
                                "Change: ${-loserChange} + ${(s.getChain() * s.getChain() * 10)}" +
                                "Result: ${-loserChange + (s.getChain() * s.getChain() * 10)} W$")
                    }
                }
            }
        }

        // Check if "change of turn" occured and update everything

        val redrawList = playerSessions.values.filter { s -> s.shouldRedraw() }.toList()
        if (redrawList.size > 0) {
            playerSessions.values.forEach { s ->
                if (!s.justPlayed()) {
                    s.changeBounty(0)
                    if (s.isIdle() && s.getChain() > 0) {
                        s.changeChain(-1)
                        logInfo("IDLED ${playerLegacies.getValue(s.getSteamId()).steamId}\n" +
                                "Chain: ${s.getChain()+1} - 1 = ${s.getChain()}")
                    }
                }
            }
            var legacyStr = ""
            playerLegacies.values.forEach { l -> legacyStr += l.getDataStr() }
            writeToFile("legacy", legacyStr)
            logInfo("Legacies Saved")
        }

        return redrawList.size > 0
    }
}

