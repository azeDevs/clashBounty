package classes


/**
 * Interface for Labryz to use 👍
 */
interface ClashBountyApi {
    /**
     * Check if Xrd process is running
     *
     * @return is a Guilty Gear Xrd REV2 process currently running
     */
    fun isXrdRunning(): Boolean

    /**
     * Init scan for Xrd lobby memory addresses, return if connection in progress
     *
     * @param steamUserId the User ID for the lobby host
     * @param displayName the Display Name for the lobby host
     * @return a scan for the Xrd lobby memory addresses is currently in progress
     */
    fun connectToXrd(steamUserId: Long, displayName: String): Boolean

    /**
     * Check if Xrd lobby memory addresses have been located
     *
     * @return the Xrd lobby memory addresses have been found and stored for reference
     */
    fun isXrdConnected(): Boolean

    /**
     * Serve a HashMap of PlayerData, using the steamUserId as key
     *
     * @return a Set of the Xrd lobby's current active players and their data
     */
    fun getXrdData(): Set<PlayerData>
}



/**
 * Class object produced by [XrdClashBountyApi]
 */
class PlayerData(val displayName: String, val steamUserId: Int) {
    var characterId: Byte = 0x0
    var matchesPlayed: Byte = 0x0
    var matchesWon: Byte = 0x0
    var loadingPct: Byte = 0x0
}


