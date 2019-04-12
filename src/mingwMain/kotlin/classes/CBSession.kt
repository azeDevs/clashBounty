package classes

import classes.Character.getCharacterName


class Session {
    private val xrdApi: XrdApi = XrdMemReader()
    private var playerSessions: MutableMap<Long, Player> = HashMap()
    private var updateCount: Long = 0

    fun getUpdateCounter() = updateCount++

    fun connected() = xrdApi.isXrdRunning() && xrdApi.connectToXrd()

    fun getLobby() = playerSessions.values.filter { e -> e.inLobby() }.toList().sortedBy { e -> e.getBounty() }
    fun getOne(index:Int) = playerSessions.values.toList().sortedBy { e -> e.getBounty() }.get(index)
    fun getAll() = playerSessions.values.toList()

    fun updatePlayerData() {
        xrdApi.getXrdData().forEach { data ->
            if (!playerSessions.containsKey(data.steamUserId)) playerSessions.put(data.steamUserId, Player(data))
            playerSessions.forEach { session ->
                session.value.inLobby(false)
                if (session.key == data.steamUserId) {
                    session.value.inLobby(true)
                    session.value.update(data)
                }
            }
        }
    }

}

class Player(playerData: PlayerData) {

    private var inLobby = false
    private var name = truncateName(playerData.displayName)
    private var character = getCharacterName(playerData.characterId)
    private val steamId = playerData.steamUserId
    private var bounty = 0
    private var chain = 0
    private var wins = playerData.matchesWon.toInt()
    private var matches = playerData.matchesPlayed.toInt()
    private var loadPct: Byte = 0x0

    private var lastUpdate: PlayerData? = null

    fun truncateName(name: String): String {
        if (name.length > 25) return name.substring(25)
        else return name
    }

    fun update(nextUpdate: PlayerData) {
        logInfo("player update ${nextUpdate.displayName}")
        if (lastUpdate == null) lastUpdate = nextUpdate
        name = nextUpdate.displayName
        character = getCharacterName(nextUpdate.characterId)
        loadPct = nextUpdate.loadingPct

        if (justWon(nextUpdate.matchesWon.toInt())) {
            chain++; wins++; matches++
        } else if (justPlayed(nextUpdate.matchesPlayed.toInt())) {
            if (chain < 2) chain = 0; else chain -= 2; matches++
        }

        lastUpdate = nextUpdate
    }

    fun getId(): Long {
        return steamId
    }

    fun getName(): String {
        return name
    }

    fun getCharacter(): String {
        return character
    }

    fun getChain(): Int {
        return chain
    }

    fun getBounty(): Int {
        return bounty
    }

    fun getMatchesWon(): Int {
        return wins
    }

    fun getMatchesPlayed(): Int {
        return matches
    }

    fun getLoadPercent(): Int {
        return loadPct.toInt()
    }

    private fun justWon(currWins: Int): Boolean {
        return currWins > lastUpdate!!.matchesWon
    }

    private fun justPlayed(currPlayed: Int): Boolean {
        return currPlayed > lastUpdate!!.matchesPlayed
    }

    fun inLobby(flag: Boolean) {
        inLobby = flag
    }

    fun inLobby(): Boolean {
        return inLobby
    }

    fun giveBounty(amount: Int) {
        bounty += amount
    }

    fun getRiskRating(): String {
        var grade = "?"
        if (getMatchesPlayed() > 4) {
            val gradeConversion = (getMatchesWon() + getChain()) / getMatchesPlayed()
            if (getMatchesPlayed() >= 16 && gradeConversion > 0.2) grade = "D"
            if (getMatchesPlayed() >= 8 && gradeConversion > 0.3) grade = "D+"
            if (gradeConversion > 0.4) grade = "C"
            if (gradeConversion > 0.5) grade = "C+"
            if (gradeConversion > 0.6) grade = "B"
            if (gradeConversion > 0.8) grade = "B+"
            if (gradeConversion > 1.0) grade = "A"
            if (gradeConversion > 1.5) grade = "A+"
            if (getMatchesPlayed() >= 8 && gradeConversion > 2.0) grade = "S"
            if (getMatchesPlayed() >= 16 && gradeConversion > 2.5) grade = "S+"
        }
        return grade
    }

}

