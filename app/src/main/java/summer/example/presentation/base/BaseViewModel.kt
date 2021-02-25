package summer.example.presentation.base

import io.adev.whatHappened.InputStep
import summer.example.ServiceLocator
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass

abstract class BaseViewModel<TView>(
    uiContext: CoroutineContext
) : CoroutinesViewModel<TView>(uiContext) {

    init {
        ServiceLocator.stepsRecorder.addStep(InputStep.init(this::class))
    }

    fun attached(clazz: KClass<*>, viewClass: KClass<*>) {
        ServiceLocator.stepsRecorder.addStep(InputStep.attach(clazz, viewClass))
    }

    fun detached(clazz: KClass<*>) {
        ServiceLocator.stepsRecorder.addStep(InputStep.detach(clazz))
    }
}