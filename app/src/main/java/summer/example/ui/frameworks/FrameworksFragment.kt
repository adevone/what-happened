package summer.example.ui.frameworks

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.SimpleItemAnimator
import summer.example.databinding.FrameworksFragmentBinding
import summer.example.entity.Basket
import summer.example.presentation.FrameworksView
import summer.example.presentation.FrameworksViewModel
import summer.example.presentation.FrameworksViewModelRecorder
import summer.example.presentation.recorder
import summer.example.ui.base.BaseFragment

class FrameworksFragment : BaseFragment(), FrameworksView {

    private val binding by viewBinding { FrameworksFragmentBinding.inflate(it) }

    private lateinit var viewModel: FrameworksViewModelRecorder

    private lateinit var frameworksAdapter: FrameworksAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = bindViewModel(FrameworksViewModel::class, fragment = this) { this }.recorder()

        frameworksAdapter = FrameworksAdapter(viewModel)
        binding.frameworksView.adapter = frameworksAdapter
        (binding.frameworksView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        binding.crashButton.setOnClickListener {
            viewModel.onCrashClick()
        }
    }

    override var items: List<Basket.Item> by didSet {
        frameworksAdapter.submitList(items)
    }

    companion object {
        fun newInstance() = FrameworksFragment()
    }
}