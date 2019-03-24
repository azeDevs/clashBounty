package classes

import classes.cbmodel.ClashBountySession


object Host {
    const val STUB_ClashBountyImpl: Boolean = true
    const val DISPLAY_NAME: String = "Labryz"
    const val STEAM_ID: Long = 76561198128284333L
}

fun main() {
    ClashBountySession().start()
//    writeOut("Some text to be written...\nBOUNTY: $100")
//    displayDialog("Title", "Lorem Ipsum Dolor Sit Amet")
}