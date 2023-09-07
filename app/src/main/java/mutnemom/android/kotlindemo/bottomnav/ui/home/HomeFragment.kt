package mutnemom.android.kotlindemo.bottomnav.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import mutnemom.android.kotlindemo.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding: FragmentHomeBinding
        get() = _binding!!

    private lateinit var homeViewModel: HomeViewModel

    private var clickCounter = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewModel()
        setEvent()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setEvent() {
        binding.apply {
            btnClick.setOnClickListener { increaseClickCounter() }
        }
    }

    private fun setupViewModel() {
        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        homeViewModel.apply {
            text.observe(viewLifecycleOwner) {
                binding.textHome.text = it
            }
        }
    }

    private fun increaseClickCounter() {
        ++clickCounter

        val builder = StringBuilder()
            .append("clicked:")
            .append(" ")
            .append(clickCounter)

        binding.txtClickCounter.text = builder.toString()
    }

}
