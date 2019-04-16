package classes


class Session {
    private val xrdApi: XrdApi = XrdMemReader()
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

    fun updatePlayerData() {
        xrdApi.getXrdData().forEach { data ->
            if (data.steamUserId == 0L) playerSessions.values.forEach { session ->
                if (session.getName().equals(data.displayName)) session.inLobby = false
            }
            if (!playerSessions.containsKey(data.steamUserId)) playerSessions.put(data.steamUserId, Player(data))
            playerSessions.forEach { session ->
                if (session.key == data.steamUserId) session.value.update(data)
            }
        }
    }

}

class Player(playerData: PlayerData) {

    private var bounty = 0
    private var chain = 0
    private var data = Pair(playerData, playerData)
    var inLobby =  true

    private fun oldData() = data.first
    private fun newData() = data.second

    fun update(updatedData: PlayerData):Boolean {
        data = Pair(newData(), updatedData)
        inLobby = true
        if (!oldData().equals(newData())) {
            if (justWon()) {
                bounty += 100 * (++chain+1) * (newData().matchesTotal + newData().matchesWon)
            } else if (justPlayed()) {
                if (chain < 2) chain = 0; else chain -= 2
                if (bounty > 10) bounty -= bounty / 2
                bounty += 10 * (chain+1) * (newData().matchesTotal + newData().matchesWon)
            }
            return true
        } else return false
    }

    fun getName(): String {
        return newData().displayName
    }

    fun getId(): Long {
        return newData().steamUserId
    }

    fun getCharacter(): String {
        return newData().characterName
    }

    fun getChain(): Int {
        return chain
    }

    fun getBounty(): Int {
        return bounty
    }

    fun getMatchesWon(): Int {
        return newData().matchesWon
    }

    fun getMatchesPlayed(): Int {
        return newData().matchesTotal
    }

    fun getLoadPercent(): Int {
        return newData().loadingPct
    }

    private fun justWon(): Boolean {
        return newData().matchesWon > oldData().matchesWon
    }

    private fun justPlayed(): Boolean {
        return newData().matchesTotal > oldData().matchesTotal
    }

    fun getRiskRating(): String {
        var grade = "?"
        if (getMatchesWon() > 0) {
            grade = "D"
            val gradeConversion:Float = (getMatchesWon() + getChain()).toFloat() / (getMatchesPlayed() - getChain()).toFloat()
            if (gradeConversion >= 0.1f) grade = "D+"
            if (gradeConversion >= 0.2f) grade = "C"
            if (gradeConversion >= 0.3f) grade = "C+"
            if (gradeConversion >= 0.4f) grade = "B"
            if (gradeConversion >= 0.6f) grade = "B+"
            if (gradeConversion >= 1.0f) grade = "A"
            if (gradeConversion >= 1.5f) grade = "A+"
            if (gradeConversion >= 2.0f) grade = "S"
            if (gradeConversion >= 3.0f) grade = "S+"
        }
        return grade
    }

}

