package classes.session

import classes.appendToConsole
import classes.consoleText
import kotlinx.cinterop.CPointer
import libui.ktx.Button
import libui.ktx.TextArea
import libui.ktx.TextField
import platform.posix.FILE
import platform.posix.fclose
import platform.posix.fopen
import platform.posix.fprintf

private val IC_INFO = "> "
private val IC_BOOL = "? "
private val IC_FUNC = "ƒ "
private val IC_STEP = "… "
private val IC_WARN = "\uD83D\uDCA3 "

fun logInfo(text: String) = addLog("\n$IC_INFO $text")
fun logFunc(text: String) = addLog("\n$IC_FUNC $text")
fun logStep(text: String) = addLog("\n$IC_STEP $text")
fun logWarn(text: String) = addLog("\n$IC_WARN $text")
fun logBool(text: String, bool: Boolean): Boolean { addLog("\n$IC_BOOL $text: ${bool}");return bool }

private fun addLog(logText: String) {
    writeToTextFile("log", logText)
    appendToConsole(logText)
}

fun writeToTextFile(file: String, text: String) {
    val fp: CPointer<FILE>? = fopen("${file}.txt" ,"a")
    fprintf(fp, "$text")
    fclose(fp)
}

fun getFullscreen(): String {
    var out = ""
    for (i in 1..96) out += "█"
    return out
}