package com.example.core.network

import androidx.paging.PagingSource
import androidx.paging.PagingState
import retrofit2.Response

class GenericPagingSource<T : Any>(
    private val apiCall: suspend (skip: Int, limit: Int) -> Response<List<T>>,
    private val pageSize: Int = 20
) : PagingSource<Int, T>() {

    override fun getRefreshKey(state: PagingState<Int, T>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(pageSize) ?: anchorPage?.nextKey?.minus(pageSize)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> {
        return try {
            val skip = params.key ?: 0
            val loadSize = params.loadSize
            
            val response = apiCall(skip, loadSize)
            
            if (response.isSuccessful) {
                val data = response.body() ?: emptyList()
                LoadResult.Page(
                    data = data,
                    prevKey = if (skip == 0) null else maxOf(0, skip - loadSize),
                    nextKey = if (data.isEmpty() || data.size < loadSize) null else skip + loadSize
                )
            } else {
                LoadResult.Error(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}
