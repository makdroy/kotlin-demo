package mutnemom.android.kotlindemo.fragments

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import mutnemom.android.kotlindemo.databinding.ActivityAboutFragmentBinding
import mutnemom.android.kotlindemo.fragments.backpress.BackPressDispatcherActivity
import mutnemom.android.kotlindemo.fragments.transaction.AnimateFragmentTransactionActivity

class AboutFragmentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAboutFragmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAboutFragmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBackPressDispatcher.setOnClickListener {
            startActivity(Intent(this, BackPressDispatcherActivity::class.java))
        }

        binding.btnAnimateTransaction.setOnClickListener {
            startActivity(Intent(this, AnimateFragmentTransactionActivity::class.java))
        }
    }

}
