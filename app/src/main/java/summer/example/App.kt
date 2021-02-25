package summer.example

import android.app.Application
import android.util.Log
import java.io.File
import kotlin.system.exitProcess

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        val crashLogFile = File(filesDir, "today.log")
        if (crashLogFile.exists()) {
            val crashText = crashLogFile.readText()
            Log.e("ERROR", crashText)
            crashLogFile.delete()
        }

        Thread.currentThread().setUncaughtExceptionHandler { _, _ ->
            val stepsJson = ServiceLocator.stepsRecorder.dump()
            if (!crashLogFile.exists()) {
                crashLogFile.createNewFile()
            }
            crashLogFile.writeText(stepsJson)
            Log.e("ERROR", stepsJson)
            exitProcess(1)
        }
    }
}