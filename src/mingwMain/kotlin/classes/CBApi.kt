package classes


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
 * Class object produced by [XrdClashBountyApi]
 */
class PlayerData(val displayName: String, val steamUserId: Long) {
    var characterId: Byte = 0x0
    var matchesPlayed: Byte = 0x0
    var matchesWon: Byte = 0x0
    var loadingPct: Byte = 0x0
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
