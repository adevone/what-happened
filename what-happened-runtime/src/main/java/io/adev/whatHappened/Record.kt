package io.adev.whatHappened

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Record(val interfaces: Array<KClass<*>> = [])