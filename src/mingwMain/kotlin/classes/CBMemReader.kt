package classes

import kotlinx.cinterop.*
import kwinhelp.*
import platform.windows.*
import classes.session.*
import kotlin.math.abs

class ClashBountyImpl : ClashBountyApi  {

    override fun isXrdRunning() : Boolean {
        val procname = "GuiltyGearXrd.exe"
        var snap = CreateToolhelp32Snapshot(TH32CS_SNAPPROCESS, 0)
        val pe32size: UInt = sizeOf<PROCESSENTRY32>().toUInt()
        val pe32ptr = nativeHeap.alloc<PROCESSENTRY32>()
        val pe32: PROCESSENTRY32 = pe32ptr
        pe32.dwSize = pe32size
        var pid: UInt = 0u
        if (Process32First(snap, pe32.ptr) == 0) {
            println("Process32First failed!")
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
            println("Process32First failed!")
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
        if (pid == 0u) {
            println("$procname not open!")
            return false
        } else {
            println("$procname has an id of $pid, continuing...")
        }
        phandle = OpenProcess(LowLevelConstants.PROC_ALL_ACCESS, 0, pid)
        if (phandle == null) {
            println("$procname failed to open.")
            return false
        } else {
            println("$procname was opened!")
        }
        infoAddr = getDataAddr(pid, "GuiltyGearXrd.exe", phandle)
        if(infoAddr?.toLong() == 0L) {
            println("Failed to find Lobby Data")
            return false
        }
        println(infoAddr.toLong().toString(16))
        println("Found Lobby Data Address!")
        return true
    }

    fun getDataAddr(pid : UInt, modulename : String, pHandle : CPointer<out CPointed>?): CPointer<ByteVar>? {
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
                    return 0L.toCPointer<ByteVar>()
                }
                var newlptr = buffer.value.toLong()
                newlptr = newlptr + 0x44CL
                var newptr = newlptr.toCPointer<ByteVar>()
                return newptr
            }
        }
        return 0L.toCPointer<ByteVar>()
    }

   override fun isXrdConnected(): Boolean {
        return !(infoAddr!!.equals(0L.toCPointer<ByteVar>()))
   }

    override fun getXrdData(): Set<PlayerData> {
        if (!isXrdConnected()){
            return emptySet()
        }
        var pDataSet = HashSet<PlayerData>()
        for(i in 0..8){
            var pdoffset = (infoAddr.toLong() + (LowLevelConstants.GG_STRUCT_SIZE * i).toLong()).toCPointer<ByteVar>()
            var buffer = nativeHeap.allocArray<ByteVar>(LowLevelConstants.GG_STRUCT_SIZE)
            var bytesread = nativeHeap.alloc<ULongVar>()
            var error = ReadProcessMemory(phandle, pdoffset, buffer, LowLevelConstants.GG_STRUCT_SIZE.toULong(), bytesread.ptr)
            var bufbytearray = buffer.pointed.readValues<ByteVar>(LowLevelConstants.GG_STRUCT_SIZE).getBytes()
            if(error == 0){
                println("ReadProcessMemory Failed")
                return emptySet()
            } else if (i == 0 && bufbytearray[0xC].toInt() == 0) {
                println("No lobby data to read, host display name empty")
                return emptySet()
            } else if (bufbytearray[0xC].toInt() == 0) {
                continue
            }
            var dispname = bufbytearray.stringFromUtf8(0xC, 0x24).trim('\u0000')
            var steamid = 0L
            for(j in 0..7){
                steamid += bufbytearray[7-j].toLong() shl j
            }
            var pd = PlayerData(dispname, steamid)
            pd.characterId = bufbytearray[0x36]
            pd.matchesPlayed = bufbytearray[8]
            pd.matchesWon = bufbytearray[0xA]
            pd.loadingPct = bufbytearray[0x40]
            pDataSet.add(pd)
        }
        return pDataSet
    }

    var infoAddr = 0L.toCPointer<ByteVar>()
    var phandle : HANDLE? = nativeHeap.alloc<IntVar>().ptr
}

fun main(args: Array<String>) {
    val cbi = ClashBountyImpl()
    val connected = cbi.connectToXrd()
    if(connected) {
        var pdset = cbi.getXrdData()
        println(pdset.isEmpty())
        for (pd in pdset.iterator()){
            println("Display Name: " + pd.displayName)
            println("Character ID: 0x" + pd.characterId.toString(16))
        }
    }
}

object LowLevelConstants {
    const val PROC_ALL_ACCESS: UInt = 0x438u
    const val GG_STRUCT_SIZE = 0x48
}