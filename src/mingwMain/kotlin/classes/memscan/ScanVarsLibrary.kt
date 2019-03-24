package classes.memscan

import classes.output.LogBool
import kotlinx.cinterop.*
import kwinhelp.PROCESSENTRY32
import platform.windows._MEMORY_BASIC_INFORMATION

fun getProcessName(): String = "GuiltyGearXrd.exe"
fun getProcessEntry32size(): UInt = sizeOf<PROCESSENTRY32>().toUInt()
fun getProcessEntry32ptr(): PROCESSENTRY32 = nativeHeap.alloc()
fun getProcessEntry32(): PROCESSENTRY32 = getProcessEntry32ptr()

object ScanLogs {
    val FIND_PROC_FAILED: LogBool =     LogBool("${getProcessName()} failed to open!", false)
    val FOUND_PROCESS: LogBool =        LogBool("${getProcessName()} was opened!", true)
    val FIND_AOBS_FAILED: LogBool =     LogBool("Failed to find AOBs", false)
    val FOUND_ADDRESS: LogBool =        LogBool("Found Lobby Data Address!", true)
    val FOUND_BYTE_RESULT: LogBool =    LogBool("Result found!", true)
}

object LowLevelMutables {
    var currentAddr = 0L.toCPointer<IntVar>()
    var bytesread = nativeHeap.alloc<ULongVar>()
    var buffer = nativeHeap.allocArray<ByteVar>(0x30)
    var meminfobuffer = nativeHeap.alloc<_MEMORY_BASIC_INFORMATION>()
}

object LowLevelConstants {
    const val PROC_ALL_ACCESS: UInt = 0x438u
    const val MEM_READWRITE: UInt = 4u
    const val MEM_GUARD: UInt = 0x100u
    const val GG_STRUCT_SIZE = 0x48
}