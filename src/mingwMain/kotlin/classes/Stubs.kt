package classes

// Temporary stub class until actual XrdMemoryExtractorStub is finished
open class XrdMemoryExtractorStub: XrdClashBountyApi {
    override fun isXrdRunning(): Boolean = false
    override fun connectToXrd(steamUserId: Int, displayName: String): Boolean = false
    override fun isXrdConnected(): Boolean = false
    override fun getXrdData(): Set<PlayerData> = HashSet()
}