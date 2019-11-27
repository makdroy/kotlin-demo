package mutnemom.android.kotlindemo.fragments.transaction

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_standby.*

import mutnemom.android.kotlindemo.R

class StandbyFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance() =
            StandbyFragment()
    }

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
    ): View? {
        return inflater.inflate(R.layout.fragment_standby, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        txtTapHint?.setOnClickListener { listener.onHintTapped() }
    }

    interface OnHintTappedListener {
        fun onHintTapped()
    }

}
