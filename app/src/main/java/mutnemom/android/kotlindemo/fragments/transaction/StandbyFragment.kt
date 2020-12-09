package mutnemom.android.kotlindemo.fragments.transaction

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import mutnemom.android.kotlindemo.databinding.FragmentStandbyBinding

class StandbyFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance() =
            StandbyFragment()
    }

    private var _binding: FragmentStandbyBinding? = null
    private val binding: FragmentStandbyBinding
        get() = _binding!!

    private lateinit var listener: OnHintTappedListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        when (context) {
            is OnHintTappedListener -> listener = context
            else -> throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStandbyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.txtTapHint.setOnClickListener { listener.onHintTapped() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    interface OnHintTappedListener {
        fun onHintTapped()
    }

}
