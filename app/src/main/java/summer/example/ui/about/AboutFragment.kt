package summer.example.ui.about

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import com.squareup.picasso.Picasso
import summer.example.databinding.AboutFragmentBinding
import summer.example.entity.About
import summer.example.presentation.AboutView
import summer.example.presentation.AboutViewModel
import summer.example.presentation.AboutViewModelRecorder
import summer.example.presentation.recorder
import summer.example.ui.base.BaseFragment

class AboutFragment : BaseFragment(), AboutView {
    private val binding by viewBinding { AboutFragmentBinding.inflate(it) }

    private lateinit var viewModel: AboutViewModelRecorder
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = bindViewModel(AboutViewModel::class, fragment = this) { this }.recorder()
    }

    override var about: About? by didSetNotNull { about ->
        binding.frameworkNameView.text = about.frameworkName
        binding.authorView.text = about.author
        Picasso.get().load(about.logoUrl).into(binding.logoView)
    }

    override var isLoading: Boolean by didSet {
        binding.loadingView.isVisible = isLoading
        binding.contentView.isVisible = !isLoading
    }

    companion object {
        fun newInstance() = AboutFragment()
    }
}