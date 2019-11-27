package mutnemom.android.kotlindemo.fragments

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_about_fragment.*
import mutnemom.android.kotlindemo.R
import mutnemom.android.kotlindemo.fragments.backpress.BackPressDispatcherActivity
import mutnemom.android.kotlindemo.fragments.transaction.AnimateFragmentTransactionActivity

class AboutFragmentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_fragment)

        btnBackPressDispatcher?.setOnClickListener {
            startActivity(Intent(this, BackPressDispatcherActivity::class.java))
        }

        btnAnimateTransaction?.setOnClickListener {
            startActivity(Intent(this, AnimateFragmentTransactionActivity::class.java))
        }
    }

}
