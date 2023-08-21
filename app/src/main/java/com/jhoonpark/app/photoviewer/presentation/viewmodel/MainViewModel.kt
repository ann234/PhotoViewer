package com.jhoonpark.app.photoviewer.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jhoonpark.app.photoviewer.domain.model.PhotoDataModel
import com.jhoonpark.app.photoviewer.domain.repository.PhotoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val photoRepository: PhotoRepository,
): ViewModel() {
    companion object {
        const val TAG = "MainViewModel"
        const val RESTORE_READBUTTON_TIME_MILLS = 3000L
    }

    enum class MainState {
        IDLE,
        READING,
        SORTING,
        CLEARING,
    }
    var mainState = MainState.IDLE
        private set

    private val _readButtonEnabled = MutableLiveData(true)
    val readButtonEnabled: LiveData<Boolean> get() = _readButtonEnabled

    private val _photoList: MutableLiveData<List<PhotoDataModel>> = MutableLiveData()
    val photoList: LiveData<List<PhotoDataModel>> get() = _photoList

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    init {
        _photoList.value = mutableListOf()
    }

    private var updatePhotoListJob: Job? = null
    suspend fun updatePhotoList() {
        if(mainState == MainState.IDLE) {
            mainState = MainState.READING
            try {
                updatePhotoListJob = viewModelScope.launch {
                    photoRepository.getPhotosList()
                        .onSuccess { newPhotoList ->
                            val oldList = _photoList.value ?: emptyList()
                            val newList = oldList + newPhotoList

                            _photoList.value = newList
                        }
                        .onFailure {
                            _errorMessage.value = "데이터를 불러오는데 실패했습니다! 다시 시도해주세요."
                            Log.e(TAG, "Failed to fetch data: $it")
                        }
                }
            } finally {
                mainState = MainState.IDLE
            }
        }
    }

    suspend fun clearPhotoList() {
        if(mainState == MainState.IDLE || mainState == MainState.READING) {
            //  Clear 버튼 클릭 후 최소 3초 간 Read button 비활성화
            updatePhotoListJob?.cancel()
            val startTime = System.currentTimeMillis()
            _readButtonEnabled.value = false

            mainState = MainState.CLEARING

            try {
                _photoList.value = emptyList()
            } finally {
                mainState = MainState.IDLE
            }

            //  실행 후 3초가 지난 후 ReadButton을 재활성화
            var remainTime = System.currentTimeMillis() - startTime
            while(remainTime < RESTORE_READBUTTON_TIME_MILLS) {
                delay(100L)
                remainTime = System.currentTimeMillis() - startTime
            }
            _readButtonEnabled.value = true
        }
    }

    fun sortPhotoList() = viewModelScope.launch  {
        if(mainState == MainState.IDLE) {
            mainState = MainState.SORTING
            try {
                _photoList.value?.let { photoList ->
                    val sortedList = photoList.toMutableList()
                    sortedList.sortBy {
                        it.title
                    }
                    _photoList.value = sortedList
                }
            } finally {
                mainState = MainState.IDLE
            }
        }
    }
}