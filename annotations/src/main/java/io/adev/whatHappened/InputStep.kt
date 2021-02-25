package io.adev.whatHappened

import kotlin.reflect.KClass

data class InputStep(
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

        fun init(viewModelType: KClass<*>): InputStep {
            return InputStep(type = Type.Init, viewModelType)
        }

        fun attach(viewModelType: KClass<*>, viewType: KClass<*>): InputStep {
            return InputStep(type = Type.Attach, viewModelType, viewType)
        }

        fun detach(viewModelType: KClass<*>): InputStep {
            return InputStep(type = Type.Detach, viewModelType)
        }

        fun interact(
            viewModelType: KClass<*>,
            methodName: String,
            arguments: List<Argument<*>>,
        ): InputStep {
            return InputStep(type = Type.Interact, viewModelType, viewType = null, methodName, arguments)
        }
    }
}