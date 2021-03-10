package io.adev.whatHappened

class RecordingHolder<T>(
    val recording: T,
    val happenedEventRecorder: HappenedEventRecorder,
)