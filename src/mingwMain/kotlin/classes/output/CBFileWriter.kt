package classes.output

import kotlinx.cinterop.CPointer
import platform.posix.FILE
import platform.posix.fclose
import platform.posix.fopen
import platform.posix.fprintf
import kotlin.system.getTimeMillis


fun writeOut(text: String) {
    writeToTextFile("$text\n", "out")
    logInfo("writeOut()")
}

fun logInfo(text: String) {
    writeToTextFile("[] $text\n", "log")
    print("[] $text\n")
}

class LogBool(val text:String, val value:Boolean)
fun logBool(logBool: LogBool):Boolean {
    logInfo(logBool.text)
    return logBool.value
}

private fun writeToTextFile(text: String, file: String) {
    val fp: CPointer<FILE>? = fopen("${file}.txt" ,"a")
    fprintf(fp, text)
    fclose(fp)
}

private val timeStart = getTimeMillis()
private fun getTimeStamp(): String {
    val secondsElapsed = getTimeMillis() - timeStart * 0.001
    var resultingMinutes = (secondsElapsed / 60).toString()
    var resultingSeconds = (secondsElapsed % 60).toString()
    for (i in resultingMinutes.length..4) resultingMinutes = "0$resultingMinutes"
    if (resultingSeconds.length > 1) resultingSeconds = "$resultingSeconds"
    else resultingSeconds = "0$resultingSeconds"
    return "[ $resultingMinutes:$resultingSeconds ] "
}