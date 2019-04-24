package classes

import classes.Character.getCharacterName
import classes.libui.data
import kotlin.math.abs

class Player(playerData: PlayerData) {

    private var bounty = 0
    private var change = 0
    private var chain = 0
    private var idle = 0
    private var data = Pair(playerData,playerData)

    private fun oldData() = data.first
    fun getData() = data.second

    fun swapDataWithLatest(updatedData: PlayerData) { data = Pair(getData(), updatedData) }

    fun shouldRedraw() = !oldData().equals(getData())

    fun getDisplayName() = getData().displayName

    fun getNameString() = "${getDisplayName()}  -  [ Steam ID: ${getSteamId()} ]"

    fun getSteamId() = getData().steamUserId

    fun getCharacter(shortened:Boolean) = getCharacterName(getData().characterId, shortened)

    fun isScoreboardWorthy() = getBounty() > 0 && idle > 0 && getMatchesWon() > 0

    fun getBounty() = bounty

    fun getBountyFormatted() = "${addCommas(getBounty().toString())} W$"

    fun getBountyString() = if (getBounty() > 0) "Bounty: ${getBountyFormatted()} (${change})" else "Free"

    fun getChain() = chain

    fun isIdle() = idle == 0 || --idle == 0

    fun changeChain(amount:Int) {
        idle = MAX_IDLE
        chain += amount
        if (chain < 0) chain = 0
        if (chain > 8) chain = 8
    }

    fun getChangeString(): String {
        if (change > 0) return "+${addCommas(change.toString())} W$\n "
        else if (change < 0) return " \n-${addCommas(abs(change).toString())} W$"
        else return " \n "
    }

    fun getMatchesWon() = getData().matchesWon

    fun getMatchesPlayed() = getData().matchesTotal

    fun getRecordString() = "Chain: ${getChain()}  [ W:${getMatchesWon()} / M:${getMatchesPlayed()} ]"

    fun getLoadPercent() = getData().loadingPct

    val MAX_IDLE = 8
    fun getStatusString() = if (idle == 0) "Idle: ${idle}   [${getLoadPercent()}%]" else "Standby: ${idle}  [${getLoadPercent()}%]"


    fun justPlayed() = getData().matchesTotal > oldData().matchesTotal

    fun justLost() = getData().matchesWon == oldData().matchesWon && justPlayed()

    fun justWon() = getData().matchesWon > oldData().matchesWon && justPlayed()

    fun changeBounty(amount:Int) {
        change = amount
        bounty += amount
        if (bounty < 0) bounty = 0
    }

    fun getRating() = (getMatchesWon() + getChain()).toFloat() / (getMatchesPlayed() - getChain()).toFloat()


    fun getRatingLetter(): String {
        var grade = "-"
        if (getMatchesWon() > 0) {
            grade = "D"
            val gradeConversion = getRating()
            if (gradeConversion >= 0.1f) grade = "D+"
            if (gradeConversion >= 0.2f) grade = "C"
            if (gradeConversion >= 0.3f) grade = "C+"
            if (gradeConversion >= 0.4f) grade = "B"
            if (getMatchesWon() >= 4 && gradeConversion >= 0.6f) grade = "B+"
            if (getMatchesWon() >= 8 && gradeConversion >= 1.0f) grade = "A"
            if (getMatchesWon() >= 16 && gradeConversion >= 1.5f) grade = "A+"
            if (getMatchesWon() >= 32 && gradeConversion >= 2.0f) grade = "S"
            if (getMatchesWon() >= 64 && gradeConversion >= 3.0f) grade = "S+"
        }
        return grade
    }

    fun getRatingString() = "Risk Rating: ${getRatingLetter()}"

}

