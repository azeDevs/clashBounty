package classes.session

import classes.getCBImpl
import classes.output.logInfo
import classes.output.logWarn

class XrdDataManager {
    val xrdApi: ClashBountyApi = getCBImpl()

    fun connect() {
        if (!xrdApi.isXrdRunning()) logWarn("Xrd process is not running")
        else if (!xrdApi.isXrdConnected()) xrdApi.connectToXrd()
        else logInfo("Xrd process is already connected")
    }

    fun getData(): Map<Long, Player> {
        var updatedData: MutableMap<Long, Player> = HashMap()
        if (xrdApi.isXrdConnected()) xrdApi.getXrdData().forEach {
                data -> updatedData.put(data.steamUserId, Player(data))
        } else logWarn("Xrd process is not yet connected")
        return updatedData
    }

}

class Player(val data: PlayerData) {
    // Clash Bounty game stats
    var presentInLobby: Boolean = true
    private var bountyScore: Int = 0
    private var chainBonus: Int = 0

    // String getters for file writing
    fun getDisplayNameString():String {
        if (data.displayName.length > 25) return "${data.displayName.substring(22)}..."
        else return data.displayName
    }

    fun getPlacing():Int {
        return playersData.filter { player ->
            player.key != data.steamUserId &&
            bountyScore < player.value.bountyScore
        }.entries.size + 1
    }

    fun getPlacingString():String {
        var placingStr:String = getPlacing().toString()
        when(placingStr.subSequence(placingStr.length-1,placingStr.length)) {
            "1" ->  return "${placingStr}st"
            "2" ->  return "${placingStr}nd"
            "3" ->  return "${placingStr}rd"
            else -> return "${placingStr}th"
        }
        return placingStr
    }

    fun getBountyScoreString():String { return "${bountyScore} W$" }

    fun getRiskRatingString():String {
        val gradeConversion = (data.matchesWon + chainBonus) / (data.matchesPlayed)
        var grade = "?"
        if ((data.matchesPlayed) > 15 && gradeConversion > 0.2) grade = "D"
        if ((data.matchesPlayed) > 10 && gradeConversion > 0.3) grade = "D+"
        if ((data.matchesPlayed) > 5  && gradeConversion > 0.4) grade = "C"
        if ((data.matchesPlayed) > 5  && gradeConversion > 0.5) grade = "C+"
        if ((data.matchesPlayed) > 5  && gradeConversion > 0.6) grade = "B"
        if ((data.matchesPlayed) > 5  && gradeConversion > 0.8) grade = "B+"
        if ((data.matchesPlayed) > 5  && gradeConversion > 1.0) grade = "A"
        if ((data.matchesPlayed) > 5  && gradeConversion > 1.5) grade = "A+"
        if ((data.matchesPlayed) > 10 && gradeConversion > 2.0) grade = "S"
        if ((data.matchesPlayed) > 15 && gradeConversion > 2.5) grade = "S+"
        return grade
    }

    // Utility functions for detecting Δ game state
    fun isLoading() = data.loadingPct > 0x0 && data.loadingPct < 0x64
    fun lostMatch(player: Player) = matchesIncremented(player) && winsIncremented(player)
    fun wonMatch(player: Player) = winsIncremented(player)

    // Utility functions for code readability
    private fun winsIncremented(player: Player) = data.matchesWon == player.data.matchesWon
    private fun matchesIncremented(player: Player) = data.matchesPlayed > player.data.matchesPlayed

}