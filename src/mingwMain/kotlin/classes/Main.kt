package classes

import kotlinx.cinterop.*
import platform.posix.*

fun main() {
    displayAppWindow()
}

fun writeToFile(fileName: String, text: String) {
    val fp: CPointer<FILE>? = fopen("${fileName}.txt" ,"w+")
    fprintf(fp, "$text")
    fclose(fp)
}