package classes

import classes.Character.getCharacterName
import classes.session.ClashBountyApi
import classes.session.logFunc
import classes.session.logInfo
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import libui.ktx.*

val xrdApi: ClashBountyApi = ClashBountyImpl()
lateinit var statusText: TextField
lateinit var connectBtn: Button
lateinit var consoleText: TextArea
lateinit var refreshBtn: Button

fun main() {
    displayAppWindow()
    GlobalScope.launch {
        logFunc("coroutine")
        delay(800)
        uiLoop()
    }
}

private fun displayAppWindow() = appWindow("gearNet: Clash Bounty", 320, 240) {
    val xrdApi: ClashBountyApi = ClashBountyImpl()
    vbox {
            statusText = textfield { readonly = true; value = "..." }

            connectBtn = button(text = "Connect to GGXrd") {
                action {
                    statusText.value = "Xrd is not connected."
                    if (xrdApi.connectToXrd()) {
                        statusText.value = "Xrd Connected!"
                        refreshBtn.enabled = true
                    }
                }
            }

        consoleText = textarea { readonly = true; stretchy = true; value = "" }
        refreshBtn = button(text = "Get lobby data") {
            action {
                logXrdData()
            }
        }
        refreshBtn.enabled = false
    }
}

private fun logXrdData() {
    logFunc("logXrdData")
    var dataStr = "LOBBY PLAYERS: ${xrdApi.getXrdData().size}"
    xrdApi.getXrdData().forEach { data ->
        dataStr += "\nPlayer: ${data.displayName}"
        dataStr += "\nSteamID: ${data.steamUserId}"
        dataStr += "\nCharacter: ${getCharacterName(data.characterId)}"
    }
    logInfo(dataStr)
}

private fun uiLoop() {
    logFunc("uiLoop")
    if (xrdApi.isXrdRunning()) {
        setAppStatus("Xrd is running.")
        connectBtn.text = "Connect to GGXrd!"
        connectBtn.enabled = true
    } else {
        setAppStatus("Xrd is not running.")
        connectBtn.text = "Connect to GGXrd?"
        connectBtn.enabled = false
    }
}

private fun setAppStatus(text: String) {
    statusText.value = text
}

fun appendToConsole(text: String) {
    consoleText.append(text)
    consoleText.show()
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

    fun getCharacterName(byte: Byte):String {
        when (byte) {
            SO -> return "Sol Badguy"
            KY -> return "Ky Kiske"
            MA -> return "May"
            MI -> return "Millia"
            ZA -> return "Zato=1"
            PO -> return "Potemkin"
            CH -> return "Chipp"
            FA -> return "Faust"
            AX -> return "Axl Low"
            VE -> return "Venom"
            SL -> return "Slayer"
            IN -> return "I-No"
            BE -> return "Bedman"
            RA -> return "Ramlethal"
            SI -> return "Sin"
            EL -> return "Elpelt"
            LE -> return "Leo Whitefang"
            JO -> return "Johnny"
            JC -> return "Jack-O"
            JM -> return "Jam"
            KU -> return "Kum Haehyun"
            RV -> return "Raven"
            DI -> return "Dizzy"
            BA -> return "Baiken"
            AN -> return "Answer"
            else -> return "???"
        }
    }
}