package classes

import kotlinx.cinterop.*
import kwinhelp.*
import platform.windows.CloseHandle
import platform.windows.OpenProcess

class ClashBountyImpl : ClashBountyApi {

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
        var buf = java.nio.ByteBuffer.allocate(0x30)
        buf.order(java.nio.ByteOrder.LITTLE_ENDIAN)
        buf.putLong(steamUserId)
        buf.putInt(8, 0)
        var nameBytes = displayName.padEnd(0x24, '\u0000').toByteArray()
        buf.position(0xC)
        buf.put(nameBytes)
        scanForAOB(buf.array(), phandle)

    }

    fun scanForAOB(aob: ByteArray, pHandle: CPointer<out CPointed>?): CPointer<out CPointed>? {
        var baseAddr =
    }

    override fun isXrdConnected(): Boolean {
        return true
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getLobbyData(): Set<PlayerData> {
        return emptySet()
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

fun main(args: Array<String>) {
    val cbi = ClashBountyImpl()
    cbi.connectToXrd(1234L, "Labryz")
}

object LowLevelConstants {
    const val PROC_ALL_ACCESS: UInt = 0x438u
    const val MEM_READWRITE: UInt = 4u
    const val MEM_GUARD: UInt = 0x100u
}