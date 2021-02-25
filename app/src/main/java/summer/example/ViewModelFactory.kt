package summer.example

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import summer.example.domain.frameworks.GetAllFrameworkItems
import summer.example.domain.frameworks.GetSpring
import summer.example.domain.frameworks.GetSummer
import summer.example.presentation.AboutViewModel
import summer.example.presentation.BasketViewModel
import summer.example.presentation.FrameworkDetailsViewModel
import summer.example.presentation.FrameworksViewModel
import summer.example.presentation.MainViewModel
import summer.example.ui.MainActivity

class ViewModelFactory : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return when (modelClass) {
            AboutViewModel::class.java -> {
                ServiceLocator.aboutViewModel() as T
            }
            BasketViewModel::class.java -> {
                ServiceLocator.basketViewModel() as T
            }
            FrameworkDetailsViewModel::class.java -> {
                ServiceLocator.frameworkDetailsViewModel() as T
            }
            FrameworksViewModel::class.java -> {
                ServiceLocator.frameworksViewModel() as T
            }
            MainViewModel::class.java -> {
                ServiceLocator.mainViewModel() as T
            }
            else -> throw IllegalArgumentException("modelClass can not be ${modelClass.name}")
        }
    }
}