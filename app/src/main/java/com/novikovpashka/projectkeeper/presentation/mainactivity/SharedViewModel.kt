package com.novikovpashka.projectkeeper.presentation.mainactivity

import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.QuerySnapshot
import com.novikovpashka.projectkeeper.AccentColors
import com.novikovpashka.projectkeeper.CurrencyList
import com.novikovpashka.projectkeeper.data.apicurrency.NoConnectivityException
import com.novikovpashka.projectkeeper.data.model.Project
import com.novikovpashka.projectkeeper.data.repository.CurrencyRepository
import com.novikovpashka.projectkeeper.data.repository.FirestoreRepository
import com.novikovpashka.projectkeeper.data.repository.SettingsRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.IllegalStateException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class SharedViewModel @Inject constructor (
    private val firestoreRepository: FirestoreRepository,
    private val settingsRepository: SettingsRepository,
    private val currencyRepository: CurrencyRepository,
) : ViewModel() {

    val projects: LiveData<List<Project>>
        get() = _projects

    private val _projects = MediatorLiveData<List<Project>>()
    private val _projectsObserved = MutableLiveData<List<Project>>()

    val searchTextLiveData = MutableLiveData("")
    val sortParamLiveData = MutableLiveData(
        settingsRepository.loadSortParam()
    )
    val orderParamLiveData = MutableLiveData(
        settingsRepository.loadOrderParam()
    )
    val currency = MutableLiveData(CurrencyList.RUB)
    val selectMode = MutableLiveData(false)

    val usdrubRate = MutableLiveData(settingsRepository
        .loadUSDRateFromStorage())
    val eurrubRate = MutableLiveData(settingsRepository
        .loadEURRateFromStorage())
    val updated: MutableLiveData<String> = MutableLiveData()

    private val projectsToRestore = mutableListOf<Project>()

    val projectsToDelete = MutableLiveData<List<Project>>()
    private val projectsToDeleteList = mutableListOf<Project>()

    val projectsIdToDelete = MutableLiveData<List<Int>>()
    private val projectsIdToDeleteList = mutableListOf<Int>()

    private val _shimmer = MutableLiveData(true)
    val shimmerActive: LiveData<Boolean>
        get() = _shimmer

    private val _snackbarWithAction = MutableLiveData<String?>()
    val snackbarWithAction: LiveData<String?>
        get() = _snackbarWithAction

    private val _snackbarInfo = MutableLiveData<String?>()
    val snackbarInfo: LiveData<String?>
        get() = _snackbarInfo

    private val _title = MutableLiveData<String?>()
    val title: LiveData<String?>
        get() = _title

    init {
        viewModelScope.launch {
            addProjectsObserver()
            currency.value = settingsRepository.loadCurrentCurrencyFromStorage()

            _projects.addSource(_projectsObserved) {
                _projects.value = it
                _shimmer.value = false
            }

            _projects.addSource(searchTextLiveData) {
                viewModelScope.launch {
                    _projects.value = sortAndFilterProjectList()
                    _shimmer.value = false
                }
            }

            _projects.addSource(sortParamLiveData) {
                viewModelScope.launch {
                    _projects.value = sortAndFilterProjectList()
                    _shimmer.value = false
                }
            }

            _projects.addSource(orderParamLiveData) {
                viewModelScope.launch {
                    _projects.value = sortAndFilterProjectList()
                    _shimmer.value = false
                }
            }
        }
    }

    private suspend fun sortAndFilterProjectList(): List<Project> {
        return suspendCoroutine { continuation ->
            val projectsList = mutableListOf<Project>()
            //filter projects by searching text
            if (_projectsObserved.value != null) {
                if (searchTextLiveData.value!!.isNotEmpty()) {
                    for (project in _projectsObserved.value!!) {
                        if (project.name.lowercase(Locale.getDefault())
                                .contains((searchTextLiveData.value!!))
                        )
                            projectsList.add(project)
                    }
                }
                else projectsList.addAll(_projectsObserved.value!!)
            }
            //sort projects
            when (sortParamLiveData.value!!) {
                SortParam.BY_NAME -> {
                    when (orderParamLiveData.value!!) {
                        OrderParam.ASCENDING -> projectsList.sortBy {it.name.lowercase(Locale.getDefault())}
                        OrderParam.DESCENDING -> projectsList.sortByDescending {it.name.lowercase(Locale.getDefault())}
                    }
                }
                SortParam.BY_DATE_ADDED -> {
                    when (orderParamLiveData.value!!) {
                        OrderParam.ASCENDING -> projectsList.sortBy {it.dateStamp}
                        OrderParam.DESCENDING -> projectsList.sortByDescending {it.dateStamp}
                    }
                }
            }
            continuation.resume(projectsList)
        }
    }

    private suspend fun addProjectsObserver() {
        firestoreRepository.getAllProjects().addSnapshotListener { value, _ ->
            if (value != null) {
                viewModelScope.launch {
                    val projectsList = loadProjectsObserved(value)

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
            firestoreRepository.deleteProject(projectsToRestore[0])
            _snackbarWithAction.value = projectsToRestore[0].name + " deleted"
            viewModelScope.launch {
                delay(5000)
                _snackbarWithAction.value = null
            }
        }
        else {
            firestoreRepository.deleteSeveralProjects(projectsToRestore)
            _snackbarWithAction.value = projectsToRestore.size.toString() + " projects deleted"
            viewModelScope.launch {
                delay(5000)
                _snackbarWithAction.value = null
            }
        }
        selectMode.value = false
    }

    fun deleteProject (project: Project) {
        projectsToRestore.clear()
        projectsToRestore.add(project)
        firestoreRepository.deleteProject(project)
        _snackbarWithAction.value = projectsToRestore[0].name + " deleted"
        viewModelScope.launch {
            delay(5000)
            _snackbarWithAction.value = null
        }
    }

    private suspend fun loadProjectsObserved (value: QuerySnapshot): MutableList<Project> {
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

    fun loadRates() {
        viewModelScope.launch {
            try {
                val usdrubRateResponce = currencyRepository.getRateUSDRUB()
                val eurrubRateResponce = currencyRepository.getRateEURRUB()
                if (usdrubRateResponce.isSuccessful && eurrubRateResponce.isSuccessful) {
                    usdrubRate.value = usdrubRateResponce.body()
                    eurrubRate.value = eurrubRateResponce.body()
                    val dateFormat = SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss")
                    updated.value = dateFormat.format(Date())
                    settingsRepository.saveRatesToStorage(
                        usdrubRateResponce.body()!!,
                        eurrubRateResponce.body()!!
                    )
                }
            }
            catch (e: NoConnectivityException) {
                _snackbarInfo.value = e.message
            }
            catch (e: Exception) {
                _snackbarInfo.value = e.message
            }
        }
    }

    fun loadAccentColor(): Int {
        return settingsRepository.loadAccentColorFromStorage()
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

    val currentCurrency = MutableLiveData(
        settingsRepository.loadCurrentCurrencyFromStorage()
    )
    val currentTheme = MutableLiveData(AppCompatDelegate.getDefaultNightMode())
    val accentColor = MutableLiveData(
        settingsRepository.loadAccentColorFromStorage()
    )

    fun saveAndApplyCurrency (currency: CurrencyList) {
        settingsRepository.saveCurrentCurrencyToStorage(currency)
        currentCurrency.value = currency
    }

    fun saveCurrentTheme () {
        settingsRepository.saveCurrentThemeToStorage()
        currentTheme.value = AppCompatDelegate.getDefaultNightMode()
    }

    fun getAccentColorsList(): MutableList<Int> {
        val colorList: MutableList<Int> = mutableListOf()
        for (x in AccentColors.values()) {
            colorList.add(
                x.color
            )
        }
        return colorList
    }

    fun saveAndApplyAccentColor(color: Int) {
        settingsRepository.saveAccentColorToStorage(color)
        accentColor.value = color
    }

    fun saveSortAndOrderParamsToStorage () {
        settingsRepository.saveSortAndOrderParamsToStorage(
            sortParam = sortParamLiveData.value!!,
            orderParam = orderParamLiveData.value!!
        )
    }

    class Factory @Inject constructor(
        private val firestoreRepository: FirestoreRepository,
        private val settingsRepository: SettingsRepository,
        private val currencyRepository: CurrencyRepository
    ): ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SharedViewModel::class.java)) {
                return SharedViewModel(
                    firestoreRepository,
                    settingsRepository,
                    currencyRepository
                ) as T
            }
            else throw IllegalStateException("Unknown ViewModel class")
        }
    }

}

