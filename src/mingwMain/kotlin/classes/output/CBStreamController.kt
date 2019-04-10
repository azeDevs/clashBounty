//package classes.session
//
//import classes.session.getFullscreen
//import classes.session.logInfo
//
//val strs = "name, 2012, 2017".split("\n").toTypedArray()
//
//var target:String = "./lobby_1st/txt_lobby_1st_"
//
//fun setLobbyStats() {
////    if(opendir("./lobby_1st") == null) mkdir("./lobby_1st")
////    if(opendir("./lobby_2nd") == null) mkdir("./lobby_2nd")
////    if(opendir("./lobby_3rd") == null) mkdir("./lobby_3rd")
////    if(opendir("./lobby_4th") == null) mkdir("./lobby_4th")
//    players.filter { data -> data.value.getPlacing() < 5 }.forEach {
//            data -> writePlayerToLobby(data.value)
//    }
//}
//
//fun showLobbyOverlay() {
//
//}
//
//fun showLoadingOverlay() {
//
//}
//
//fun showCombatOverlay() {
//
//}
//
//private fun writePlayerToLobby(player:Player) {
//    target = "./lobby_${player.getPlacingString()}/txt_lobby_${player.getPlacingString()}_"
//    setRank(player.getRiskRatingString())
//    writeToTextFile("${target}name.txt", player.getDisplayNameString())
//    writeToTextFile("${target}rank.txt", "${player.getPlacingString()}\n${player.getRiskRatingString()}")
//    writeToTextFile("${target}bounty.txt", player.getBountyScoreString())
//    logInfo("writePlayerToLobby")
//}
//
//private fun setRank(rank: String) {
//    when (rank.split("\n").toTypedArray().get(1).subSequence(0, 1)) {
//        "S" -> {
//            writeToTextFile("${target}S.txt", getFullscreen())
//            writeToTextFile("${target}A.txt", "")
//            writeToTextFile("${target}B.txt", "")
//            writeToTextFile("${target}C.txt", "")
//            writeToTextFile("${target}D.txt", "")
//        }
//        "A" -> {
//            writeToTextFile("${target}S.txt", "")
//            writeToTextFile("${target}A.txt", getFullscreen())
//            writeToTextFile("${target}B.txt", "")
//            writeToTextFile("${target}C.txt", "")
//            writeToTextFile("${target}D.txt", "")
//        }
//        "B" -> {
//            writeToTextFile("${target}S.txt", "")
//            writeToTextFile("${target}A.txt", "")
//            writeToTextFile("${target}B.txt", getFullscreen())
//            writeToTextFile("${target}C.txt", "")
//            writeToTextFile("${target}D.txt", "")
//        }
//        "C" -> {
//            writeToTextFile("${target}S.txt", "")
//            writeToTextFile("${target}A.txt", "")
//            writeToTextFile("${target}B.txt", "")
//            writeToTextFile("${target}C.txt", getFullscreen())
//            writeToTextFile("${target}D.txt", "")
//        }
//        "D" -> {
//            writeToTextFile("${target}S.txt", "")
//            writeToTextFile("${target}A.txt", "")
//            writeToTextFile("${target}B.txt", "")
//            writeToTextFile("${target}C.txt", "")
//            writeToTextFile("${target}D.txt", getFullscreen())
//        }
//    }
//}