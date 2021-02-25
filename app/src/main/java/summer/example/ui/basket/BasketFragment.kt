package summer.example.ui.basket

import android.os.Bundle
import android.view.View
import summer.example.databinding.BasketFragmentBinding
import summer.example.entity.Basket
import summer.example.presentation.BasketView
import summer.example.presentation.BasketViewModel
import summer.example.presentation.BasketViewModelRecorder
import summer.example.presentation.recorder
import summer.example.ui.base.BaseFragment

class BasketFragment : BaseFragment(), BasketView {
    private val binding by viewBinding { BasketFragmentBinding.inflate(it) }

    private lateinit var viewModel: BasketViewModelRecorder
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = bindViewModel(BasketViewModel::class, fragment = this) { this }.recorder()
    }

    override var items: List<Basket.Item> by didSet {
        binding.basketView.text = items.joinToString(separator = "\n") { item ->
            "${item.framework.name}=${item.quantity}"
        }
    }

    companion object {
        fun newInstance() = BasketFragment()
    }
}