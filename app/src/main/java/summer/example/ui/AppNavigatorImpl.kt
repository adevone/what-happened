package summer.example.ui

import androidx.fragment.app.FragmentManager
import summer.example.entity.Framework
import summer.example.presentation.base.AppNavigator
import summer.example.ui.frameworks.FrameworkDetailsFragment

interface NavigationHost {
    val containerId: Int
    val fragmentManager: FragmentManager
}

class AppNavigatorImpl(
    private val host: NavigationHost,
) : AppNavigator {

    override fun toFrameworkDetails(framework: Framework) {
        host.fragmentManager.beginTransaction()
            .replace(host.containerId, FrameworkDetailsFragment.newInstance(framework))
            .addToBackStack(FrameworkDetailsFragment::class.simpleName)
            .commit()
    }
}