package com.example.colortooth.ui

import androidx.lifecycle.*
import com.example.colortooth.data.ClickData
import com.example.colortooth.data.ClickRepository
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException

class MainViewModel(private val repository: ClickRepository) : ViewModel() {

    val allCLicks: LiveData<List<ClickData>> = repository.allClicks.asLiveData()

    var msgCount: Int = 0

    fun insert(entry: ClickData) = viewModelScope.launch {
        repository.insert(entry)
    }

    fun delete() = viewModelScope.launch {
        repository.delete()
    }

    fun count() = viewModelScope.launch {
        msgCount = repository.count()
    }


}

class MainViewModelFactory(private val repository: ClickRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}