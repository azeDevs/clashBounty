package classes

import kotlinx.cinterop.*
import platform.posix.*

fun main() {
    displayAppWindow()
}

fun appendToTextFile(fileName: String, text: String): String {
    val fp: CPointer<FILE>? = fopen("${fileName}.txt" ,"a")
    fprintf(fp, "$text")
    fclose(fp)
    return text
}

fun writeToTextFile(fileName: String, text: String): String {
    val fp: CPointer<FILE>? = fopen("${fileName}.txt" ,"w")
    fprintf(fp, "$text")
    fclose(fp)
    return text
}

fun readFromTextFile(fileName: String): String {
    var result = ""
    val file = fopen("${fileName}.txt" ,"r")
    fseek(file, 0, SEEK_END)
    val fileSize = ftell(file)
    fseek(file, 0, SEEK_SET)
    memScoped {
        val buffer = allocArray<ByteVar>(fileSize)
        fread(buffer, fileSize.toULong(), 1, file)
        result = buffer.toKString()
    }
    fclose(file)
    return result
}