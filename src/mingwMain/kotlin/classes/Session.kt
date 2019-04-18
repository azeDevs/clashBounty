package classes


class Session {
    private val xrdApi: XrdApi = MemReader()
    private var playerSessions: MutableMap<Long, Player> = HashMap()
    private var updateCount: Int = 0

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
        var refresh = false
        var winner: Player? = null
        var loser: Player? = null
        xrdApi.getXrdData().forEach { data ->
            if (data.steamUserId == 0L) playerSessions.values.forEach { session -> if (data.equals(session.getData())) session.inLobby = false }

            if (!playerSessions.containsKey(data.steamUserId)) playerSessions.put(data.steamUserId, Player(data))
            playerSessions.values.forEach { session ->
                if (session.getSteamId() == data.steamUserId && !data.equals(session.getData())) {
                    refresh = true
                    if (session.justWon()) winner = session
                    else if (session.justPlayed()) loser = session
                    session.update(data)
                }
            }
        }

        if (winner != null && loser != null) {
            if (loser!!.getBounty() > 10) {
                winner?.changeBounty(loser?.getBounty()?.div(4)!!)
                loser?.changeBounty(-(loser?.getBounty()?.div(2)!!))
            }
        }
        winner = null; loser = null
        return refresh
    }
}

