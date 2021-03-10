package io.adev.whatHappened.testGen

import io.adev.whatHappened.serializable.SerializableInputStep
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.io.File

// For example: summer.example.generated reproduce1 record.json ./app/src/test/java
fun main(args: Array<String>) {
    val pkg = args[0]
    val caseName = args[1]
    val stepsJson = File(args[2]).readText()
    val outDir = File(args[3])
    val steps = Json.decodeFromString(ListSerializer(SerializableInputStep.serializer()), stepsJson)
    val kotlinCode = generateKotlinCode(pkg, caseName, steps)
    val outFile = File(outDir, "${pkg.replace(".", "/")}/$caseName.kt")
    if (!outFile.exists()) {
        outFile.createNewFile()
    }
    outFile.writeText(kotlinCode)
}