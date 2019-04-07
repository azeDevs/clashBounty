package classes.memscan

import classes.memscan.LowLevelMutables.currentAddr
import classes.memscan.LowLevelMutables.meminfobuffer
import kotlinx.cinterop.CPointed
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.toCPointer
import kotlinx.cinterop.toLong
import kotlinx.cinterop.MEMORY_BASIC_INFORMATION
import kotlinx.cinterop.nativeHeap

var meminfobuffer = nativeHeap.allocate<MEMORY_BASIC_INFORMATION>()
fun getAOB(aob: ByteArray, pHandle: CPointer<out CPointed>?): CPointer<out CPointed>? {
    var locatedAddr: CPointer<out CPointed>? = null
    var currentAddr = 0uL.toCPointer()
    var inforeturned = VirtualQueryEx(pHandle, currentAddr, meminfobuffer, sizeOf<MEMORY_BASIC_INFORMATION>().toULong())
    while(inforeturned > 0 && locatedAddr == null) {
        if (isMemAccessible()) locatedAddr = scanBytes(aob, pHandle, currentAddr)
        currentAddr = (meminfobuffer.BaseAddress.toLong() + meminfobuffer.RegionSize.toLong()).toCPointer()
        inforeturned = VirtualQueryEx(pHandle, currentAddr, meminfobuffer, sizeOf<MEMORY_BASIC_INFORMATION>().toULong())
    }
    return locatedAddr
}
private fun scanBytes(aob: ByteArray, pHandle: CPointer<out CPointed>?, currentAddr : CPointer<out CPointed>?): CPointer<out CPointed>? {
    val regionStart = meminfobuffer.BaseAddress.toLong()
    val regionClose = (meminfobuffer.BaseAddress.toLong() + meminfobuffer.RegionSize.toLong())
    val bytesread = nativeHeap.allocate<Int>()
    val buffer = nativeHeap.allocArray<ByteVar>(0x30)
    for (p in regionStart .. regionClose step 4) {
          var poffset : ULong = p.toULong()
          var error = ReadProcessMemory(pHandle, poffset.toCPointer(), buffer, 0x30, bytesread)
          if(error == 0uL) {
              nativeHeap.free(bytesread)
              return null
          }
          if(bytesread<IntVar>.pointed == 0x30) {
              var equal = true
              for (i in 0..0x30) if (aob[i] != buffer[i]) equal = false
              if (equal) return locatedAddr
          }
    }
    return null
}

private fun foundByteResult(aob: ByteArray, buffer : CPointer<out CPointer>): Boolean {

}
private fun isMemAccessible():Boolean {
    return (meminfobuffer.Protect and LowLevelConstants.MEM_READWRITE
            == LowLevelConstants.MEM_READWRITE
            && meminfobuffer.Protect and LowLevelConstants.MEM_GUARD
            != LowLevelConstants.MEM_GUARD)
}