package summer.example.presentation

import io.adev.whatHappened.Record
import summer.example.entity.Tab
import summer.example.presentation.base.BaseViewModel
import kotlin.coroutines.CoroutineContext

interface MainView {
    var tabs: List<Tab>
    var selectedTab: Tab?
}

@Record
class MainViewModel(
    uiContext: CoroutineContext,
) : BaseViewModel<MainView>(uiContext) {

    private val allTabs = Tab.values().toList()
    override val viewProxy = object : MainView {
        override var tabs by state({ it::tabs }, initial = allTabs)
        override var selectedTab by state({ it::selectedTab }, initial = allTabs.first())
    }

    fun onMenuItemClick(tab: Tab) {
        viewProxy.selectedTab = tab
    }
}