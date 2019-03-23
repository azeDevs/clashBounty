package classes

import kotlinx.cinterop.CPointer
import platform.posix.FILE
import platform.posix.fclose
import platform.posix.fopen
import platform.posix.fprintf

fun writeOut(text: String) {
    val fp: CPointer<FILE>? = fopen("out.txt" ,"a")
    fprintf(fp, text)
    fclose(fp)
}