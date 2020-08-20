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
import kotlinx.android.synthetic.main.fragment_press_back.*
import mutnemom.android.kotlindemo.R

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

    private var pressBackMessage: String? = null
    private var listener: OnFragmentInteractionListener? = null
    private var toast: Toast? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
            val callback = object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
//                    when {
//                        toast != null && toast!!.view.isShown -> requireActivity().finish()
//                        else -> {
//                            toast = Toast
//                                .makeText(context, pressBackMessage, Toast.LENGTH_SHORT)
//                                .apply { show() }
//                        }
//                    }
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
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_press_back, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        txtHello?.setOnClickListener { onInteract(Uri.parse(pressBackMessage)) }
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
