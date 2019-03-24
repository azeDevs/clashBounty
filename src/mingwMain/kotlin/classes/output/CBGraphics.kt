package classes.output

import kotlinx.cinterop.*
import platform.windows.*


fun displayDialog(title: String, message: String) {
    val pathText = StringBuilder()
    memScoped {
        val buffer = allocArray<UShortVar>(MAX_PATH)
        GetModuleFileNameW(null, buffer, MAX_PATH)
        val path: String = buffer.toKString().split("\\").dropLast(1).joinToString("\\")
        pathText.append("path: $path\n")
    }
    MessageBoxW(null, "message:\n$message\n$pathText",
        title, (MB_OK or MB_ICONASTERISK).convert())
}