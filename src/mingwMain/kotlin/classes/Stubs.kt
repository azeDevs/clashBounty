package classes

// Temporary stub class until actual ClashBountyImpl is finished
open class ClashBountyImplStub: ClashBountyApi {
    override fun isXrdRunning(): Boolean = false
    override fun connectToXrd(steamUserId: Long, displayName: String): Boolean = false
    override fun isXrdConnected(): Boolean = false
    override fun getXrdData(): Set<PlayerData> = HashSet()
}