package com.novikovpashka.projectkeeper.presentation.mainactivity

import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.*
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.QuerySnapshot
import com.novikovpashka.projectkeeper.AccentColors
import com.novikovpashka.projectkeeper.CurrencyList
import com.novikovpashka.projectkeeper.data.apicurrency.NoConnectivityException
import com.novikovpashka.projectkeeper.data.model.Incoming
import com.novikovpashka.projectkeeper.data.model.Project
import com.novikovpashka.projectkeeper.data.repository.CurrencyRepository
import com.novikovpashka.projectkeeper.data.repository.FirestoreRepository
import com.novikovpashka.projectkeeper.data.repository.SettingsRepository
import kotlinx.coroutines.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class SharedViewModel @Inject constructor(
    private val firestoreRepository: FirestoreRepository,
    private val settingsRepository: SettingsRepository,
    private val currencyRepository: CurrencyRepository,
) : ViewModel() {

    val projects: LiveData<List<Project>>
        get() = _projects
    private val _projects = MediatorLiveData<List<Project>>()

    private var currentProjectList = mutableListOf<Project>()
    private var filteredProjectList = mutableListOf<Project>()

    val searchTextLiveData = MutableLiveData("")

    val sortParamLiveData = MutableLiveData(settingsRepository.loadSortParam())
    fun setSortParam(sortParam: SortParam) {
        sortParamLiveData.value = sortParam
    }

    val orderParamLiveData = MutableLiveData(settingsRepository.loadOrderParam())
    fun setOrderParam(orderParam: OrderParam) {
        orderParamLiveData.value = orderParam
    }

    val currencyLiveData = MutableLiveData(settingsRepository.loadCurrentCurrencyFromStorage())
    fun setCurrency(currency: CurrencyList) {
        currencyLiveData.value = currency
        settingsRepository.saveCurrentCurrencyToStorage(currency)
    }

    val selectMode = MutableLiveData(false)
    val progressBar = MutableLiveData(false)

    val usdrubRate = MutableLiveData(settingsRepository.loadUSDRateFromStorage())
    val eurrubRate = MutableLiveData(settingsRepository.loadEURRateFromStorage())
    val ratesUpdatedDate: MutableLiveData<String> = MutableLiveData()

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

    val toolbarMode = MutableLiveData(ToolbarMode.DEFAULT)

    init {

        loadProjects()

        loadRatesAndSaveToStorage()

        currencyLiveData.value = settingsRepository.loadCurrentCurrencyFromStorage()

        _projects.addSource(searchTextLiveData) {
            viewModelScope.launch {
                sortAndFilterProjectList()
                _projects.value = filteredProjectList
            }
        }

        _projects.addSource(sortParamLiveData) {
            viewModelScope.launch {
                sortAndFilterProjectList()
                _projects.value = filteredProjectList
            }
        }

        _projects.addSource(orderParamLiveData) {
            viewModelScope.launch {
                sortAndFilterProjectList()
                _projects.value = filteredProjectList
            }
        }

    }

    private fun loadProjects() = viewModelScope.launch(Dispatchers.Default) {
        val loadedProjectsList = mutableListOf<Project>()
        getProjects().addOnSuccessListener {
            viewModelScope.launch(Dispatchers.Default) {
                val list = mutableListOf<Deferred<Project>>()
                for (item in it) {
                    val job = async {
                        item.toObject(Project::class.java)
                    }
                    list.add(job)
                }
                for (project in list) {
                    loadedProjectsList.add(project.await())
                }
                currentProjectList = loadedProjectsList
                sortAndFilterProjectList()
                _projects.postValue(filteredProjectList)
                _shimmer.postValue(false)
            }
        }
    }

    private suspend fun getProjects(): Task<QuerySnapshot> = viewModelScope.async {
        withContext(Dispatchers.IO) {
            return@withContext firestoreRepository.getProjects().get()
        }
    }.await()

    private suspend fun sortAndFilterProjectList() = withContext(Dispatchers.IO) {
        val projectsList = mutableListOf<Project>()
        //filter projects by searching text
        if (currentProjectList.isNotEmpty()) {
            if (searchTextLiveData.value!!.isNotEmpty()) {
                val project: List<Deferred<Project?>> = List(currentProjectList.size) {
                    async {
                        return@async checkTextFiltered(currentProjectList[it])
                    }
                }
                for (proj in project) {
                    proj.await()?.let { projectsList.add(it) }
                }
            } else projectsList.addAll(currentProjectList)
        }
        //sort projects
        launch {
            when (sortParamLiveData.value!!) {
                SortParam.BY_NAME -> {
                    when (orderParamLiveData.value!!) {
                        OrderParam.ASCENDING -> projectsList.sortBy { it.name.lowercase(Locale.getDefault()) }
                        OrderParam.DESCENDING -> projectsList.sortByDescending {
                            it.name.lowercase(Locale.getDefault())
                        }
                    }
                }
                SortParam.BY_DATE_ADDED -> {
                    when (orderParamLiveData.value!!) {
                        OrderParam.ASCENDING -> projectsList.sortBy { it.dateStamp }
                        OrderParam.DESCENDING -> projectsList.sortByDescending { it.dateStamp }
                    }
                }
            }
        }
        filteredProjectList = projectsList
    }

    fun checkTextFiltered(project: Project): Project? {
        return if (project.name.lowercase(Locale.getDefault())
                .contains((searchTextLiveData.value!!))
        ) project
        else null
    }

    suspend fun setProjectsPostValue() {
        sortAndFilterProjectList()
        _projects.postValue(filteredProjectList)
    }

    fun addProject(project: Project) = viewModelScope.launch(Dispatchers.IO) {
        firestoreRepository.addProject(project).addOnCompleteListener {
            viewModelScope.launch(Dispatchers.Default) {
                currentProjectList.add(project)
                setProjectsPostValue()
            }
        }
    }

    private fun addSeveralProjects(projects: List<Project>) =
        viewModelScope.launch(Dispatchers.IO) {
            firestoreRepository.addSeveralProjects(projects).addOnCompleteListener {
                viewModelScope.launch(Dispatchers.Default) {
                    currentProjectList.addAll(projects)
                    setProjectsPostValue()
                }
            }
        }

    fun updateProject(project: Project) = viewModelScope.launch(Dispatchers.IO) {
        firestoreRepository.updateProject(project).addOnCompleteListener {
            viewModelScope.launch(Dispatchers.Default) {
                currentProjectList[currentProjectList.indexOf(project)] = project
                setProjectsPostValue()
                makeSnackbarInfo("${project.name} updated")
            }
        }
    }

    fun deleteProject(project: Project) = viewModelScope.launch {
        progressBar.value = true
        projectsToRestore.clear()
        projectsToRestore.add(project)
        withContext(Dispatchers.IO) {
            firestoreRepository.deleteProject(project).addOnCompleteListener {
                viewModelScope.launch(Dispatchers.Default) {
                    currentProjectList.remove(project)
                    setProjectsPostValue()
                    progressBar.postValue(false)
                    makeSnackbarInfo("${project.name} deleted")
                }
            }
        }
    }

    fun deleteSelectedProjects() = viewModelScope.launch {
        progressBar.value = true
        projectsToRestore.clear()
        projectsToRestore.addAll(projectsToDeleteList)
        withContext(Dispatchers.Default) {
            if (projectsToRestore.size == 1) {
                firestoreRepository.deleteProject(projectsToRestore[0]).addOnCompleteListener {
                    viewModelScope.launch(Dispatchers.Default) {
                        currentProjectList.remove(projectsToRestore[0])
                        setProjectsPostValue()
                        selectMode.postValue(false)
                        progressBar.postValue(false)
                        makeSnackbarWIthAction("${projectsToRestore[0].name} deleted")
                    }
                }
            } else {
                firestoreRepository.deleteSeveralProjects(projectsToRestore).addOnCompleteListener {
                    viewModelScope.launch(Dispatchers.Default) {
                        currentProjectList.removeAll(projectsToRestore)
                        setProjectsPostValue()
                        selectMode.postValue(false)
                        progressBar.postValue(false)
                        makeSnackbarWIthAction("${projectsToRestore.size} projects deleted")
                    }
                }
            }
        }
    }

    private suspend fun makeSnackbarInfo(message: String) {
        _snackbarInfo.postValue(message)
        delay(3000)
        _snackbarWithAction.postValue(null)
    }

    private suspend fun makeSnackbarWIthAction(message: String) {
        _snackbarWithAction.value?.let {
            viewModelScope.launch {
                _snackbarWithAction.postValue(null)
            }.join()
        }
        _snackbarWithAction.postValue(message)
        delay(5000)
        _snackbarWithAction.postValue(null)
    }

    fun loadRatesAndSaveToStorage() = viewModelScope.launch(Dispatchers.IO) {
        try {
            val usdrubRateResponce = currencyRepository.getRateUSDRUB()
            val eurrubRateResponce = currencyRepository.getRateEURRUB()
            if (usdrubRateResponce.isSuccessful && eurrubRateResponce.isSuccessful) {
                usdrubRate.postValue(usdrubRateResponce.body())
                eurrubRate.postValue(eurrubRateResponce.body())
                val dateFormat = SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss")
                ratesUpdatedDate.postValue(dateFormat.format(Date()))
                settingsRepository.saveRatesToStorage(
                    usdrubRateResponce.body()!!,
                    eurrubRateResponce.body()!!
                )
            }
        } catch (e: NoConnectivityException) {
            makeSnackbarInfo(e.message)
        } catch (e: Exception) {
            makeSnackbarInfo(e.message!!)
        }
    }

    fun addProjectToDelete(project: Project, position: Int) {
        if (projectsToDeleteList.isEmpty()) {
            selectMode.value = true
        }
        projectsToDeleteList.add(project)
        projectsToDelete.value = projectsToDeleteList
        projectsIdToDeleteList.add(position)
        projectsIdToDelete.value = projectsIdToDeleteList
        _title.value = projectsIdToDeleteList.size.toString()
    }

    fun removeProjectToDelete(project: Project, position: Int) {
        projectsToDeleteList.remove(project)
        projectsToDelete.value = projectsToDeleteList
        projectsIdToDeleteList.remove(position)
        projectsIdToDelete.value = projectsIdToDeleteList
        if (projectsToDeleteList.isEmpty()) {
            _title.value = null
            selectMode.value = false
        } else _title.value = projectsIdToDeleteList.size.toString()
    }

    fun clearSelectedProjects(): MutableList<Int> {
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
        if (projectsToRestore.size == 1) {
            addProject(projectsToRestore[0])
        } else {
            addSeveralProjects(projectsToRestore)
        }
    }

    val nightMode = MutableLiveData(settingsRepository.loadNightModeFromStorage())
    fun setNightMode(nightmode: NightMode) {
        nightMode.value = nightmode.value
        AppCompatDelegate.setDefaultNightMode(nightmode.value)
        settingsRepository.saveNightModeToStorage()
    }

    val accentColor = MutableLiveData(settingsRepository.loadAccentColorFromStorage())
    fun setAccentColor(color: Int) {
        if (color != accentColor.value) {
            settingsRepository.saveAccentColorToStorage(color)
            accentColor.value = color
        }
    }

    fun loadAccentColorFromStorage(): Int {
        return settingsRepository.loadAccentColorFromStorage()
    }

    fun loadThemeIdFromStorage(): Int {
        return settingsRepository.loadThemeIdFromStorage()
    }

    fun getAccentColorsList(): MutableList<Int> {
        val colorList: MutableList<Int> = mutableListOf()
        for (x in AccentColors.values()) {
            colorList.add(x.color)
        }
        return colorList
    }


    fun saveSortAndOrderParamsToStorage() {
        settingsRepository.saveSortAndOrderParamsToStorage(
            sortParam = sortParamLiveData.value!!,
            orderParam = orderParamLiveData.value!!
        )
    }

    fun addFiveRandomProject() {
        viewModelScope.launch {
            val projects: List<Deferred<Project>> = List(5) {
                async {
                    getRandomProject()
                }
            }
            addSeveralProjects(projects.awaitAll())
        }
    }

    private suspend fun getRandomProject(): Project {
        return suspendCoroutine { continuation ->
            val name = "Test" + (Math.random() * 100000).toInt()
            val description = "Project description" + (Math.random() * 1000000).toInt()
            val price =
                DecimalFormat("####").format(((Math.random() * 200000).toInt() / 1000 * 1000).toLong())
                    .toDouble()
            val incomings: MutableList<Incoming> = ArrayList()
            for (i in 0..19) {
                val incomingDescription =
                    "Incoming description" + (Math.random() * 1000000).toInt()

                val incomingValue = DecimalFormat("####")
                    .format(((Math.random() * price).toInt() / 20 / 1000 * 1000).toLong())
                    .toDouble()
                val incoming = Incoming(
                    incomingDescription,
                    incomingValue,
                    Date().time
                )
                incomings.add(incoming)
            }
            continuation.resume(Project(name, price, description, incomings))
        }
    }

    class Factory @Inject constructor(
        private val firestoreRepository: FirestoreRepository,
        private val settingsRepository: SettingsRepository,
        private val currencyRepository: CurrencyRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SharedViewModel::class.java)) {
                return SharedViewModel(
                    firestoreRepository,
                    settingsRepository,
                    currencyRepository
                ) as T
            } else throw IllegalStateException("Unknown ViewModel class")
        }
    }

    enum class ToolbarMode { DEFAULT, SELECT, SEARCH }

    enum class NightMode(val value: Int) {
        AS_SYSTEM(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM),
        NIGHT(AppCompatDelegate.MODE_NIGHT_YES),
        DAY(AppCompatDelegate.MODE_NIGHT_NO)
    }

}

