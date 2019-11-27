package mutnemom.android.kotlindemo.fragments.transaction

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import mutnemom.android.kotlindemo.R

class IncomingFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance() =
            IncomingFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_incoming, container, false)
    }

}
