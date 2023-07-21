package mutnemom.android.kotlindemo

import android.content.res.Configuration
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import mutnemom.android.kotlindemo.databinding.ActivityPerAppLanguageBinding
import mutnemom.android.kotlindemo.extensions.toast

class PerAppLanguageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPerAppLanguageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPerAppLanguageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setEvent()
    }

    @Suppress("DEPRECATION")
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

            // reassign TextView value
            binding.txtAppName.text = resources.getString(R.string.app_name)
            title = resources.getString(R.string.app_name)

        } else {
            toast("-> new config: ${newConfig.locale.language}")
        }
    }

    private fun setEvent() {
        binding.apply {
            switchOverrideSystemLanguage.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    radioEn.isEnabled = true
                    radioTh.isEnabled = true
                } else {
                    radioEn.isEnabled = false
                    radioTh.isEnabled = false

                    radioEn.isChecked = false
                    radioTh.isChecked = false

                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
                }
            }

            radioGroup.setOnCheckedChangeListener { _, id ->
                val localeList = when (id) {
                    R.id.radioEn -> LocaleListCompat.forLanguageTags("en-US")
                    R.id.radioTh -> LocaleListCompat.forLanguageTags("th")
                    else -> LocaleListCompat.getDefault()
                }

                AppCompatDelegate.setApplicationLocales(localeList)
            }
        }
    }

}
