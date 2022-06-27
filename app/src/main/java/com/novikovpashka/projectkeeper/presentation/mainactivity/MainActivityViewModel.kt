package com.novikovpashka.projectkeeper.presentation.mainactivity


import android.app.Application
import androidx.lifecycle.*
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.novikovpashka.projectkeeper.CurrencyList
import com.novikovpashka.projectkeeper.data.apicurrency.NoConnectivityException
import com.novikovpashka.projectkeeper.data.datafirestore.Project
import com.novikovpashka.projectkeeper.data.datafirestore.ProjectFirestoreRepo
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class MainActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val projectsRepository = ProjectFirestoreRepo.instance!!

    val searchTextLiveData = MutableLiveData("")
    val sortParamLiveData = MutableLiveData(SortParam.BY_DATE_ADDED)
    val orderParamLiveData = MutableLiveData(OrderParam.ASCENDING)
    val currency = MutableLiveData(CurrencyList.RUB)
    val selectMode = MutableLiveData(false)
    val USDRUB = MutableLiveData(projectsRepository
        .loadUSDRateFromStorage(application.applicationContext))
    val EURRUB = MutableLiveData(projectsRepository
        .loadEURRateFromStorage(application.applicationContext))
    val updated: MutableLiveData<String> = MutableLiveData()

    var currentAccentColor = projectsRepository.loadAccentColorFromStorage(getApplication<Application>().applicationContext)

    fun getValueUSDRUB() {
        viewModelScope.launch {
            try {
                val value = projectsRepository.getUSDRUB(getApplication<Application>().applicationContext)
                if (value.isSuccessful) {
                    USDRUB.value = value.body()
                    val dateFormat = SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss")
                    updated.value = dateFormat.format(Date())
                    projectsRepository.saveUSDRateToStorage(getApplication<Application>().applicationContext, value.body()!!)
                }
            }
            catch (e: NoConnectivityException) {
                _snackbar.value = e.message
            }
        }
    }

    fun getValueEURRUB() {
        viewModelScope.launch {
            try {
                val value = projectsRepository.getEURRUB(getApplication<Application>().applicationContext)
                if (value.isSuccessful) {
                    EURRUB.value = value.body()
                    val dateFormat = SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss")
                    updated.value = dateFormat.format(Date())
                    projectsRepository.saveEURRateToStorage(getApplication<Application>().applicationContext, value.body()!!)
                }
            }
            catch (e: NoConnectivityException) {
                _snackbar.value = e.message
            }
        }
    }

    val projectsToRestore = mutableListOf<Project>()

    val projectsToDelete = MutableLiveData<List<Project>>()
    private val projectsToDeleteList = mutableListOf<Project>()

    val projectsIdToDelete = MutableLiveData<List<Int>>()
    private val projectsIdToDeleteList = mutableListOf<Int>()

    private val _shimmerActive = MutableLiveData(true)
    val shimmerActive: LiveData<Boolean>
        get() = _shimmerActive

    private val _snackbar = MutableLiveData<String?>()
    val snackbar: LiveData<String?>
        get() = _snackbar

    private val _title = MutableLiveData<String?>()
    val title: LiveData<String?>
        get() = _title


    val projects: LiveData<List<Project>>
        get() = _projects

    private val _projects = MediatorLiveData<List<Project>>()
    private val _projectsObserved = MutableLiveData<List<Project>>()

    init {
        addProjectsObserver()
        currency.value = projectsRepository
            .loadCurrentCurrencyFromStorage(getApplication<Application>().applicationContext)

        _projects.addSource(searchTextLiveData) {
            viewModelScope.launch {
                _projects.value = getProjectsList(it, sortParamLiveData.value!!, orderParamLiveData.value!!)
                _shimmerActive.value = false
            }
        }

        _projects.addSource(sortParamLiveData) {
            viewModelScope.launch {
                _projects.value = getProjectsList(searchTextLiveData.value!!, it, orderParamLiveData.value!!)
                _shimmerActive.value = false
            }
        }

        _projects.addSource(orderParamLiveData) {
            viewModelScope.launch {
                _projects.value = getProjectsList(searchTextLiveData.value!!,sortParamLiveData.value!!, it)
                _shimmerActive.value = false
            }
        }

        _projects.addSource(_projectsObserved) {
            _projects.value = it
            _shimmerActive.value = false
        }
    }

    private suspend fun getProjectsList(searchText: String, sortParam: SortParam, orderParam: OrderParam): List<Project> {
        return suspendCoroutine { continuation ->
            val projectsList = mutableListOf<Project>()
            projectsRepository.getAllProjects().get()
                .addOnSuccessListener { queryDocumentSnapshots ->
                    if (!queryDocumentSnapshots.isEmpty) {
                        val documentSnapshotList = queryDocumentSnapshots.documents
                        for (documentSnapshot: DocumentSnapshot in documentSnapshotList) {
                            val project = documentSnapshot.toObject(Project::class.java)
                            if (project!!.name.lowercase(Locale.getDefault())
                                    .contains((searchText)))
                                        projectsList.add(project)
                        }
                    }
                    if (sortParam == SortParam.BY_NAME) projectsList.sortBy { it.name.lowercase(
                        Locale.getDefault()
                    ) }
                    else projectsList.sortBy { it.dateAdded }

                    if (orderParam == OrderParam.DESCENDING) projectsList.reverse()

                    continuation.resume(projectsList)
                }
        }
    }

    private fun addProjectsObserver() {
        projectsRepository.getAllProjects().addSnapshotListener { value, error ->
            if (value != null) {
                viewModelScope.launch {
                    var projectsList = loadProjectsObserved(value, error)

                    if (sortParamLiveData.value == SortParam.BY_NAME) projectsList.sortBy { it.name.lowercase(
                        Locale.getDefault()
                    ) }
                    else projectsList.sortBy { it.dateAdded }

                    if (orderParamLiveData.value == OrderParam.DESCENDING) projectsList.reverse()

                    _projectsObserved.value = projectsList
                }
            }
        }
    }

    fun addProject (project: Project) {
        projectsRepository.addProject(project)
    }

    fun updateProject (project: Project) {
        projectsRepository.updateProject(project)
    }

    fun deleteProjects () {
        projectsToRestore.clear()
        projectsToRestore.addAll(projectsToDeleteList)
        for (project in projectsToRestore) projectsRepository.deleteProject(project)
        selectMode.value = false

        if (projectsToRestore.size == 1) {
            _snackbar.value = projectsToRestore.get(0).name + " deleted"
            viewModelScope.launch {
                delay(5000)
               _snackbar.value = null
            }
        }
        else {
            _snackbar.value = projectsToRestore.size.toString() + " projects deleted"
            viewModelScope.launch {
                delay(5000)
                _snackbar.value = null
            }
        }
    }

    fun deleteProject (project: Project) {
        projectsToRestore.clear()
        projectsToRestore.add(project)
        projectsRepository.deleteProject(project)
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

    fun loadAccentColor(): Int {
        return projectsRepository.loadAccentColorFromStorage(getApplication<Application>().applicationContext)
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


    enum class SortParam {BY_NAME, BY_DATE_ADDED}
    enum class OrderParam {ASCENDING, DESCENDING}

}

