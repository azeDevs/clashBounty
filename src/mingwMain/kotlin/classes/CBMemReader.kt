package classes

import kotlinx.cinterop.*
import kwinhelp.*
import platform.windows.CloseHandle
import platform.windows.HANDLE
import platform.windows.OpenProcess
import platform.windows.ReadProcessMemory

class XrdMemReader : XrdApi {

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
            return logBool("isXrdRunning", false)
        }
        while (Process32Next(snap, pe32.ptr) != 0) {
            val entryname = pe32.szExeFile.toKString()
            if (procname.equals(entryname)) {
                pid = pe32.th32ProcessID.toUInt()
                break
            }
        }
        CloseHandle(snap)
        return logBool("isXrdRunning", pid != 0u)
    }

    override fun connectToXrd() : Boolean {
        logFunc("connectToXrd")
        val procname = "GuiltyGearXrd.exe"
        var snap = CreateToolhelp32Snapshot(TH32CS_SNAPPROCESS, 0)
        val pe32size: UInt = sizeOf<PROCESSENTRY32>().toUInt()
        val pe32ptr = nativeHeap.alloc<PROCESSENTRY32>()
        val pe32 : PROCESSENTRY32 = pe32ptr
        pe32.dwSize = pe32size
        var pid : UInt = 0u
        if (Process32First(snap, pe32.ptr) == 0) {
            logWarn("Process32First failed!")
            return logBool("Process32First != 0", Process32First(snap, pe32.ptr) != 0)
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
            return logBool("pid != 0u", pid != 0u)
        } else {
            logInfo("$procname has an id of $pid")
        }
        phandle = OpenProcess(LowLevelConstants.PROC_ALL_ACCESS, 0, pid)
        if (phandle == null) {
            logWarn("$procname failed to open.")
            return logBool("phandle != null", phandle != null)
        } else {
            logInfo("$procname was opened!")
        }
        infoAddr = getDataAddr(pid, "GuiltyGearXrd.exe", phandle)
        if(infoAddr?.toLong() == 0L) {
            logWarn("Failed to find Lobby")
            return logBool("infoAddr?.toLong() != 0L", infoAddr?.toLong() != 0L)
        }
        logInfo("Lobby Data Address: " + infoAddr.toLong().toString(16))
        return true
    }

    private fun getDataAddr(pid : UInt, modulename : String, pHandle : CPointer<out CPointed>?): CPointer<ByteVar>? {
        logFunc("getDataAddr")
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

    // I'm working on the curfew alert adapter not refreshing properly
    override fun isXrdConnected(): Boolean {
        logFunc("isXrdConnected")
        val connected = (logBool("infoAddr != null", infoAddr != null) &&
                logBool("infoAddr.equals(CPointer)", !infoAddr!!.equals(0L.toCPointer<ByteVar>())))
        return logBool("isXrdConnected", connected)
    }

    override fun getXrdData(): Set<PlayerUpdate> {
        logFunc("getXrdData")
        if (!isXrdConnected()) {
            logStep("returning emptySet")
            return emptySet()
        }

        var pDataSet = HashSet<PlayerUpdate>()
        logStep("for(i in 0..8)")
        for(i in 0..8){
            var pdoffset = (infoAddr.toLong() + (LowLevelConstants.GG_STRUCT_SIZE * i).toLong()).toCPointer<ByteVar>()
            var buffer = nativeHeap.allocArray<ByteVar>(LowLevelConstants.GG_STRUCT_SIZE)
            var bytesread = nativeHeap.alloc<ULongVar>()
            var error = ReadProcessMemory(phandle, pdoffset, buffer, LowLevelConstants.GG_STRUCT_SIZE.toULong(), bytesread.ptr)
            var bufbytearray = buffer.pointed.readValues(LowLevelConstants.GG_STRUCT_SIZE).getBytes()
            if(error == 0){
                logWarn("ReadProcessMemory Failed")
                logStep("returning emptySet")
                return emptySet()
            } else if (i == 0 && bufbytearray[0xC].toInt() == 0) {
                logWarn("No lobby update to read, host display name empty")
                logStep("returning emptySet")
                return emptySet()
            } else if (bufbytearray[0xC].toInt() == 0) {
                continue
            }
            var dispname = bufbytearray.stringFromUtf8(0xC, 0x24).trim('\u0000')
            var steamid = 0L
            for(j in 0..7){
                steamid += bufbytearray[7-j].toLong() shl j
            }
            var pd = PlayerUpdate(dispname, steamid)
            pd.characterId = bufbytearray[0x36]
            pd.matchesPlayed = bufbytearray[8]
            pd.matchesWon = bufbytearray[0xA]
            pd.loadingPct = bufbytearray[0x40]
            pDataSet.add(pd)
        }
        logStep("returning pDataSet")
        return pDataSet
    }

    var infoAddr = 0L.toCPointer<ByteVar>()
    var phandle : HANDLE? = nativeHeap.alloc<IntVar>().ptr

}

object LowLevelConstants {
    const val PROC_ALL_ACCESS: UInt = 0x438u
    const val GG_STRUCT_SIZE = 0x48
}