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
    fun connectToXrd(steamUserId: Int, displayName: String): Boolean

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
 * Class object produced by [ClashBountyApi]
 */
class PlayerData(val displayName: String, val steamUserId: Int) {

    var characterId: Byte = 0x0
    var matchesPlayed: Byte = 0x0
    var matchesWon: Byte = 0x0
    var loadingPct: Byte = 0x0

}


/**
 * Character ID constants
 * based on training mode quick selection order
 * to be used within [PlayerData] objects
 */
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

}

