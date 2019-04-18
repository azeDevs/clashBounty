package classes

import kotlinx.cinterop.CPointer
import platform.posix.FILE
import platform.posix.fclose
import platform.posix.fopen
import platform.posix.fprintf

fun writeToFile(fileName: String, text: String) {
    val fp: CPointer<FILE>? = fopen("${fileName}.txt" ,"w+")
    fprintf(fp, "$text")
    fclose(fp)
}

fun truncate(name: String, length: Int): String {
    if (name.length > length) return name.substring(0, length)
    else return name
}

object Character {
    const val SO: Byte = 0x00 ;const val KY: Byte = 0x01 ;const val MA: Byte = 0x02 ;const val MI: Byte = 0x03 ;const val ZA: Byte = 0x04 ;const val PO: Byte = 0x05 ;const val CH: Byte = 0x06 ;const val FA: Byte = 0x07 ;const val AX: Byte = 0x08 ;const val VE: Byte = 0x09 ;const val SL: Byte = 0x0A ;const val IN: Byte = 0x0B ;const val BE: Byte = 0x0C ;const val RA: Byte = 0x0D ;const val SI: Byte = 0x0E ;const val EL: Byte = 0x0F ;const val LE: Byte = 0x10 ;const val JO: Byte = 0x11 ;const val JC: Byte = 0x12 ;const val JM: Byte = 0x13 ;const val KU: Byte = 0x14 ;const val RV: Byte = 0x15 ;const val DI: Byte = 0x16 ;const val BA: Byte = 0x17 ;const val AN: Byte = 0x18
    val initials: Boolean = false
    fun getCharacterName(byte: Byte): String {
        when (byte) {
            SO -> if (initials) return "SO" else return "Sol Badguy"
            KY -> if (initials) return "KY" else return "Ky Kiske"
            MA -> if (initials) return "MA" else return "May"
            MI -> if (initials) return "MI" else return "Millia Rage"
            ZA -> if (initials) return "ZA" else return "Zato=1"
            PO -> if (initials) return "PO" else return "Potemkin"
            CH -> if (initials) return "CH" else return "Chipp Zanuff"
            FA -> if (initials) return "FA" else return "Faust"
            AX -> if (initials) return "AX" else return "Axl Low"
            VE -> if (initials) return "VE" else return "Venom"
            SL -> if (initials) return "SL" else return "Slayer"
            IN -> if (initials) return "IN" else return "I-No"
            BE -> if (initials) return "BE" else return "Bedman"
            RA -> if (initials) return "RA" else return "Ramlethal Valentine"
            SI -> if (initials) return "SI" else return "Sin Kiske"
            EL -> if (initials) return "EL" else return "Elpelt Valentine"
            LE -> if (initials) return "LE" else return "Leo Whitefang"
            JO -> if (initials) return "JO" else return "Johnny Sfondi"
            JC -> if (initials) return "JC" else return "Jack-O Valentine"
            JM -> if (initials) return "JM" else return "Jam Kuradoberi"
            KU -> if (initials) return "KU" else return "Kum Haehyun"
            RV -> if (initials) return "RV" else return "Raven"
            DI -> if (initials) return "DI" else return "Dizzy"
            BA -> if (initials) return "BA" else return "Baiken"
            AN -> if (initials) return "AN" else return "Answer"
            else -> if (initials) return "??" else return "???"
        }
    }
}