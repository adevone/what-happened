package summer.example.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import io.adev.whatHappened.HappenedEvent
import io.adev.whatHappened.RecordingHolder
import summer.DidSetMixin
import summer.example.ServiceLocator
import summer.example.ViewModelFactory
import summer.example.presentation.base.AppNavigator
import summer.example.presentation.base.BaseViewModel
import summer.example.presentation.base.NavigationView
import summer.example.ui.MainActivity
import kotlin.reflect.KClass

abstract class BaseFragment : Fragment(), NavigationView {

    private var viewBindingDelegate: ViewBindingDelegate<*>? = null
    fun <TBinding : ViewBinding> viewBinding(
        createBinding: (LayoutInflater) -> TBinding,
    ): ViewBindingDelegate<TBinding> {
        return ViewBindingDelegate(createBinding).also {
            viewBindingDelegate = it
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val delegate = viewBindingDelegate ?: throw ViewBindingNotProvidedException()
        return delegate.inflateBinding(inflater).root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewBindingDelegate?.clearBinding()
        ServiceLocator.happenedEventRecorder.append(HappenedEvent.detach(viewModelClass))
    }

    lateinit var viewClass: KClass<*>
    lateinit var viewModelClass: KClass<*>
    inline fun <reified TView, TViewModel : BaseViewModel<TView>> bindViewModel(
        viewModelClass: KClass<TViewModel>,
        fragment: Fragment,
        noinline provideView: () -> TView,
    ): RecordingHolder<TViewModel> {
        val provider = ViewModelProvider(fragment, ViewModelFactory())
        val viewModel = provider[viewModelClass.java]
        viewModel.bindView(fragment.viewLifecycleOwner, provideView)
        this.viewClass = TView::class
        this.viewModelClass = viewModelClass
        ServiceLocator.happenedEventRecorder.append(HappenedEvent.attach(viewModelClass, viewClass))
        return RecordingHolder(viewModel, ServiceLocator.happenedEventRecorder)
    }

    override val navigate: (navigation: (AppNavigator) -> Unit) -> Unit = { navigate ->
        val navigator = (requireActivity() as MainActivity).navigator
        navigate(navigator)
    }

    companion object : DidSetMixin
}

class ViewBindingNotProvidedException : IllegalStateException(
    "Please view binding: val binding by viewBinding { <BINDING>.inflate(it) }"
)