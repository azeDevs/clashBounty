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

class MemData(val description:String, val address:Long, val offset:List<Long>) { var dataInt:Int = -1 }

val ML = arrayOf(
    MemData("Player 1 HP", 0x01B18C78L, listOf(0x9CCL)),
    MemData("Player 2 HP", 0x01B18C7CL, listOf(0x9CCL)),
    MemData("Match Timer", 0x0177A8ACL, listOf(0x708L, 0x4CL, 0x450L))
)

fun getMemData(memData:MemData): MemData {

    // CONNECT
    var infoAddr: CPointer<ByteVar>?
    var phandle: HANDLE?

    val PROC_ALL_ACCESS: UInt = 0x438u
    val FOUR_BYTE = 4

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

    // GET ADDR
    logFunc("getDataAddr")
    var mod = nativeHeap.alloc<MODULEENTRY32>()
    mod.dwSize = sizeOf<MODULEENTRY32>().toUInt()
    var hSnap = CreateToolhelp32Snapshot(TH32CS_SNAPMODULE, pid)

    infoAddr = 0L.toCPointer()
    while(Module32Next(hSnap, mod.ptr) != 0){
        if (procname.equals(mod.szModule.toKString())) {
            CloseHandle(hSnap)
            var modlong = mod.modBaseAddr.toLong()
            var modoffset = (modlong + memData.address).toCPointer<ByteVar>()
            var buffer = nativeHeap.alloc<IntVar>()
            var bytesread = nativeHeap.alloc<ULongVar>()
            var error = ReadProcessMemory(phandle, modoffset, buffer.ptr, 4, bytesread.ptr)
            if (error == 0) { infoAddr = 0L.toCPointer(); continue }
            var newlptr = buffer.value.toLong()
            infoAddr = (newlptr + memData.offset.get(0)).toCPointer()
        }
    }

    logFunc("getXrdData")
    if (!(infoAddr != null && !infoAddr!!.equals(0L.toCPointer<ByteVar>()))) return memData
    var buffer = nativeHeap.allocArray<ByteVar>(FOUR_BYTE)
    var bytesread = nativeHeap.alloc<ULongVar>()
    var error = ReadProcessMemory(phandle, infoAddr, buffer, FOUR_BYTE.toULong(), bytesread.ptr)
    if(error == 0) return memData

    var intbuffer = buffer.reinterpret<IntVar>()
    memData.dataInt = intbuffer.pointed.value
    return memData
}
