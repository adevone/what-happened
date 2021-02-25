package summer.example.recording

import io.adev.whatHappened.InputStep
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.serializer
import kotlin.reflect.full.createType

@Serializable
data class SerializableInputStep(
    val type: Type,
    val viewModelType: String,
    val viewType: String = "",
    val methodName: String = "",
    val arguments: List<Argument> = emptyList(),
) {
    enum class Type {
        Init,
        Attach,
        Detach,
        Interact
    }

    @Serializable
    data class Argument(
        val name: String,
        val value: JsonElement? = null,
        val isHidden: Boolean = false,
    )
}

fun InputStep.toSerializable(json: Json): SerializableInputStep {
    return SerializableInputStep(
        type = this.type.toSerializable(),
        viewModelType = this.viewModelType.qualifiedName!!,
        viewType = this.viewType?.qualifiedName ?: "",
        methodName = this.methodName,
        arguments = this.arguments.map { step ->
            step.toSerializable(json)
        },
    )
}

private fun InputStep.Type.toSerializable(): SerializableInputStep.Type {
    return when (this) {
        InputStep.Type.Init -> SerializableInputStep.Type.Init
        InputStep.Type.Attach -> SerializableInputStep.Type.Attach
        InputStep.Type.Detach -> SerializableInputStep.Type.Detach
        InputStep.Type.Interact -> SerializableInputStep.Type.Interact
    }
}

private fun InputStep.Argument<*>.toSerializable(json: Json): SerializableInputStep.Argument {
    val serializer = serializer(this.clazz.createType())
    return SerializableInputStep.Argument(
        name = this.name,
        value = json.encodeToJsonElement(serializer, value),
        isHidden = this.isHidden,
    )
}

inline fun <reified T> decode(json: String): T {
    val argSerializer = serializer(T::class.createType()) as KSerializer<T>
    return Json.decodeFromString(argSerializer, json)
}