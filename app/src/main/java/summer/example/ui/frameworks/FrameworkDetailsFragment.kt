package summer.example.ui.frameworks

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.os.bundleOf
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import summer.example.databinding.FrameworkDetailsFragmentBinding
import summer.example.entity.Framework
import summer.example.entity.FullFramework
import summer.example.presentation.FrameworkDetailsView
import summer.example.presentation.FrameworkDetailsViewModel
import summer.example.presentation.FrameworkDetailsViewModelRecorder
import summer.example.presentation.recorder
import summer.example.ui.base.BaseFragment

class FrameworkDetailsFragment : BaseFragment(), FrameworkDetailsView {

    private val binding by viewBinding { FrameworkDetailsFragmentBinding.inflate(it) }

    private lateinit var viewModel: FrameworkDetailsViewModelRecorder
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = bindViewModel(FrameworkDetailsViewModel::class, fragment = this) { this }.recorder()

        val frameworkString = arguments?.getString(FRAMEWORK_KEY) ?: return
        val framework = Json.decodeFromString(Framework.serializer(), frameworkString)
        viewModel.init(initialFramework = framework)
        viewModel.init(initialFramework = null)
    }

    override var framework: FullFramework? by didSet {
        binding.nameView.text = framework?.name ?: ""
        binding.versionView.text = framework?.version ?: ""
    }

    override var notifyAboutName = { frameworkName: String ->
        Toast.makeText(context, frameworkName, Toast.LENGTH_LONG).show()
    }

    companion object {
        private const val FRAMEWORK_KEY = "framework_key"

        fun newInstance(framework: Framework) = FrameworkDetailsFragment().also {
            it.arguments = bundleOf(
                FRAMEWORK_KEY to Json.encodeToString(framework)
            )
        }
    }
}