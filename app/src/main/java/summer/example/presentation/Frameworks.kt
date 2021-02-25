package summer.example.presentation

import io.adev.whatHappened.Hide
import io.adev.whatHappened.Record
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import summer.example.domain.basket.BasketController
import summer.example.domain.frameworks.GetAllFrameworkItems
import summer.example.entity.Basket
import summer.example.presentation.base.BaseViewModel
import summer.example.presentation.base.NavigationView
import summer.example.presentation.base.navigationViewProxy
import kotlin.coroutines.CoroutineContext

interface FrameworksView : NavigationView {
    var items: List<Basket.Item>
}

@Record
class FrameworksViewModel(
    private val basketController: BasketController,
    private val getAllFrameworkItems: GetAllFrameworkItems,
    uiContext: CoroutineContext,
) : BaseViewModel<FrameworksView>(uiContext) {

    override val viewProxy: FrameworksView = object : FrameworksView,
        NavigationView by navigationViewProxy() {
        override var items by state({ it::items }, initial = emptyList())
    }

    init {
        basketController.flow.onEach {
            updateFrameworks()
        }.launchIn(scope)
    }

    init {
        updateFrameworks()
    }

    fun onItemClick(@Hide password: String, item: Basket.Item) {
        viewProxy.navigate { it.toFrameworkDetails(item.framework) }
    }

    fun onIncreaseClick(item: Basket.Item) {
        basketController.increase(item.framework)
    }

    fun onDecreaseClick(item: Basket.Item) {
        basketController.decrease(item.framework)
    }

    private fun updateFrameworks() {
        scope.launch {
            val frameworks = getAllFrameworkItems(springVersion = "5.0")
            viewProxy.items = frameworks
        }
    }

    fun onCrashClick() {
        throw IllegalStateException("app is crashed")
    }
}