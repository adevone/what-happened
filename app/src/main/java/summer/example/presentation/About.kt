package summer.example.presentation

import io.adev.whatHappened.Record
import kotlinx.coroutines.launch
import summer.example.domain.about.GetAbout
import summer.example.entity.About
import summer.example.presentation.base.BaseViewModel
import summer.example.presentation.base.LoadingView
import summer.example.presentation.base.loadingViewProxy
import summer.example.presentation.base.withLoading
import kotlin.coroutines.CoroutineContext

interface AboutView : LoadingView {
    var about: About?
}

@Record
class AboutViewModel(
    private val getAbout: GetAbout,
    uiContext: CoroutineContext,
) : BaseViewModel<AboutView>(uiContext) {

    override val viewProxy: AboutView = object : AboutView,
        LoadingView by loadingViewProxy() {
        override var about by state({ it::about }, initial = null)
    }

    init {
        scope.launch {
            withLoading {
                val about = getAbout()
                viewProxy.about = about
            }
        }
    }
}