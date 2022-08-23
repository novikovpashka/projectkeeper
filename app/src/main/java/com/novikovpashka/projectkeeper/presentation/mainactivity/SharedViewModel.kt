package com.novikovpashka.projectkeeper.presentation.mainactivity

import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.*
import com.github.javafaker.Faker
import com.novikovpashka.projectkeeper.AccentColors
import com.novikovpashka.projectkeeper.CurrencyList
import com.novikovpashka.projectkeeper.data.apicurrency.NoConnectivityException
import com.novikovpashka.projectkeeper.data.model.Incoming
import com.novikovpashka.projectkeeper.data.model.Project
import com.novikovpashka.projectkeeper.data.repository.CurrencyRepository
import com.novikovpashka.projectkeeper.data.repository.FirestoreRepository
import com.novikovpashka.projectkeeper.data.repository.Repository
import com.novikovpashka.projectkeeper.data.UserSettings
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class SharedViewModel @Inject constructor(
    private val firestoreRepository: FirestoreRepository,
    private val userSettings: UserSettings,
    private val currencyRepository: CurrencyRepository,
    private val repository: Repository
) : ViewModel() {

    val projects: LiveData<List<Project>>
        get() = _projects
    private val _projects = MediatorLiveData<List<Project>>()

    private var currentProjectList = mutableListOf<Project>()
    private var filteredProjectList = mutableListOf<Project>()

    val searchTextLiveData = MutableLiveData("")

    val sortParamLiveData = MutableLiveData(userSettings.loadSortParam())
    fun setSortParam(sortParam: SortParam) {
        sortParamLiveData.value = sortParam
    }

    val orderParamLiveData = MutableLiveData(userSettings.loadOrderParam())
    fun setOrderParam(orderParam: OrderParam) {
        orderParamLiveData.value = orderParam
    }

    val currencyLiveData = MutableLiveData(userSettings.loadCurrentCurrencyFromStorage())
    fun setCurrency(currency: CurrencyList) {
        currencyLiveData.value = currency
        userSettings.saveCurrentCurrencyToStorage(currency)
    }

    val selectMode = MutableLiveData(false)
    val progressBar = MutableLiveData(false)

    val usdrubRate = MutableLiveData(userSettings.loadUSDRateFromStorage())
    val eurrubRate = MutableLiveData(userSettings.loadEURRateFromStorage())
    val ratesUpdatedDate: MutableLiveData<String> = MutableLiveData()

    private val projectsToRestore = mutableListOf<Project>()
    private val projectsToDelete = mutableListOf<Project>()

    val selectedId = MutableLiveData<List<Int>>()
    private val selectedIdList = mutableListOf<Int>()

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

        currencyLiveData.value = userSettings.loadCurrentCurrencyFromStorage()

        _projects.addSource(searchTextLiveData) {
            viewModelScope.launch {
                sortAndSetProjectList()
            }
        }

        _projects.addSource(sortParamLiveData) {
            viewModelScope.launch {
                sortAndSetProjectList()
            }
        }

        _projects.addSource(orderParamLiveData) {
            viewModelScope.launch {
                sortAndSetProjectList()
            }
        }

    }

    private fun loadProjects() = viewModelScope.launch(Dispatchers.Default) {
        currentProjectList = repository.getProjects(viewModelScope).toMutableList()
        sortAndSetProjectList()
        _shimmer.postValue(false)
    }

    private suspend fun sortAndSetProjectList() = withContext(Dispatchers.IO) {
        val projectsList = mutableListOf<Project>()
        //filter projects by searching text
        if (currentProjectList.isNotEmpty()) {
            if (searchTextLiveData.value!!.isNotEmpty()) {
                val project = List(currentProjectList.size) {
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
        filteredProjectList = projectsList
        _projects.postValue(filteredProjectList)
    }

    fun checkTextFiltered(project: Project): Project? {
        return if (project.name.lowercase(Locale.getDefault())
                .contains((searchTextLiveData.value!!))
        ) project
        else null
    }

    fun addProject(project: Project) = viewModelScope.launch(Dispatchers.IO) {
        repository.addProject(project)
        currentProjectList.add(project)
        sortAndSetProjectList()
    }

    private fun addSeveralProjects(projects: List<Project>) =
        viewModelScope.launch(Dispatchers.IO) {
            repository.addMultipleProjects(projects)
            currentProjectList.addAll(projects)
            sortAndSetProjectList()
        }

    fun updateProject(project: Project) = viewModelScope.launch(Dispatchers.IO) {
        firestoreRepository.updateProject(project).addOnCompleteListener {
            viewModelScope.launch(Dispatchers.Default) {
                currentProjectList[currentProjectList.indexOf(project)] = project
                sortAndSetProjectList()
                makeSnackbarInfo("${project.name} updated")
            }
        }
    }

    fun deleteProject(project: Project) = viewModelScope.launch(Dispatchers.IO) {
        progressBar.postValue(true)
        projectsToRestore.clear()
        projectsToRestore.add(project)
        repository.deleteProject(project)
        currentProjectList.remove(project)
        sortAndSetProjectList()
        progressBar.postValue(false)
        makeSnackbarInfo("${project.name} deleted")
    }

    fun deleteSelectedProjects() = viewModelScope.launch(Dispatchers.IO) {
        progressBar.postValue(true)
        projectsToRestore.clear()
        projectsToRestore.addAll(projectsToDelete)
        if (projectsToRestore.size == 1) {
            repository.deleteProject(projectsToRestore[0])
            currentProjectList.remove(projectsToRestore[0])
            sortAndSetProjectList()
            selectMode.postValue(false)
            progressBar.postValue(false)
            makeSnackbarWIthAction("${projectsToRestore[0].name} deleted")
        } else {
            repository.deleteMultipleProjects(projectsToRestore)
            currentProjectList.removeAll(projectsToRestore)
            sortAndSetProjectList()
            selectMode.postValue(false)
            progressBar.postValue(false)
            makeSnackbarWIthAction("${projectsToRestore.size} projects deleted")
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
                userSettings.saveRatesToStorage(
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
        if (projectsToDelete.isEmpty()) {
            selectMode.value = true
        }
        projectsToDelete.add(project)
        selectedIdList.add(position)
        selectedId.value = selectedIdList
        _title.value = selectedIdList.size.toString()
    }

    fun removeProjectToDelete(project: Project, position: Int) {
        projectsToDelete.remove(project)
        selectedIdList.remove(position)
        selectedId.value = selectedIdList
        if (projectsToDelete.isEmpty()) {
            _title.value = null
            selectMode.value = false
        } else _title.value = selectedIdList.size.toString()
    }

    fun clearSelectedProjects(): MutableList<Int> {
        val idToNotify = mutableListOf<Int>()
        idToNotify.addAll(selectedIdList)
        projectsToDelete.clear()
        selectedIdList.clear()
        selectedId.value = selectedIdList
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

    val nightMode = MutableLiveData(userSettings.loadNightModeFromStorage())
    fun setNightMode(nightmode: NightMode) {
        nightMode.value = nightmode.value
        AppCompatDelegate.setDefaultNightMode(nightmode.value)
        userSettings.saveNightModeToStorage()
    }

    val accentColor = MutableLiveData(userSettings.loadAccentColorFromStorage())
    fun setAccentColor(color: Int) {
        if (color != accentColor.value) {
            userSettings.saveAccentColorToStorage(color)
            accentColor.value = color
        }
    }

    fun loadAccentColorFromStorage(): Int {
        return userSettings.loadAccentColorFromStorage()
    }

    fun loadThemeIdFromStorage(): Int {
        return userSettings.loadThemeIdFromStorage()
    }

    fun getAccentColorsList(): MutableList<Int> {
        val colorList: MutableList<Int> = mutableListOf()
        for (x in AccentColors.values()) {
            colorList.add(x.color)
        }
        return colorList
    }

    fun saveSortAndOrderParamsToStorage() {
        userSettings.saveSortAndOrderParamsToStorage(
            sortParam = sortParamLiveData.value!!,
            orderParam = orderParamLiveData.value!!
        )
    }

    fun addFiveRandomProject() {
        viewModelScope.launch {

            val projects: MutableList<Project> = mutableListOf()

            for (i in 1..5) {
                projects.add(getRandomProject())
                delay(1)
            }
            addSeveralProjects(projects)
        }
    }

    val faker = Faker.instance()

    private suspend fun getRandomProject(): Project {
        return suspendCoroutine { continuation ->
            val name = faker.country().capital()
            val description = faker.harryPotter().quote()
            val price = (100000 until 1000000).random().toDouble()

            val incomings: MutableList<Incoming> = ArrayList()
            for (i in 0..4) {
                val incomingDescription =
                    faker.harryPotter().quote()

                val x = (price / 7).toInt()
                val y = (price / 10).toInt()

                val incomingValue = (y until x).random().toDouble()

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
        private val userSettings: UserSettings,
        private val currencyRepository: CurrencyRepository,
        private val repository: Repository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SharedViewModel::class.java)) {
                return SharedViewModel(
                    firestoreRepository,
                    userSettings,
                    currencyRepository,
                    repository
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

