package classes

import kotlin.math.roundToInt


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

    fun getAll() = playerSessions.values.toList().sortedByDescending { item -> item.getBounty() }

    fun updatePlayerData() {
        var winner: Player? = null
        var loser: Player? = null
        xrdApi.getXrdData().forEach { data ->
            if (data.steamUserId == 0L) playerSessions.values.forEach { session -> if (data.equals(session.getData())) session.inLobby = false }
            if (!playerSessions.containsKey(data.steamUserId)) playerSessions.put(data.steamUserId, Player(data))
            playerSessions.values.forEach { session ->
                if (session.getId() == data.steamUserId && !data.equals(session.getData())) {
                    if (session.justWon()) winner = session
                    else if (session.justPlayed()) loser = session

                    if (winner != null && loser != null) {
                        if (loser!!.getBounty() > 10) {
                            winner?.changeBounty(loser?.getBounty()?.times(0.25f)!!.roundToInt())
                            loser?.changeBounty(-loser?.getBounty()?.times(0.5f)!!.roundToInt())
                        }
                    }

                    session.update(data)
                }
            }
        }
        winner = null; loser = null
    }

}

class Player(playerData: PlayerData) {

    private var bounty = 0
    private var chain = 0
    private var data = Pair(playerData, playerData)
    var inLobby =  true

    private fun oldData() = data.first
    private fun newData() = data.second
    fun getData() = newData()

    fun update(updatedData: PlayerData):Boolean {
        data = Pair(newData(), updatedData)
        inLobby = true
        if (!oldData().equals(newData())) {
            if (justWon()) {
                bounty += 100 * (++chain+1) * (newData().matchesTotal + newData().matchesWon)
            } else if (justPlayed()) {
                if (chain < 2) chain = 0; else chain -= 2
                bounty += 10 * (chain+1) * (newData().matchesTotal + newData().matchesWon)
            }
            return true
        } else return false
    }

    fun getName() = newData().displayName

    fun getNameString() = if (inLobby) "${getName()}  -  [ID1${getId()}]" else "${getName()}  [ID0${getId()}]"

    fun getId() = newData().steamUserId

    fun getCharacter() = newData().characterName

    fun getBounty() = bounty

    fun getBountyString() = if (getBounty() > 0) "Bounty: ${getBounty()} W$" else "Civilian"

    fun getChain() = chain

    fun getMatchesWon() = newData().matchesWon

    fun getMatchesPlayed() = newData().matchesTotal

    fun getRecordString() = "C:${getChain()}  /  W:${getMatchesWon()}  /  M:${getMatchesPlayed()}"

    fun getLoadPercent() = newData().loadingPct

    fun getStatusString(): String {
        if (getLoadPercent() == 0) return "Standby: ${getLoadPercent()}%"
        else if (getLoadPercent() == 100) return "Standby: ${getLoadPercent()}%"
        else return "Loading: ${getLoadPercent()}%"
    }

    fun justWon() = newData().matchesWon > oldData().matchesWon

    fun justPlayed() = newData().matchesTotal > oldData().matchesTotal

    fun changeBounty(amount:Int) {
        bounty += amount
        if (bounty<0) bounty = 0
    }

    fun getRatingString(): String {
        var grade = "Harmless"
        if (getMatchesWon() > 0) {
            grade = "D"
            val gradeConversion = (getMatchesWon() + getChain()).toFloat() / (getMatchesPlayed() - getChain()).toFloat()
            if (gradeConversion >= 0.1f) grade = "D+"
            if (gradeConversion >= 0.2f) grade = "C"
            if (gradeConversion >= 0.3f) grade = "C+"
            if (gradeConversion >= 0.4f) grade = "B"
            if (gradeConversion >= 0.6f) grade = "B+"
            if (getMatchesWon() >= 4 && gradeConversion >= 1.0f) grade = "A"
            if (getMatchesWon() >= 8 && gradeConversion >= 1.5f) grade = "A+"
            if (getMatchesWon() >= 12 && gradeConversion >= 2.0f) grade = "S"
            if (getMatchesWon() >= 16 && gradeConversion >= 3.0f) grade = "S+"
            return "Risk Rating: ${grade}"
        } else return grade

    }

}

