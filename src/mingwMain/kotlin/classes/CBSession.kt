package classes

import classes.Character.getCharacterName


class Session {
    constructor() {
        writeToTextFile("legacy", "#13243546b120200c4w18m24p58e2789!#8465133b98700c0w10m364p664e1024!")
        readFromTextFile("legacy").split("!").forEach { legacy ->
            playerSessions.put(Player(legacy).getId(), Player(legacy))
        }
    }

    private val xrdApi: XrdApi = XrdMemReader()
    private var playerSessions: MutableMap<Long, Player> = HashMap()

    fun updatePlayerData(): List<Player> {
        if (xrdApi.isXrdRunning() && xrdApi.connectToXrd()) {
            val xrdData = xrdApi.getXrdData()
            playerSessions.forEach { session ->
                var player = session.value
                player.inLobby(false)
                xrdData.forEach { data ->
                    if (session.key == data.steamUserId) {
                        player.inLobby(true)
                        player.update(data)
                    }
                }
            }
            return playerSessions.values.toList().sortedBy { player -> player.getBounty() }
        } else return emptyList()
    }

}

class Player(val legacy: String) {

    private var inLobby = false
    private var name = "???"
    private var character = "???"
    private var steamId = extract("#").toLong()
    private var bounty = extract("b").toInt()
    private var chain = extract("c").toInt()
    private var wins = extract("w").toInt()
    private var matches = extract("m").toInt()
    private var present = extract("p").toInt()
    private var existed = extract("e").toInt()
    private var loadPct = 0

    lateinit private var lastUpdate: PlayerUpdate
    private fun extract(type: String) = Regex("(?<=${type})(d+)").find(legacy).toString()

    fun update(currUpdate: PlayerUpdate) {
        if (lastUpdate == null) lastUpdate = currUpdate
        if (name.length > 25) name = name.substring(25)
        else name = currUpdate.displayName
        character = getCharacterName(currUpdate.characterId)
        loadPct = currUpdate.loadingPct.toInt()

        if (justWon(currUpdate.matchesWon.toInt())) { chain++; wins++; matches++ }
        else if (justPlayed(currUpdate.matchesPlayed.toInt())) {
            if (chain < 2) chain = 0; else chain -= 2; matches++
        }

        lastUpdate = currUpdate
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

    fun getTurnsActive(): Int {
        return present
    }

    fun getTurnsExisted(): Int {
        return existed
    }

    fun getLoadPercent(): Int {
        return loadPct
    }

    private fun justWon(currWins: Int): Boolean {
        return currWins > lastUpdate.matchesWon
    }

    private fun justPlayed(currPlayed: Int): Boolean {
        return currPlayed > lastUpdate.matchesPlayed
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

}

class PlayerUpdate(val displayName: String, val steamUserId: Long) {
    var characterId: Byte = 0x0
    var matchesPlayed: Byte = 0x0
    var matchesWon: Byte = 0x0
    var loadingPct: Byte = 0x0

}