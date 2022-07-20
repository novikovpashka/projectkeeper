package com.novikovpashka.projectkeeper.presentation.addprojectactivity

import android.os.Parcelable
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.novikovpashka.projectkeeper.data.model.Incoming
import com.novikovpashka.projectkeeper.data.model.Project
import com.novikovpashka.projectkeeper.data.repository.SettingsRepository
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class AddProjectViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    private val incomings = mutableListOf<ItemIncoming>()
    private val _incomingsLiveData = MutableLiveData<List<ItemIncoming>>()

    val incomingsLiveData: LiveData<List<ItemIncoming>>
        get() = _incomingsLiveData

    private val _snackbar = MutableLiveData<String?>()
    val snackbar: LiveData<String?>
        get() = _snackbar

    init {
        _incomingsLiveData.value = incomings
    }

    fun addIncoming() {
        incomings.add(ItemIncoming())
        _incomingsLiveData.value = incomings
    }

    fun deleteIncoming(index: Int) {
        incomings.removeAt(index)
        _incomingsLiveData.value = incomings
    }

    fun saveIncomingValue (value: String, index: Int) {
        incomings[index].incomingValue = value
    }

    fun saveIncomingDescription (value: String, index: Int) {
        incomings[index].incomingDescription = value
    }

    fun saveIncomingDateText(value: String, index: Int) {
        incomings[index].incomingDateText = value
    }

    fun loadThemeIdFromStorage(): Int {
        return settingsRepository.loadThemeIdFromStorage()
    }

    fun parseProject(name: String, price: String, description: String): Project? {
        val incomingList = mutableListOf<Incoming>()

        if (name.isEmpty()) {
            _snackbar.value = "Project name can not be empty"
            Log.v("mytag", "empty name")
            return null
        }
        if (price.isEmpty()) {
            _snackbar.value = "Project price can not be empty"
            Log.v("mytag", "empty price")
            return null
        }
        for (itemIncoming in incomings) {
            try {
                val incomingValue: Double = itemIncoming.incomingValue.toDouble()
                val incoming = Incoming(
                    incomingValue = incomingValue,
                    incomingDescription = itemIncoming.incomingDescription,
                    incomingDate = itemIncoming.incomingDate
                )
                incomingList.add(incoming)
            }
            catch (e: NumberFormatException) {
                _snackbar.value = "Incoming value can not be empty"
                return null
            }
        }
        return Project (
            name = name,
            price = price.toDouble(),
            description = description,
            incomings = incomingList
        )
    }

    @Parcelize
    data class ItemIncoming (
        var incomingDescription: String = "",
        var incomingValue: String = "",
        var incomingDate: Long = Date().time,
        var dateStamp: Long = Date().time) : Parcelable {
        var incomingDateText: String = ""
        init {
            val simpleDateFormat = SimpleDateFormat("MMM d, yyyy", Locale.ENGLISH)
            incomingDateText = simpleDateFormat.format(incomingDate).toString()
        }
    }

    class Factory @Inject constructor(
        private val settingsRepository: SettingsRepository
    ): ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AddProjectViewModel::class.java)) {
                return AddProjectViewModel(
                    settingsRepository
                ) as T
            }
            else throw IllegalStateException("Unknown ViewModel class")
        }
    }
}