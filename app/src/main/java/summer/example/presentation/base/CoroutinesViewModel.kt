package summer.example.presentation.base

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import summer.arch.ArchViewModel
import kotlin.coroutines.CoroutineContext

abstract class CoroutinesViewModel<TView>(
    uiContext: CoroutineContext,
) : ArchViewModel<TView>() {

    val scope: CoroutineScope = CoroutineScope(uiContext)

    override fun onCleared() {
        super.onCleared()
        scope.cancel()
    }
}