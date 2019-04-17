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

    fun getAll() = playerSessions.values.toList()
        .sortedByDescending { item -> item.getRating() }
        .sortedByDescending { item -> item.getBounty() }

    fun updatePlayerData() {
        var winner: Player? = null
        var loser: Player? = null
        xrdApi.getXrdData().forEach { data ->
            if (data.steamUserId == 0L) playerSessions.values.forEach { session -> if (data.equals(session.getData())) session.inLobby = false }
            if (!playerSessions.containsKey(data.steamUserId)) playerSessions.put(data.steamUserId, Player(data))
            playerSessions.values.forEach { session ->
                if (session.getSteamId() == data.steamUserId && !data.equals(session.getData())) {
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

    fun getDisplayName() = newData().displayName

    fun getNameString() = if (inLobby) "${getDisplayName()}  -  [ID1${getSteamId()}]" else "${getDisplayName()}  [ID0${getSteamId()}]"

    fun getSteamId() = newData().steamUserId

    fun getCharacter() = newData().characterName

    fun getBounty() = bounty

    fun getBountyFormatted():String {
        var inStr = getBounty().toString()
        var commas = if (inStr.length % 3 == 0) (inStr.length/3)-1 else inStr.length/3
        var outStr = " W$"
        for (i in 0..commas-1) outStr = if (inStr.length > 3) ",${inStr.substring(inStr.length-(3*(i+1)), inStr.length-(3*i))}${outStr}" else "${inStr.substring(inStr.length-(3*(i+1)), inStr.length-(3*i))}${outStr}"
        return inStr.substring(0, inStr.length-(3*commas)) + outStr
    }

    fun getBountyString() = if (getBounty() > 0) "Bounty: ${getBountyFormatted()}" else "Free"

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

    fun getRating() = (getMatchesWon() + getChain()).toFloat() / (getMatchesPlayed() - getChain()).toFloat()


    fun getRatingLetter(): String {
        var grade = "?"
        if (getMatchesWon() > 0) {
            grade = "D"
            val gradeConversion = getRating()
            if (gradeConversion >= 0.1f) grade = "D+"
            if (gradeConversion >= 0.2f) grade = "C"
            if (gradeConversion >= 0.3f) grade = "C+"
            if (gradeConversion >= 0.4f) grade = "B"
            if (getMatchesWon() >= 2 && gradeConversion >= 0.6f) grade = "B+"
            if (getMatchesWon() >= 4 && gradeConversion >= 1.0f) grade = "A"
            if (getMatchesWon() >= 8 && gradeConversion >= 1.5f) grade = "A+"
            if (getMatchesWon() >= 16 && gradeConversion >= 2.0f) grade = "S"
            if (getMatchesWon() >= 32 && gradeConversion >= 3.0f) grade = "S+"
        }
        return grade
    }

    fun getRatingString() = "Risk Rating: ${getRatingLetter()}"

}

