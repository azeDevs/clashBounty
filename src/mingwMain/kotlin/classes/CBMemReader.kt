package classes

import kotlinx.cinterop.*
import kwinhelp.*
import kotlin.collections.ArrayList
import platform.windows.CloseHandle
import platform.windows.OpenProcess
import platform.windows.ReadProcessMemory
import platform.windows.VirtualQueryEx
import platform.windows.MEMORY_BASIC_INFORMATION

class ClashBountyImpl : ClashBountyApi {

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
        return pid != 0u
    }
    override fun connectToXrd(steamUserId: Long, displayName: String) {
        val procname = "GuiltyGearXrd.exe"
        var snap = CreateToolhelp32Snapshot(TH32CS_SNAPPROCESS, 0)
        val pe32size: UInt = sizeOf<PROCESSENTRY32>().toUInt()
        val pe32ptr = nativeHeap.alloc<PROCESSENTRY32>()
        val pe32: PROCESSENTRY32 = pe32ptr
        pe32.dwSize = pe32size
        var pid: UInt = 0u
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
        for (var i = 0; i < 8; i++){
            var curr = steamUserId and (mask shl i)
            curr = curr shr i
            buf.add(curr)
        }
        var nameBytes = displayName.padEnd(0x24, '\u0000').toByteArray()
        buf.addAll(nameBytes)
        infoAddr = scanForAOB(buf.toArray(), phandle)
        if(infoAddr.equals(nativeNullPtr)){
            println("Failed to find AOB")
            return
        }
        println("Found Lobby Data Address!")
    }

    fun scanForAOB(aob: ByteArray, pHandle: CPointer<out CPointed>?): CPointer<out CPointed>? {
        var currentAddr = 0L.toCPointer<IntVar>()
        var bytesread = nativeHeap.alloc<ULongVar>()
        var buffer = nativeHeap.allocArray<ByteVar>(0x30)
        var meminfobuffer = nativeHeap.alloc<PMEMORY_BASIC_INFORMATION>()
        var inforeturned = VirtualQueryEx(pHandle, currentAddr, meminfobuffer, sizeOf<MEMORY_BASIC_INFORMATION>())
        while(inforeturned != 0uL){
            currentAddr = meminfobuffer.BaseAddress.toLong() + meminfobuffer.RegionSize).toCPointer()
            if(meminfobuffer.Protect and LowLevelConstants.MEM_READWRITE == LowLevelConstants.MEM_READWRITE && meminfobuffer.Protect and LowLevelConstants.MEM_GUARD != LowLevelConstants.MEM_GUARD) {
                for (val p = meminfobuffer.BaseAddress.reinterpret(); p.toLong() < (meminfobuffer.BaseAddress.toLong() + meminfobuffer.RegionSize); p = (p.toLong() + 4).toCPointer()){
                    var error = ReadProcessMemory(pHandle, p, buffer, 0x30uL, bytesread)
                    if(error == 0){
                        return null
                    }
                    if(bytesread.pointed == 0x30) {
                        var equal = true
                        for (var i = 0; i < 0x30; i++){
                            if (aob[i] != buffer[i]) {
                                equal = false
                                break
                            }
                        }
                        if (equal) {
                            println("Result found!")
                            return p
                        }
                    }
                }

            }
            inforeturned = VirtualQueryEx(pHandle, currentAddr, meminfobuffer, sizeOf<MEMORY_BASIC_INFORMATION>().toULong())
        }
        return null
    }

    override fun isXrdConnected(): Boolean {
        return true
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getXrdData(): Set<PlayerData> {
        return emptySet()
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    init{
        var infoAddr = nativeNullPtr
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