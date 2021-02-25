package summer.example.ui.base

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import io.adev.whatHappened.InputStep
import io.adev.whatHappened.RecordingHolder
import summer.DidSetMixin
import summer.example.ServiceLocator
import summer.example.ViewModelFactory
import summer.example.presentation.base.BaseViewModel
import kotlin.reflect.KClass

abstract class BaseActivity : AppCompatActivity() {

    lateinit var viewClass: KClass<*>
    lateinit var viewModelClass: KClass<*>
    inline fun <reified TView, TViewModel : BaseViewModel<TView>> bindViewModel(
        viewModelClass: KClass<TViewModel>,
        activity: FragmentActivity,
        noinline provideView: () -> TView,
    ): RecordingHolder<TViewModel> {
        this.viewClass = TView::class
        this.viewModelClass = viewModelClass
        val provider = ViewModelProvider(activity, ViewModelFactory())
        val viewModel = provider[viewModelClass.java]
        viewModel.bindView(activity, provideView)
        ServiceLocator.stepsRecorder.addStep(InputStep.attach(viewModelClass, viewClass))
        return RecordingHolder(viewModel, ServiceLocator.stepsRecorder)
    }

    override fun onDestroy() {
        super.onDestroy()
        ServiceLocator.stepsRecorder.addStep(InputStep.detach(viewModelClass))
    }

    companion object : DidSetMixin
}