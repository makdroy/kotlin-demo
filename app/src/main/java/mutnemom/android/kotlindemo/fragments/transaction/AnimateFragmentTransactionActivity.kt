package mutnemom.android.kotlindemo.fragments.transaction

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import mutnemom.android.kotlindemo.R

class AnimateFragmentTransactionActivity :
    AppCompatActivity(),
    StandbyFragment.OnHintTappedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_animate_fragment_transaction)

        supportFragmentManager
            .beginTransaction()
            .add(R.id.container,
                StandbyFragment.newInstance(), "")
            .commit()
    }

    override fun onHintTapped() {
        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(
                R.anim.enter_from_right,
                0,
                0,
                R.anim.exit_to_right
            )
            .addToBackStack("")
            .replace(R.id.container,
                IncomingFragment.newInstance(), "")
            .commit()
    }

}
