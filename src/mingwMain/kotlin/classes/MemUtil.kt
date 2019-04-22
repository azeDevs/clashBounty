package classes

import kotlinx.cinterop.*
import kwinhelp.*
import platform.windows.CloseHandle
import platform.windows.HANDLE
import platform.windows.OpenProcess
import platform.windows.ReadProcessMemory

// Name: Player 1 HP
// VariableType: 4 Bytes
// Address: "GuiltyGearXrd.exe"+01B18C78
// Offset: 9CC

// Name: Player 2 HP
// VariableType: 4 Bytes
// Address: "GuiltyGearXrd.exe"+01B18C7C
// Offset: 9CC

class MemData(val description:String, val offsets : LongArray, val varSize : Int) { var data : Long = -1L }

val ML = arrayOf(
    MemData("Player 1 HP", longArrayOf(0x01B18C78L, 0x9CCL), 4),
    MemData("Player 2 HP", longArrayOf(0x01B18C7CL, 0x9CCL), 4),
    MemData("Match Timer", longArrayOf(0x0177A8ACL, 0x708L, 0x4CL, 0x450L), 4)
)

fun getMemData(memData:MemData): MemData {

    // CONNECT
    var dataPointer: CPointer<ByteVar>?
    var phandle: HANDLE?

    val PROC_ALL_ACCESS: UInt = 0x438u

    val procname = "GuiltyGearXrd.exe"
    var snap = CreateToolhelp32Snapshot(TH32CS_SNAPPROCESS, 0)
    val pe32 : PROCESSENTRY32 = nativeHeap.alloc()
    pe32.dwSize = sizeOf<PROCESSENTRY32>().toUInt()
    var pid : UInt = 0u
    if (Process32First(snap, pe32.ptr) == 0) logWarn("Process32First failed!")
    while (Process32Next(snap, pe32.ptr) != 0) {
        val entryname = pe32.szExeFile.toKString()
        if (procname.equals(entryname)) {
            pid = pe32.th32ProcessID.toUInt()
            break
        }
    }
    CloseHandle(snap)
    phandle = OpenProcess(PROC_ALL_ACCESS, 0, pid)


    logFunc("getDataAddr")
    var mod = nativeHeap.alloc<MODULEENTRY32>()
    mod.dwSize = sizeOf<MODULEENTRY32>().toUInt()
    var hSnap = CreateToolhelp32Snapshot(TH32CS_SNAPMODULE, pid)

    dataPointer = 0L.toCPointer()
    while(Module32Next(hSnap, mod.ptr) != 0){
        if (procname.equals(mod.szModule.toKString())) {
            CloseHandle(hSnap)
            dataPointer = (mod.modBaseAddr.toLong() + memData.offsets[0]).toCPointer<ByteVar>()
            var offsetlist = memData.offsets.drop(1)
            while (offsetlist.size > 0) {
                var buffer = nativeHeap.alloc<UIntVar>()
                var bytesread = nativeHeap.alloc<ULongVar>()
                var error = ReadProcessMemory(phandle, dataPointer, buffer.ptr, 4, bytesread.ptr)
                if (error == 0) {
                    dataPointer = 0L.toCPointer()
                    break
                }
                var newlptr = buffer.value.toLong()
                dataPointer = (newlptr + offsetlist[0]).toCPointer()
                offsetlist = offsetlist.drop(1)
            }
        }
    }

    logFunc("getXrdData")
    if (!(dataPointer != null && !dataPointer!!.equals(0L.toCPointer<ByteVar>()))) return memData
    var buffer = nativeHeap.allocArray<ByteVar>(memData.varSize)
    var bytesread = nativeHeap.alloc<ULongVar>()
    var error = ReadProcessMemory(phandle, dataPointer, buffer, memData.varSize.toULong(), bytesread.ptr)
    if(error == 0) return memData
    when (memData.varSize) {
        1 -> memData.data = buffer.reinterpret<ByteVar>().pointed.value.toUByte().toLong()
        2 -> memData.data = buffer.reinterpret<ShortVar>().pointed.value.toUShort().toLong()
        4 -> memData.data = buffer.reinterpret<IntVar>().pointed.value.toUInt().toLong()
    }
    return memData
}
