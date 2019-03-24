package classes

import classes.memscan.ClashBountyImplStub
import classes.output.displayDialog
import classes.output.writeOut
import classes.session.ClashBountyApi
import classes.session.ClashBountySession


object Host {
    const val DISPLAY_NAME: String = "Labryz"
    const val STEAM_ID: Long = 76561198128284333L
}

fun getCBImpl(): ClashBountyApi = ClashBountyImplStub()

fun main() {
    writeOut("Some text to be written...\nBOUNTY: $100")
    displayDialog("Title", "Lorem Ipsum Dolor Sit Amet")
    ClashBountySession().start()
}

// TODO: Notes on Kotlin/Native timed coroutine implementation
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