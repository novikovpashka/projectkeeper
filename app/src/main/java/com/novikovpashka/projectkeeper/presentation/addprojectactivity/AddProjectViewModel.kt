package com.novikovpashka.projectkeeper.presentation.addprojectactivity

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.novikovpashka.projectkeeper.data.datafirestore.Incoming
import com.novikovpashka.projectkeeper.data.datafirestore.Project
import com.novikovpashka.projectkeeper.data.datafirestore.ProjectFirestoreRepo
import java.lang.NumberFormatException
import java.text.SimpleDateFormat
import java.util.*

class AddProjectViewModel(application: Application) : AndroidViewModel(application) {
    private val incomings = mutableListOf<ItemIncoming>()
    private val _incomingsLiveData = MutableLiveData<List<ItemIncoming>>()
    private val projectsRepository = ProjectFirestoreRepo.instance!!

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

    fun saveIncomingDate(value: Long, index: Int) {
        incomings[index].incomingDate = value
    }

    fun getAccentColor(): Int {
        return projectsRepository.loadAccentColorFromStorage(getApplication<Application>().applicationContext)
    }

    fun parseProject(name: String, price: String, description: String): Project? {
        val incomingList = mutableListOf<Incoming>()
        Log.v("mytag", "name is $name")
        Log.v("mytag", "price is $price")

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

}