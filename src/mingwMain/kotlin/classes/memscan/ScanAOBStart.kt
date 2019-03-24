package classes.memscan

import classes.memscan.LowLevelMutables.currentAddr
import classes.memscan.LowLevelMutables.meminfobuffer
import kotlinx.cinterop.*
import platform.windows.MEMORY_BASIC_INFORMATION
import platform.windows.VirtualQueryEx


fun getAOB(aob: ByteArray, pHandle: CPointer<out CPointed>?): CPointer<out CPointed>? {
    var locatedAddr: CPointer<out CPointed>? = null
    var inforeturned = VirtualQueryEx(pHandle, currentAddr, meminfobuffer, sizeOf<MEMORY_BASIC_INFORMATION>().toULong())
    currentAddr = (meminfobuffer.BaseAddress.toLong() + meminfobuffer.RegionSize.toLong()).toCPointer()
    if(isMemAccessible()) locatedAddr = scanBytes(aob, pHandle)
    return locatedAddr
}

private fun isMemAccessible():Boolean {
    return (meminfobuffer.Protect and LowLevelConstants.MEM_READWRITE
            == LowLevelConstants.MEM_READWRITE
            && meminfobuffer.Protect and LowLevelConstants.MEM_GUARD
            != LowLevelConstants.MEM_GUARD)
}