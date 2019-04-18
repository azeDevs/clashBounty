package classes.session

import classes.Player
import classes.writeToFile

fun writeLobbyFiles(players: List<Player>) {
    for (i in 0..3) if (players.size > i && players.get(i).getBounty() > 0) {
        val remote = "${getPlaceString(i+1)}\n" +
                "Backdrop:\n" +
                "${getFullscreen(true)}\n" +
                "Name:\n" +
                "${players.get(i).getDisplayName()}\n" +
                "Bounty:\n" +
                "${players.get(i).getBountyFormatted()}\n" +
                "Rating:\n" +
                "${players.get(i).getRatingLetter()}\n" +
                "Chain:\n" +
                "${if (players.get(i).getChain()>0) players.get(i).getChain() else " "}\n"
        writeToFile("remote${i}", remote)
    } else writeToFile("remote${i}", "")
}

private fun getFullscreen(enabled: Boolean): String {
    if (enabled) return "████████████████████████████████████████████████████████████████████████████████████████████████"
    else return "                                                                                                "
}

private fun getPlaceString(i: Int):String {
    when(i.toString().substring(i.toString().length-1,i.toString().length)) {
        "1" -> return "1st"
        "2" -> return "2nd"
        "3" -> return "3rd"
        else -> return "${i}th"
    }
}