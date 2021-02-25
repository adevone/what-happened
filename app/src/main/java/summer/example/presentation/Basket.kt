package summer.example.presentation

import io.adev.whatHappened.Record
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import summer.example.domain.basket.BasketController
import summer.example.entity.Basket
import summer.example.presentation.base.BaseViewModel
import kotlin.coroutines.CoroutineContext

interface BasketView {
    var items: List<Basket.Item>
}

@Record
class BasketViewModel(
    basketController: BasketController,
    uiContext: CoroutineContext,
) : BaseViewModel<BasketView>(uiContext) {

    override val viewProxy = object : BasketView {
        override var items by state({ it::items }, initial = emptyList())
    }

    init {
        basketController.flow.onEach { basket ->
            viewProxy.items = basket.items
        }.launchIn(scope)
    }
}