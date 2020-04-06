package mutnemom.android.kotlindemo.datetime

import android.os.Bundle
import android.text.format.DateFormat
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_date_time.*
import mutnemom.android.kotlindemo.R
import java.util.*

class DateTimeActivity : AppCompatActivity() {

    private val format = "dd-MM-yyyy"
    private val cal = Calendar.getInstance(Locale.US)
    private val currentTimestamp = cal.timeInMillis

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_date_time)

        init()
        setEvent()
    }

    private fun init() {
        txtFormattingLabel?.text =
            resources.getString(R.string.txt_date_time_destination_format, format)

        txtCurrentTimestamp?.text = currentTimestamp.toString()
        showTextToday()
    }

    private fun setEvent() {
        radioToday?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) showTextToday()
        }
        radioIn5days?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) showTextIn5Days()
        }
        radio2daysAgo?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) showText2DaysAgo()
        }
    }

    private fun showTextToday() {
        cal.timeInMillis = currentTimestamp
        val dateFormat = DateFormat.format(format, cal).toString()
        txtFormatting?.text = dateFormat
    }

    private fun showTextIn5Days() {
        cal.timeInMillis = currentTimestamp
        cal.add(Calendar.DAY_OF_YEAR, 5)
        val dateFormat = DateFormat.format(format, cal).toString()
        txtFormatting?.text = dateFormat
    }

    private fun showText2DaysAgo() {
        cal.timeInMillis = currentTimestamp
        cal.add(Calendar.HOUR_OF_DAY, -48)
        val dateFormat = DateFormat.format(format, cal).toString()
        txtFormatting?.text = dateFormat
    }

}
