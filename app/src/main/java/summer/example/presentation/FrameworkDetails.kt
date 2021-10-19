package summer.example.presentation

import io.adev.whatHappened.Record
import summer.example.entity.Framework
import summer.example.entity.FullFramework
import summer.example.entity.toFull
import summer.example.presentation.base.BaseViewModel
import kotlin.coroutines.CoroutineContext

interface FrameworkDetailsView {
    var framework: FullFramework?
    val notifyAboutName: (frameworkName: String) -> Unit
}

@Record
class FrameworkDetailsViewModel(
    uiContext: CoroutineContext,
) : BaseViewModel<FrameworkDetailsView>(uiContext) {

    override val viewProxy = object : FrameworkDetailsView {
        override var framework by state({ it::framework }, initial = null)
        override val notifyAboutName = event { it.notifyAboutName }.perform.onlyWhenAttached()
    }

    fun init(initialFramework: Framework?) {
        viewProxy.framework = initialFramework?.toFull()
    }

    init {
        viewProxy.framework?.let { framework ->
            viewProxy.notifyAboutName(framework.name)
        }
    }
}