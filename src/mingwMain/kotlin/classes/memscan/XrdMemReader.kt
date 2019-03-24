package classes.memscan

import classes.Host.DISPLAY_NAME
import classes.Host.STEAM_ID
import classes.memscan.ScanLogs.FIND_AOBS_FAILED
import classes.memscan.ScanLogs.FIND_PROC_FAILED
import classes.memscan.ScanLogs.FOUND_ADDRESS
import classes.memscan.ScanLogs.FOUND_PROCESS
import classes.output.logBool
import classes.output.logInfo
import classes.session.ClashBountyApi
import classes.session.PlayerData
import kotlinx.cinterop.*
import kwinhelp.CreateToolhelp32Snapshot
import kwinhelp.Process32First
import kwinhelp.Process32Next
import kwinhelp.TH32CS_SNAPPROCESS
import platform.windows.CloseHandle
import platform.windows.HANDLE
import platform.windows.OpenProcess


class ClashBountyImpl() : ClashBountyApi {
    var isRunning = false
    var isConnected = false
    var processHandle: HANDLE? = null

    override fun isXrdRunning(): Boolean {
        val processId = getProcessId(getProcessName())
        if (processId == 0u) return logBool(FIND_PROC_FAILED)
        else logInfo("${getProcessName()} has an id of ${processId}, continuing...")

        processHandle = OpenProcess(LowLevelConstants.PROC_ALL_ACCESS, 0, processId)
        if (processHandle == null) return logBool(FIND_PROC_FAILED)
        else isRunning = logBool(FOUND_PROCESS)
        return isRunning && (processHandle != null)
    }

    override fun connectToXrd(): Boolean {
        if (isXrdRunning()) {
            var buf = ArrayList<Byte>()
            var mask: Long = 0xFF
            for (i in 0..8) {
                var curr: Long = STEAM_ID and (mask shl i)
                buf.add((curr shr i).toByte())
            }
            var nameChars:CharArray = DISPLAY_NAME.padEnd(0x24, '\u0000').toCharArray()
            nameChars.forEach { char -> buf.add(char.toByte()) }

            var infoAddr: CPointer<out CPointed>? = getAOB(buf.toByteArray(), processHandle)
            if (infoAddr!!.equals(nativeNullPtr)) return logBool(FIND_AOBS_FAILED)
            else isConnected = logBool(FOUND_ADDRESS)
        } else return logBool(FIND_PROC_FAILED)
        return isConnected
    }

    override fun isXrdConnected(): Boolean = isConnected

    override fun getXrdData(): Set<PlayerData> {
        // TODO: C# Code to be converted
        /*
        Action updateAction = () => {
            while (true) {
                byte[] structstorage = ms.Read<byte>(structbaseaddr, (int)(StructSize * 8), false);

                var playercounter = 0;

                foreach (GGLobbyInfo ggInfo in playerdatas) {
                    int currStructIdx = (int)(playercounter * StructSize);
                    ggInfo.steamId = BitConverter.ToUInt64(structstorage, currStructIdx);
                    ggInfo.matches = structstorage[currStructIdx + 8];
                    ggInfo.wins = structstorage[currStructIdx + 0xA];

                    if (structstorage[currStructIdx + 0xC] == 0) {
                        ggInfo.displayName = "";
                    } else {
                        List<byte> dNameBytes = new List<byte>();
                        var strcounter = currStructIdx + 0xC;

                        while (structstorage[strcounter] != 0) {
                            dNameBytes.Add(structstorage[strcounter]);
                            strcounter++;
                        }

                        ggInfo.displayName = Encoding.ASCII.GetString(dNameBytes.ToArray());
                    }

                    ggInfo.charId = structstorage[currStructIdx + 0x36];
                    playercounter++;
                }

                string jtext = JsonConvert.SerializeObject(playerdatas);
                File.WriteAllText("lobbyinfo.json", jtext);
                Thread.Sleep(2500);
            }
        };
        Task loopTask = new Task(updateAction);
        loopTask.Start();
        Console.WriteLine("Started Writing");
        loopTask.Wait();
        */
        return HashSet()
    }

    private fun getProcessId(processName:String): UInt {
        var pid: UInt = 0u; val snapshot = CreateToolhelp32Snapshot(TH32CS_SNAPPROCESS, 0)
        getProcessEntry32().dwSize = getProcessEntry32size()
        if (Process32First(snapshot, getProcessEntry32().ptr) == 0) logInfo("Process32First failed")
        else while (Process32Next(snapshot, getProcessEntry32().ptr) != 0)
            if (processName.equals(getProcessEntry32().szExeFile.toKString())) {
                pid = getProcessEntry32().th32ProcessID.toUInt(); break }
        CloseHandle(snapshot)
        return pid
    }

}

// Temporary stub class until actual ClashBountyImpl is finished
open class ClashBountyImplStub: ClashBountyApi {
    override fun isXrdRunning(): Boolean = false
    override fun connectToXrd(): Boolean = false
    override fun isXrdConnected(): Boolean = false
    override fun getXrdData(): Set<PlayerData> = HashSet()
}