package io.adev.whatHappened.serializable

import io.adev.whatHappened.HappenedEvent
import io.adev.whatHappened.HappenedEventRecorder
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

class SerializationHappenedEventRecorder(
    private val json: Json,
) : HappenedEventRecorder {

    private val steps = mutableListOf<HappenedEvent>()

    override fun append(step: HappenedEvent) {
        steps.add(step)
    }

    fun dump(): String {
        val serializableSteps = steps.map { it.toSerializable(json) }
        return json.encodeToString(
            ListSerializer(SerializableInputStep.serializer()),
            serializableSteps
        )
    }
}