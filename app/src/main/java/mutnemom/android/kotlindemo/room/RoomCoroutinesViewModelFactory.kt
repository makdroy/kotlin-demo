package mutnemom.android.kotlindemo.room

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class RoomCoroutinesViewModelFactory(private val application: Application) :
    ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RoomCoroutinesViewModel::class.java)) {
            return RoomCoroutinesViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}
