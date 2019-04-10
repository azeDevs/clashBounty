package classes.session

import classes.currPlayers

class Player(val data: PlayerData) {
    // Clash Bounty game stats
    var presentInLobby: Boolean = true

    // Utility functions for detecting Δ game state
    fun isLoading() = data.loadingPct > 0x0 && data.loadingPct < 0x64
    fun lostMatch() = currPlayers.get(data.steamUserId) != null &&
            data.matchesPlayed > currPlayers.get(data.steamUserId)!!.data.matchesPlayed &&
            data.matchesWon == currPlayers.get(data.steamUserId)!!.data.matchesWon
    fun wonMatch() = (currPlayers.get(data.steamUserId) != null &&
            data.matchesWon == currPlayers.get(data.steamUserId)!!.data.matchesWon)

//    private var bountyScore: Int = 0
//    private var chainBonus: Int = 0
//
//    // String getters for file writing
//    fun getDisplayNameString():String {
//        if (data.displayName.length > 25) return "${data.displayName.substring(22)}..."
//        else return data.displayName
//    }
//
//    fun getPlacing(currPlayers:Map<Long, Player>):Int {
//        logFunc("getPlacing")
//        return currPlayers.filter { player ->
//            player.key != data.steamUserId &&
//            bountyScore < player.value.bountyScore
//        }.entries.size + 1
//    }
//
//    fun getPlacingString():String {
//        var placingStr = getPlacing().toString()
//        when(placingStr.subSequence(placingStr.length-1,placingStr.length)) {
//            "1" ->  return "${placingStr}st"
//            "2" ->  return "${placingStr}nd"
//            "3" ->  return "${placingStr}rd"
//            else -> return "${placingStr}th"
//        }
//        return placingStr
//    }
//
//    fun getBountyScoreString():String { return "${bountyScore} W$" }
//
//    fun getRiskRatingString():String {
//        val gradeConversion = (data.matchesWon + chainBonus) / (data.matchesPlayed)
//        var grade = "?"
//        if ((data.matchesPlayed) > 15 && gradeConversion > 0.2) grade = "D"
//        if ((data.matchesPlayed) > 10 && gradeConversion > 0.3) grade = "D+"
//        if ((data.matchesPlayed) > 5  && gradeConversion > 0.4) grade = "C"
//        if ((data.matchesPlayed) > 5  && gradeConversion > 0.5) grade = "C+"
//        if ((data.matchesPlayed) > 5  && gradeConversion > 0.6) grade = "B"
//        if ((data.matchesPlayed) > 5  && gradeConversion > 0.8) grade = "B+"
//        if ((data.matchesPlayed) > 5  && gradeConversion > 1.0) grade = "A"
//        if ((data.matchesPlayed) > 5  && gradeConversion > 1.5) grade = "A+"
//        if ((data.matchesPlayed) > 10 && gradeConversion > 2.0) grade = "S"
//        if ((data.matchesPlayed) > 15 && gradeConversion > 2.5) grade = "S+"
//        return grade
//    }

}

object Character {
    const val SO: Byte = 0x00
    const val KY: Byte = 0x01
    const val MA: Byte = 0x02
    const val MI: Byte = 0x03
    const val ZA: Byte = 0x04
    const val PO: Byte = 0x05
    const val CH: Byte = 0x06
    const val FA: Byte = 0x07
    const val AX: Byte = 0x08
    const val VE: Byte = 0x09
    const val SL: Byte = 0x0A
    const val IN: Byte = 0x0B
    const val BE: Byte = 0x0C
    const val RA: Byte = 0x0D
    const val SI: Byte = 0x0E
    const val EL: Byte = 0x0F
    const val LE: Byte = 0x10
    const val JO: Byte = 0x11
    const val JC: Byte = 0x12
    const val JM: Byte = 0x13
    const val KU: Byte = 0x14
    const val RV: Byte = 0x15
    const val DI: Byte = 0x16
    const val BA: Byte = 0x17
    const val AN: Byte = 0x18

    fun getCharacterName(byte: Byte): String {
        when (byte) {
            SO -> return "Sol Badguy"
            KY -> return "Ky Kiske"
            MA -> return "May"
            MI -> return "Millia Rage"
            ZA -> return "Zato=1"
            PO -> return "Potemkin"
            CH -> return "Chipp Zanuff"
            FA -> return "Faust"
            AX -> return "Axl Low"
            VE -> return "Venom"
            SL -> return "Slayer"
            IN -> return "I-No"
            BE -> return "Bedman"
            RA -> return "Ramlethal Valentine"
            SI -> return "Sin Kiske"
            EL -> return "Elpelt Valentine"
            LE -> return "Leo Whitefang"
            JO -> return "Johnny"
            JC -> return "Jack-O"
            JM -> return "Jam Kurodoberi"
            KU -> return "Kum Haehyun"
            RV -> return "Raven"
            DI -> return "Dizzy"
            BA -> return "Baiken"
            AN -> return "Answer"
            else -> return "???"
        }
    }
}