package summer.example.presentation.base

interface NavigationView {
    val navigate: (navigation: (AppNavigator) -> Unit) -> Unit
}

fun <TView : NavigationView> BaseViewModel<TView>.navigationViewProxy() =
    object : NavigationView {
        override val navigate = event { it.navigate }.perform.exactlyOnce()
    }