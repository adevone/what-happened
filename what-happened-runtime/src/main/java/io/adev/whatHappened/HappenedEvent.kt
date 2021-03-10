package io.adev.whatHappened

import kotlin.reflect.KClass

data class HappenedEvent(
    val type: Type,
    val viewModelType: KClass<*>,
    val viewType: KClass<*>? = null,
    val methodName: String = "",
    val arguments: List<Argument<*>> = emptyList(),
) {
    enum class Type {
        Init,
        Attach,
        Detach,
        Interact
    }

    data class Argument<T : Any>(
        val name: String,
        val value: T? = null,
        val clazz: KClass<T>,
        val isHidden: Boolean = false,
    )

    companion object {

        fun init(viewModelType: KClass<*>): HappenedEvent {
            return HappenedEvent(type = Type.Init, viewModelType)
        }

        fun attach(viewModelType: KClass<*>, viewType: KClass<*>): HappenedEvent {
            return HappenedEvent(type = Type.Attach, viewModelType, viewType)
        }

        fun detach(viewModelType: KClass<*>): HappenedEvent {
            return HappenedEvent(type = Type.Detach, viewModelType)
        }

        fun viewInteract(
            viewModelType: KClass<*>,
            methodName: String,
            arguments: List<Argument<*>>,
        ): HappenedEvent {
            return HappenedEvent(type = Type.Interact, viewModelType, viewType = null, methodName, arguments)
        }
    }
}