package com.novikovpashka.projectkeeper.presentation.editprojectactivity

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.novikovpashka.projectkeeper.data.model.Incoming
import com.novikovpashka.projectkeeper.data.model.Project
import com.novikovpashka.projectkeeper.data.UserSettings
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class EditProjectViewModel @Inject constructor(private val userSettings: UserSettings) : ViewModel() {
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

    fun getAccentColor(): Int {
        return userSettings.loadAccentColorFromStorage()
    }

    fun parseProject(name: String, price: String, description: String, dateStamp: Long): Project? {
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
                    value = incomingValue,
                    description = itemIncoming.incomingDescription,
                    date = itemIncoming.incomingDate
                )
                incomingList.add(incoming)
            }
            catch (e: NumberFormatException) {
                _snackbar.value = "Incoming value can not be empty"
                return null
            }
        }

        val project = Project(
            name = name,
            price = price.toDouble(),
            description = description,
            incomings = incomingList
        )
        project.dateStamp = dateStamp
        return project
    }

    fun parseAndPutIncoming(incomingsProject: List<Incoming>) {

        for (incoming in incomingsProject) {
            val incomingDescription = incoming.description
            val incomingValue = incoming.value.toInt().toString()
            val incomingDate = incoming.date
            val dateStamp = incoming.dateStamp
            incomings.add(
                ItemIncoming(
                incomingDescription = incomingDescription,
                incomingValue = incomingValue,
                incomingDate = incomingDate,
                dateStamp = dateStamp
                )
            )
        }

    }

    data class ItemIncoming (
        var incomingDescription: String = "",
        var incomingValue: String = "",
        var incomingDate: Long = Date().time,
        var dateStamp: Long = Date().time) {
        var incomingDateText: String = ""
        init {
            val simpleDateFormat = SimpleDateFormat("MMM d, yyyy", Locale.ENGLISH)
            incomingDateText = simpleDateFormat.format(incomingDate).toString()
        }
    }

    class Factory @Inject constructor(
        private val userSettings: UserSettings
    ): ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(EditProjectViewModel::class.java)) {
                return EditProjectViewModel(
                    userSettings
                ) as T
            }
            else throw IllegalStateException("Unknown ViewModel class")
        }
    }

}