package com.novikovpashka.projectkeeper.presentation.mainactivity


import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.novikovpashka.projectkeeper.AccentColors
import com.novikovpashka.projectkeeper.CurrencyList
import com.novikovpashka.projectkeeper.data.apicurrency.NoConnectivityException
import com.novikovpashka.projectkeeper.data.dataprojects.Project
import com.novikovpashka.projectkeeper.data.dataprojects.FirestoreRepo
import com.novikovpashka.projectkeeper.data.dataprojects.SettingsRepo
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class SharedViewModel(application: Application) : AndroidViewModel(application) {

    private val firestoreRepository = FirestoreRepo.instance!!
    private val settingsRepository = SettingsRepo.instance!!

    val projects: LiveData<List<Project>>
        get() = _projects

    private val _projects = MediatorLiveData<List<Project>>()
    private val _projectsObserved = MutableLiveData<List<Project>>()

    val searchTextLiveData = MutableLiveData("")
    val sortParamLiveData = MutableLiveData(SortParam.BY_DATE_ADDED)
    val orderParamLiveData = MutableLiveData(OrderParam.ASCENDING)
    val currency = MutableLiveData(CurrencyList.RUB)
    val selectMode = MutableLiveData(false)

    val USDRUB = MutableLiveData(settingsRepository
        .loadUSDRateFromStorage(application.applicationContext))
    val EURRUB = MutableLiveData(settingsRepository
        .loadEURRateFromStorage(application.applicationContext))
    val updated: MutableLiveData<String> = MutableLiveData()

    val projectsToRestore = mutableListOf<Project>()

    val projectsToDelete = MutableLiveData<List<Project>>()
    private val projectsToDeleteList = mutableListOf<Project>()

    val projectsIdToDelete = MutableLiveData<List<Int>>()
    private val projectsIdToDeleteList = mutableListOf<Int>()

    private val _shimmer = MutableLiveData(true)
    val shimmerActive: LiveData<Boolean>
        get() = _shimmer

    private val _snackbar = MutableLiveData<String?>()
    val snackbar: LiveData<String?>
        get() = _snackbar

    private val _title = MutableLiveData<String?>()
    val title: LiveData<String?>
        get() = _title

    init {
        viewModelScope.launch {
            addProjectsObserver()
            currency.value = settingsRepository
                .loadCurrentCurrencyFromStorage(getApplication<Application>().applicationContext)

            _projects.addSource(_projectsObserved) {
                _projects.value = it
                _shimmer.value = false
            }

            _projects.addSource(searchTextLiveData) {
                viewModelScope.launch {
                    _projects.value = sortOrFilterProjectsList(
                        it,
                        sortParamLiveData.value!!,
                        orderParamLiveData.value!!
                    )
                    _shimmer.value = false
                }
            }

            _projects.addSource(sortParamLiveData) {
                viewModelScope.launch {
                    _projects.value = sortOrFilterProjectsList(
                        searchTextLiveData.value!!,
                        it,
                        orderParamLiveData.value!!
                    )
                    _shimmer.value = false
                }
            }

            _projects.addSource(orderParamLiveData) {
                viewModelScope.launch {
                    _projects.value = sortOrFilterProjectsList(
                        searchTextLiveData.value!!,
                        sortParamLiveData.value!!,
                        it
                    )
                    _shimmer.value = false
                }
            }
        }
    }

    private suspend fun sortOrFilterProjectsList (searchText: String, sortParam: SortParam, orderParam: OrderParam): List<Project> {
        return suspendCoroutine { continuation ->
            val projectsList = mutableListOf<Project>()
            if (_projectsObserved.value != null) {
                for (project in _projectsObserved.value!!) {
                    if (project.name.lowercase(Locale.getDefault())
                            .contains((searchText))
                    )
                        projectsList.add(project)
                }
            }

            if (sortParam == SortParam.BY_NAME) projectsList.sortBy { it.name.lowercase(
                Locale.getDefault()
            ) }
            else projectsList.sortBy { it.dateStamp }

            if (orderParam == OrderParam.DESCENDING) projectsList.reverse()

            continuation.resume(projectsList)
        }
    }

    suspend fun addProjectsObserver() {
        firestoreRepository.getAllProjects().addSnapshotListener { value, error ->
            if (value != null) {
                viewModelScope.launch {
                    val projectsList = loadProjectsObserved(value, error)

                    if (sortParamLiveData.value == SortParam.BY_NAME) projectsList.sortBy { it.name.lowercase(
                        Locale.getDefault()
                    ) }
                    else projectsList.sortBy { it.dateStamp }

                    if (orderParamLiveData.value == OrderParam.DESCENDING) projectsList.reverse()

                    _projectsObserved.value = projectsList
                }
            }
        }
    }

    fun addProject (project: Project) {
        firestoreRepository.addProject(project)
    }

    fun updateProject (project: Project) {
        firestoreRepository.updateProject(project)
    }

    fun deleteSelectedProjects () {
        projectsToRestore.clear()
        projectsToRestore.addAll(projectsToDeleteList)
        if (projectsToRestore.size == 1) {
            firestoreRepository.deleteProject(projectsToRestore.get(0))
            _snackbar.value = projectsToRestore.get(0).name + " deleted"
            viewModelScope.launch {
                delay(5000)
                _snackbar.value = null
            }
        }
        else {
            firestoreRepository.deleteSeveralProjects(projectsToRestore)
            _snackbar.value = projectsToRestore.size.toString() + " projects deleted"
            viewModelScope.launch {
                delay(5000)
                _snackbar.value = null
            }
        }
        selectMode.value = false
    }

    fun deleteProject (project: Project) {
        projectsToRestore.clear()
        projectsToRestore.add(project)
        firestoreRepository.deleteProject(project)
        _snackbar.value = projectsToRestore.get(0).name + " deleted"
        viewModelScope.launch {
            delay(5000)
            _snackbar.value = null
        }
    }

    private suspend fun loadProjectsObserved (value: QuerySnapshot, error: FirebaseFirestoreException?): MutableList<Project> {
        return suspendCoroutine { continuation ->
            val projectsList = mutableListOf<Project>()
            for (obj in value) {
                val project = obj.toObject(Project::class.java)
                if (project.name.lowercase(Locale.getDefault())
                        .contains(searchTextLiveData.value!!)
                )
                    projectsList.add(project)
            }
            continuation.resume(projectsList)
        }
    }

    fun loadRateUSDRUB() {
        viewModelScope.launch {
            try {
                val value = settingsRepository.getRateUSDRUB(getApplication<Application>().applicationContext)
                if (value.isSuccessful) {
                    USDRUB.value = value.body()
                    val dateFormat = SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss")
                    updated.value = dateFormat.format(Date())
                    settingsRepository.saveUSDRateToStorage(getApplication<Application>().applicationContext, value.body()!!)
                }
            }
            catch (e: NoConnectivityException) {
                _snackbar.value = e.message
            }
        }
    }

    fun loadRateEURRUB() {
        viewModelScope.launch {
            try {
                val value = settingsRepository.getRateEURRUB(getApplication<Application>().applicationContext)
                if (value.isSuccessful) {
                    EURRUB.value = value.body()
                    val dateFormat = SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss")
                    updated.value = dateFormat.format(Date())
                    settingsRepository.saveEURRateToStorage(getApplication<Application>().applicationContext, value.body()!!)
                }
            }
            catch (e: NoConnectivityException) {
                _snackbar.value = e.message
            }
        }
    }

    fun loadAccentColor(): Int {
        return settingsRepository.loadAccentColorFromStorage(getApplication<Application>().applicationContext)
    }

    fun addProjectToDelete (project: Project, position: Int) {
        projectsToDeleteList.add(project)
        projectsToDelete.value = projectsToDeleteList
        projectsIdToDeleteList.add(position)
        projectsIdToDelete.value = projectsIdToDeleteList
        _title.value = projectsIdToDeleteList.size.toString()
    }
    fun removeProjectToDelete (project: Project, position: Int) {
        projectsToDeleteList.remove(project)
        projectsToDelete.value = projectsToDeleteList
        projectsIdToDeleteList.remove(position)
        projectsIdToDelete.value = projectsIdToDeleteList
        if (projectsToDeleteList.isEmpty()) {
            _title.value = null
        }
        else _title.value = projectsIdToDeleteList.size.toString()
    }

    fun clearSelectedProjects() : MutableList<Int> {
        val idToNotify = mutableListOf<Int>()
        idToNotify.addAll(projectsIdToDeleteList)
        projectsToDeleteList.clear()
        projectsToDelete.value = projectsToDeleteList
        projectsIdToDeleteList.clear()
        projectsIdToDelete.value = projectsIdToDeleteList
        selectMode.value = false
        _title.value = null
        return idToNotify
    }

    fun restoreDeletedProjects() {
        for (project in projectsToRestore) {
            addProject(project)
        }
        projectsToRestore.clear()
    }

    val currentCurrency = MutableLiveData(settingsRepository
        .loadCurrentCurrencyFromStorage(application.applicationContext))
    val currentTheme = MutableLiveData(AppCompatDelegate.getDefaultNightMode())
    val accentColor = MutableLiveData(settingsRepository.loadAccentColorFromStorage(application.applicationContext))

    fun saveAndApplyCurrency (currency: CurrencyList) {
        settingsRepository.saveCurrentCurrencyToStorage(getApplication<Application>()
            .applicationContext, currency)
        currentCurrency.value = currency
    }

    fun saveCurrentTheme () {
        settingsRepository.saveCurrentThemeToStorage(getApplication<Application>()
            .applicationContext)
        currentTheme.value = AppCompatDelegate.getDefaultNightMode()
    }

    fun saveAndApplyAccentColor(color: Int) {
        settingsRepository.saveAccentColorToStorage(getApplication<Application>().applicationContext, color)
        accentColor.value = color
    }

    fun getAccentColors(): MutableList<Int> {
        val colorList: MutableList<Int> = mutableListOf()
        for (x in AccentColors.values()) {
            colorList.add(ContextCompat.getColor(getApplication<Application>().applicationContext, x.color))
        }
        return colorList
    }

    enum class SortParam {BY_NAME, BY_DATE_ADDED}
    enum class OrderParam {ASCENDING, DESCENDING}
}

