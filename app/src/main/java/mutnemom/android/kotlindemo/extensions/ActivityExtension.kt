package mutnemom.android.kotlindemo.extensions

import android.content.pm.PackageManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

fun AppCompatActivity.toast(text: String): Toast {
    val toast = Toast.makeText(this, text, Toast.LENGTH_SHORT)
    toast.show()
    return toast
}

fun AppCompatActivity.requestPermission(p: String, requestCode: Int) {
    if (ActivityCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(this, arrayOf(p), requestCode)
    }
}
