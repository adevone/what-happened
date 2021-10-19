package summer.example.presentation.base

import io.adev.whatHappened.HappenedEvent
import io.adev.whatHappened.Record
import summer.example.ServiceLocator
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass

@Record
abstract class BaseViewModel<TView>(
    uiContext: CoroutineContext
) : CoroutinesViewModel<TView>(uiContext) {

    init {
        ServiceLocator.happenedEventRecorder.append(HappenedEvent.init(this::class))
    }

    fun attached(clazz: KClass<*>, viewClass: KClass<*>) {
        ServiceLocator.happenedEventRecorder.append(HappenedEvent.attach(clazz, viewClass))
    }

    fun detached(clazz: KClass<*>) {
        ServiceLocator.happenedEventRecorder.append(HappenedEvent.detach(clazz))
    }
}