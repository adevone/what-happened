package summer.example.recording

import io.adev.whatHappened.InputStep
import io.adev.whatHappened.StepsRecorder
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

class SerializationStepsRecorder(
    private val json: Json,
) : StepsRecorder {

    private val steps = mutableListOf<InputStep>()

    override fun addStep(step: InputStep) {
        steps.add(step)
    }

    fun dump(): String {
        val serializableSteps = steps.map { it.toSerializable(Json) }
        return json.encodeToString(
            ListSerializer(SerializableInputStep.serializer()),
            serializableSteps
        )
    }
}