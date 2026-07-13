package com.example.features.student.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.example.core.network.GenericPagingSource
import com.example.core.repository.StudentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class Notice(
    val id: String,
    val title: String,
    val content: String,
    val date: String,
    val category: String
)

class NoticeViewModel(
    private val studentRepository: StudentRepository
) : ViewModel() {

    val noticesFlow: Flow<PagingData<Notice>> = Pager(
        config = PagingConfig(pageSize = 20, enablePlaceholders = false),
        pagingSourceFactory = { GenericPagingSource({ skip, limit -> studentRepository.getNoticesPaged(skip, limit) }) }
    ).flow
        .map { pagingData ->
            pagingData.map { dto ->
                Notice(
                    id = dto.id,
                    title = dto.title,
                    content = dto.body,
                    date = dto.date ?: "",
                    category = dto.category ?: ""
                )
            }
        }
        .cachedIn(viewModelScope)
}

class NoticeViewModelFactory(
    private val studentRepository: StudentRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NoticeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NoticeViewModel(studentRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

