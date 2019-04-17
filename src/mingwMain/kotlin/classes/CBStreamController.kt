package classes.session

import classes.Player
import classes.writeToFile

fun writeLobbyFiles(players: List<Player>) {
    for (i in 0..3) if (players.size > i && players.get(i).getBounty() > 0) {
        val remote = "SLOT ${i}\n" +
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

private fun setRank(rank: String) {
    var target = "./lobby_1st/txt_lobby_1st_"
    when (rank.split("\n").toTypedArray().get(1).subSequence(0, 1)) {
        "S" -> {
            writeToFile("${target}S.txt", getFullscreen(true))
            writeToFile("${target}A.txt", getFullscreen(false))
            writeToFile("${target}B.txt", getFullscreen(false))
            writeToFile("${target}C.txt", getFullscreen(false))
            writeToFile("${target}D.txt", getFullscreen(false))
        }
        "A" -> {
            writeToFile("${target}S.txt", getFullscreen(false))
            writeToFile("${target}A.txt", getFullscreen(true))
            writeToFile("${target}B.txt", getFullscreen(false))
            writeToFile("${target}C.txt", getFullscreen(false))
            writeToFile("${target}D.txt", getFullscreen(false))
        }
        "B" -> {
            writeToFile("${target}S.txt", getFullscreen(false))
            writeToFile("${target}A.txt", getFullscreen(false))
            writeToFile("${target}B.txt", getFullscreen(true))
            writeToFile("${target}C.txt", getFullscreen(false))
            writeToFile("${target}D.txt", getFullscreen(false))
        }
        "C" -> {
            writeToFile("${target}S.txt", getFullscreen(false))
            writeToFile("${target}A.txt", getFullscreen(false))
            writeToFile("${target}B.txt", getFullscreen(false))
            writeToFile("${target}C.txt", getFullscreen(true))
            writeToFile("${target}D.txt", getFullscreen(false))
        }
        "D" -> {
            writeToFile("${target}S.txt", getFullscreen(false))
            writeToFile("${target}A.txt", getFullscreen(false))
            writeToFile("${target}B.txt", getFullscreen(false))
            writeToFile("${target}C.txt", getFullscreen(false))
            writeToFile("${target}D.txt", getFullscreen(true))
        }
    }
}


private fun getFullscreen(enabled: Boolean): String {
    if (enabled) return "████████████████████████████████████████████████████████████████████████████████████████████████"
    else return "                                                                                                "
}