

import kotlinx.cinterop.*
import kwinhelp.*
import platform.windows.*

class ClashBountyImpl  {

    fun isXrdRunning() : Boolean {
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
    fun connectToXrd(steamUserId: Long, displayName: String) {
        val procname = "GuiltyGearXrd.exe"
        var snap = CreateToolhelp32Snapshot(TH32CS_SNAPPROCESS, 0)
        val pe32size: UInt = sizeOf<PROCESSENTRY32>().toUInt()
        val pe32ptr = nativeHeap.alloc<PROCESSENTRY32>()
        val pe32 : PROCESSENTRY32 = pe32ptr
        pe32.dwSize = pe32size
        var pid : UInt = 0u
        if (Process32First(snap, pe32.ptr) == 0) {
            println("Process32First failed!")
            return
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
            return
        } else {
            println("$procname has an id of $pid, continuing...")
        }
        var phandle = OpenProcess(LowLevelConstants.PROC_ALL_ACCESS, 0, pid)
        if (phandle == null) {
            println("$procname failed to open.")
            return
        } else {
            println("$procname was opened!")
        }
        var buf = ArrayList<Byte>()
        var mask : Long = 0xFF
        for (i in 0..8) {
            var curr = steamUserId and (mask shl i)
            curr = (curr shr i)
            buf.add(curr.toByte())
        }
        var infoAddr = getDataAddr(pid, "GuiltyGearXrd.exe", phandle)
        if(infoAddr?.toLong() == 0L) {
            println("Failed to find Lobby Data")
            return
        }
        println(infoAddr.toLong().toString(16))
        println("Found Lobby Data Address!")
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

   fun isXrdConnected(): Boolean {
        return true
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun getXrdData(): Set<Int> {
        return emptySet()
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    init{
        var infoAddr = 0L.toCPointer<ByteVar>()
    }
}

fun main(args: Array<String>) {
    val cbi = ClashBountyImpl()
    cbi.connectToXrd(76561198128284333L, "Labryz")
}

object LowLevelConstants {
    const val PROC_ALL_ACCESS: UInt = 0x438u
    const val MEM_READWRITE: UInt = 4u
    const val MEM_GUARD: UInt = 0x100u
    const val GG_STRUCT_SIZE = 0x48
}