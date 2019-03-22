package classes

object Host {
    const val STEAM_ID: Int = -1
    const val DISPLAY_NAME: String = "noname"
}

fun main(args: Array<String>) {
    val session = ClashBountySession()
    println("Starting Clash Bounty Session...")
    session.start()
}

