package mutnemom.android.kotlindemo.fragments.backpress

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import mutnemom.android.kotlindemo.databinding.FragmentPressBackBinding

private const val KEY_PRESS_BACK_MESSAGE = "param1"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [PressBackFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [PressBackFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PressBackFragment : Fragment() {

    private var _binding: FragmentPressBackBinding? = null
    private val binding: FragmentPressBackBinding
        get() = _binding!!

    private var pressBackMessage: String? = null
    private var listener: OnFragmentInteractionListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
            val callback = object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    Toast.makeText(context, "back", Toast.LENGTH_SHORT).show()
                    requireActivity().finish()
                }
            }

            requireActivity().onBackPressedDispatcher.addCallback(this, callback)
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            pressBackMessage = it.getString(KEY_PRESS_BACK_MESSAGE)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPressBackBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.txtHello.setOnClickListener { onInteract(Uri.parse(pressBackMessage)) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun onInteract(uri: Uri) {
        listener?.onFragmentInteraction(uri)
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @return A new instance of fragment PressBackFragment.
         */
        @JvmStatic
        fun newInstance(param1: String) =
            PressBackFragment().apply {
                arguments = Bundle().apply {
                    putString(KEY_PRESS_BACK_MESSAGE, param1)
                }
            }
    }
}
