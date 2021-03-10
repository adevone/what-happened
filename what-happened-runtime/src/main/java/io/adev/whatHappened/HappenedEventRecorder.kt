package io.adev.whatHappened

interface HappenedEventRecorder {
    fun append(step: HappenedEvent)
}