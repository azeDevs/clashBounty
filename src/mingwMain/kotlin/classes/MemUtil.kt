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

class MemData(val offsets : LongArray, val varSize : Int) { var data : Long = -1L }

fun getMemData(memData:MemData): MemData {

    // CONNECT
    var infoAddr: CPointer<ByteVar>?
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

    // GET ADDR
    logFunc("getDataAddr")
    var mod = nativeHeap.alloc<MODULEENTRY32>()
    mod.dwSize = sizeOf<MODULEENTRY32>().toUInt()
    var hSnap = CreateToolhelp32Snapshot(TH32CS_SNAPMODULE, pid)
    var dataPointer = 0L.toCPointer<ByteVar>()
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
    if(memData.varSize == 1) {
        var bytebuffer = buffer.reinterpret<ByteVar>()
        memData.data = bytebuffer.pointed.value.toUByte().toLong()
        return memData
    }
    if(memData.varSize == 2) {
        var shortbuffer = buffer.reinterpret<ShortVar>()
        memData.data = shortbuffer.pointed.value.toUShort().toLong()
        return memData
    }
    if(memData.varSize == 4) {
        var intbuffer = buffer.reinterpret<IntVar>()
        memData.data = intbuffer.pointed.value.toUInt().toLong()
        return memData
    }
    /*var outData = 0L
    if (memData.varType == 2) for(i in 0..1) outData += bufbytearray[1-i].toUByte().toInt() shl i
    if (memData.varType == 4) for(i in 0..3) outData += bufbytearray[3-i].toUByte().toInt() shl i
    if (memData.varType == 8) for(i in 0..7) outData += bufbytearray[7-i].toUByte().toInt() shl i
    memData.dataInt = outData.toInt()*/

    return memData
}

