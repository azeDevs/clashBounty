package classes

import classes.cbmodel.ClashBountyApi
import classes.cbmodel.PlayerData

// Temporary stub class until actual ClashBountyImpl is finished
open class ClashBountyImplStub: ClashBountyApi {
    override fun isXrdRunning(): Boolean = false
    override fun connectToXrd(): Boolean = false
    override fun isXrdConnected(): Boolean = false
    override fun getXrdData(): Set<PlayerData> = HashSet()
}


//// Declared here to avoid InvalidMutabilityException in native targets.
//private var loopContext: CoroutineContext? = null
//
//object EngineScope : CoroutineScope {
//    private fun checkContext() {
//        if (loopContext == null) error("Engine context is not present")
//    }
//
//    private val nonNullContext: CoroutineContext
//        get() {
//            checkContext()
//            return loopContext!!
//        }
//
//    private fun mkContext(base: CoroutineContext) =
//        base + SupervisorJob(parent = base[Job]) + CoroutineExceptionHandler { ctx, ex -> printErr("EngineScope Exception Handler", ex) }
//
//    fun runProgram(program: suspend () -> Unit) {
//        loopContext?.let { error("Engine context is already present") }
//
//        runBlocking {
//            loopContext = mkContext(coroutineContext)
//            program()
//        }
//
//        loopContext = null
//    }
//
//    fun performMainLoop(mainLoop: () -> Unit) {
//        checkContext()
//        launch {
//            while (isActive) {
//                yield()
//                mainLoop()
//            }
//        }
//    }
//
//    fun stopMainLoop() {
//        nonNullContext.cancel()
//    }
//
//    override val coroutineContext: CoroutineContext get() = nonNullContext
//}