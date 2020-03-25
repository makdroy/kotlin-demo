package mutnemom.android.kotlindemo.fragments.backpress

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import mutnemom.android.kotlindemo.R

class BackPressDispatcherActivity :
    AppCompatActivity(),
    PressBackFragment.OnFragmentInteractionListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_back_press_dispatcher)

        supportFragmentManager
            .beginTransaction()
            .add(R.id.container, PressBackFragment.newInstance("mutnemom.android.kotlindemo"))
            .commit()
    }

    override fun onFragmentInteraction(uri: Uri) {
        Log.e("tag", "-> from fragment: ${uri.path}")
    }
}
