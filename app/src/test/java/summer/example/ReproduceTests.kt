package summer.example

import io.adev.whatHappened.serializable.decode
import kotlinx.coroutines.Dispatchers
import summer.example.domain.basket.CoroutinesBasketController
import summer.example.domain.frameworks.GetAllFrameworkItems
import summer.example.domain.frameworks.GetSpring
import summer.example.domain.frameworks.GetSummer
import summer.example.generated.reproduce1
import summer.example.presentation.FrameworkDetailsViewModel
import summer.example.presentation.FrameworksViewModel
import summer.example.presentation.MainViewModel
import kotlin.test.Test

class ReproduceTests {

    @Test
    fun case1() {
        val basketController = CoroutinesBasketController()

        reproduce1(
            createMainViewModel = {
                MainViewModel(uiContext = Dispatchers.Unconfined)
            },
            createFrameworksViewModel = {
                FrameworksViewModel(
                    basketController = basketController,
                    getAllFrameworkItems = GetAllFrameworkItems(GetSpring(), GetSummer(), basketController),
                    uiContext = Dispatchers.Unconfined
                )
            },
            createFrameworkDetailsViewModel = {
                FrameworkDetailsViewModel(uiContext = Dispatchers.Unconfined)
            },
            callOnItemClickOfFrameworksViewModel = { viewModel, item ->
                viewModel.onItemClick(
                    password = "123",
                    item = decode(item)
                )
            }
        )
    }
}

