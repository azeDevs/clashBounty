package classes

import kotlinx.cinterop.*
import kwinhelp.*
import platform.windows.CloseHandle
import platform.windows.HANDLE
import platform.windows.OpenProcess
import platform.windows.ReadProcessMemory

class MemReader : XrdApi {

    val PROC_ALL_ACCESS: UInt = 0x438u
    val GG_STRUCT_SIZE = 0x48

    override fun isXrdRunning() : Boolean {
        val procname = "GuiltyGearXrd.exe"
        var snap = CreateToolhelp32Snapshot(TH32CS_SNAPPROCESS, 0)
        val pe32size: UInt = sizeOf<PROCESSENTRY32>().toUInt()
        val pe32ptr = nativeHeap.alloc<PROCESSENTRY32>()
        val pe32: PROCESSENTRY32 = pe32ptr
        pe32.dwSize = pe32size
        var pid: UInt = 0u
        if (Process32First(snap, pe32.ptr) == 0) {
            logWarn("Process32First failed!")
            return false
        }
        while (Process32Next(snap, pe32.ptr) != 0) {
            val entryname = pe32.szExeFile.toKString()
            if (procname.equals(entryname)) {
                pid = pe32.th32ProcessID.toUInt()
                break
            }
        }
        CloseHandle(snap)
        return pid != 0u
    }

    override fun connectToXrd() : Boolean {
        val procname = "GuiltyGearXrd.exe"
        var snap = CreateToolhelp32Snapshot(TH32CS_SNAPPROCESS, 0)
        val pe32size: UInt = sizeOf<PROCESSENTRY32>().toUInt()
        val pe32ptr = nativeHeap.alloc<PROCESSENTRY32>()
        val pe32 : PROCESSENTRY32 = pe32ptr
        pe32.dwSize = pe32size
        var pid : UInt = 0u
        if (Process32First(snap, pe32.ptr) == 0) {
            logWarn("Process32First failed!")
            return Process32First(snap, pe32.ptr) != 0
        }
        while (Process32Next(snap, pe32.ptr) != 0) {
            val entryname = pe32.szExeFile.toKString()
            if (procname.equals(entryname)) {
                pid = pe32.th32ProcessID.toUInt()
                break
            }
        }
        CloseHandle(snap)
        if (pid == 0u) {
            logWarn("$procname not open!")
            return pid != 0u
        } else {
            println("$procname has an id of $pid")
        }
        phandle = OpenProcess(PROC_ALL_ACCESS, 0, pid)
        if (phandle == null) {
            logWarn("$procname failed to open.")
            return phandle != null
        } else {
            println("$procname was opened!")
        }
        infoAddr = getDataAddr(pid, "GuiltyGearXrd.exe", phandle)
        if(infoAddr?.toLong() == 0L) {
            logWarn("Failed to find Lobby")
            return infoAddr?.toLong() != 0L
        }
        println("Lobby Data Address: " + infoAddr.toLong().toString(16))
        return true
    }

    private fun getDataAddr(pid : UInt, modulename : String, pHandle : CPointer<out CPointed>?): CPointer<ByteVar>? {
        var mod = nativeHeap.alloc<MODULEENTRY32>()
        mod.dwSize = sizeOf<MODULEENTRY32>().toUInt()
        var hSnap = CreateToolhelp32Snapshot(TH32CS_SNAPMODULE, pid)

        while(Module32Next(hSnap, mod.ptr) != 0){
            val entryname = mod.szModule.toKString()
            if (modulename.equals(entryname)) {
                CloseHandle(hSnap)
                var modbase = mod.modBaseAddr
                var modlong = modbase.toLong()
                var modoffset = (modlong + 0x1C25AB4L).toCPointer<ByteVar>()
                var buffer = nativeHeap.alloc<IntVar>()
                var bytesread = nativeHeap.alloc<ULongVar>()
                var error = ReadProcessMemory(pHandle, modoffset, buffer.ptr, 4, bytesread.ptr)
                if (error == 0){
                    return 0L.toCPointer()
                }
                var newlptr = buffer.value.toLong()
                newlptr = newlptr + 0x44CL
                var newptr = newlptr.toCPointer<ByteVar>()
                return newptr
            }
        }
        return 0L.toCPointer()
    }

    override fun isXrdConnected(): Boolean {
        val connected = (infoAddr != null) && !infoAddr!!.equals(0L.toCPointer<ByteVar>())
        return connected
    }

    override fun getXrdData(): Set<PlayerData> {
        if (!isXrdConnected()) return emptySet()

        var playerDataSet = HashSet<PlayerData>()
        for(i in 0..8){
            var pdoffset = (infoAddr.toLong() + (GG_STRUCT_SIZE * i).toLong()).toCPointer<ByteVar>()
            var buffer = nativeHeap.allocArray<ByteVar>(GG_STRUCT_SIZE)
            var bytesread = nativeHeap.alloc<ULongVar>()
            var error = ReadProcessMemory(phandle, pdoffset, buffer, GG_STRUCT_SIZE.toULong(), bytesread.ptr)
            var bufbytearray = buffer.pointed.readValues(GG_STRUCT_SIZE).getBytes()

            if(error == 0) return emptySet()
            else if (i == 0 && bufbytearray[0xC].toInt() == 0) return emptySet()
            else if (bufbytearray[0xC].toInt() == 0) continue

            var dispname = bufbytearray.stringFromUtf8(0xC, 0x24).trim('\u0000')
            var steamidptr = buffer.reinterpret<LongVar>()
            var steamid = steamidptr.pointed.value
            var playerData = PlayerData(
                displayName = truncate(dispname, 24),
                steamUserId = steamid,
                characterId = bufbytearray[0x36],
                matchesTotal = bufbytearray[8].toInt(),
                matchesWon = bufbytearray[0xA].toInt(),
                loadingPct = bufbytearray[0x40].toInt())
            playerDataSet.add(playerData)
        }
        return playerDataSet
    }

    var infoAddr = 0L.toCPointer<ByteVar>()
    var phandle : HANDLE? = nativeHeap.alloc<IntVar>().ptr

}