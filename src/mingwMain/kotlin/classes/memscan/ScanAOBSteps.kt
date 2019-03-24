package classes.memscan

import classes.memscan.LowLevelMutables.buffer
import classes.memscan.LowLevelMutables.meminfobuffer
import classes.memscan.ScanLogs.FOUND_BYTE_RESULT
import classes.output.logBool
import kotlinx.cinterop.CPointed
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.get
import kotlinx.cinterop.toLong


fun scanBytes(aob: ByteArray, pHandle: CPointer<out CPointed>?): CPointer<out CPointed>? {
    val regionStart = meminfobuffer.BaseAddress.toLong()
    val regionClose = (meminfobuffer.BaseAddress.toLong() + meminfobuffer.RegionSize.toLong())
    for (p in regionStart .. regionClose step 4) {
//        var error = ReadProcessMemory(pHandle, p.toCPointer(), buffer, 0x30uL, bytesread)
//        if(error == 0) return null
//        if(bytesread.pointed == 0x30) {
//            if (foundByteResult(aob)) return p.toCPointer()
//        }

    }
    return null
}

private fun foundByteResult(aob: ByteArray): Boolean {
    var equal = true
    for (i in 0..0x30) if (aob[i] != buffer[i]) equal = false
    if (equal) return logBool(FOUND_BYTE_RESULT)
    return false
}

