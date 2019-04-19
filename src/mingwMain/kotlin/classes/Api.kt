package classes

import classes.Character.getCharacterName


/**
 * Interface for Labryz to use 👍
 */
interface XrdApi {
    /**
     * Check if Xrd process is running
     *
     * @return is a Guilty Gear Xrd REV2 process currently running
     */
    fun isXrdRunning(): Boolean

    /**
     * Init scan for Xrd lobby memory addresses, return if connection in progress
     *
     * @return a scan for the Xrd lobby memory addresses is currently in progress
     */
    fun connectToXrd(): Boolean

    /**
     * Check if Xrd lobby memory addresses have been located
     *
     * @return the Xrd lobby memory addresses have been found and stored for reference
     */
    fun isXrdConnected(): Boolean

    /**
     * Serve a HashMap of PlayerData, using the steamUserId as key
     *
     * @return a Set of the Xrd lobby's currentOverlay active players and their data
     */
    fun getXrdData(): Set<PlayerData>
}


/**
 * Class object produced by [MemReader]
 */
class PlayerData(
    val displayName: String,
    val steamUserId: Long,
    val characterId: Byte,
    val matchesTotal: Int,
    val matchesWon: Int,
    val loadingPct: Int) {
    fun equals(otherData: PlayerData): Boolean {
        return displayName.equals(otherData.displayName) &&
                characterId.equals(otherData.characterId) &&
                matchesTotal.equals(otherData.matchesTotal) &&
                matchesWon.equals(otherData.matchesWon) &&
                loadingPct.equals(otherData.loadingPct)
    }
}



