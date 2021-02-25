package summer.example.ui

import android.os.Bundle
import android.view.Menu
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.fragment.app.FragmentManager
import summer.example.R
import summer.example.databinding.ActivityMainBinding
import summer.example.entity.Tab
import summer.example.presentation.MainView
import summer.example.presentation.MainViewModel
import summer.example.presentation.MainViewModelRecorder
import summer.example.presentation.base.AppNavigator
import summer.example.presentation.recorder
import summer.example.ui.about.AboutFragment
import summer.example.ui.base.BaseActivity
import summer.example.ui.basket.BasketFragment
import summer.example.ui.frameworks.FrameworksFragment

class MainActivity : BaseActivity(), MainView, NavigationHost {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModelRecorder
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = bindViewModel(MainViewModel::class, activity = this) { this }.recorder()
    }

    override var tabs: List<Tab> by didSet {
        binding.bottomNavigationView.menu.clear()
        tabs.forEach { tab ->
            binding.bottomNavigationView.menu
                .add(
                    Menu.NONE,
                    tab.itemId,
                    Menu.NONE,
                    tab.title
                )
                .setIcon(tab.iconRes)
                .setOnMenuItemClickListener {
                    viewModel.onMenuItemClick(tab)
                    false
                }
        }
    }

    override var selectedTab: Tab? = null
        set(selectedTab) {
            val previousTab = field
            field = selectedTab
            if (selectedTab != previousTab && selectedTab != null) {
                val fragment = when (selectedTab) {
                    Tab.Frameworks -> FrameworksFragment.newInstance()
                    Tab.About -> AboutFragment.newInstance()
                    Tab.Basket -> BasketFragment.newInstance()
                }
                supportFragmentManager.beginTransaction()
                    .replace(R.id.contentContainer, fragment)
                    .addToBackStack(selectedTab.name)
                    .commit()
                binding.bottomNavigationView.selectedItemId = selectedTab.itemId
            }
        }

    private val Tab.itemId: Int
        @IdRes get() = when (this) {
            Tab.Frameworks -> R.id.frameworksItem
            Tab.About -> R.id.aboutItem
            Tab.Basket -> R.id.basketItem
        }

    private val Tab.iconRes: Int
        @DrawableRes get() = when (this) {
            Tab.Frameworks -> R.drawable.menu_frameworks
            Tab.About -> R.drawable.menu_about
            Tab.Basket -> R.drawable.menu_basket
        }

    private val Tab.title: String
        get() = when (this) {
            Tab.Frameworks -> getString(R.string.menu_frameworks)
            Tab.About -> getString(R.string.menu_about)
            Tab.Basket -> getString(R.string.menu_basket)
        }

    override val containerId: Int = R.id.contentContainer
    override val fragmentManager: FragmentManager get() = supportFragmentManager

    val navigator: AppNavigator = AppNavigatorImpl(host = this)

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 1) {
            supportFragmentManager.popBackStack()
        } else {
            finish()
        }
    }
}
