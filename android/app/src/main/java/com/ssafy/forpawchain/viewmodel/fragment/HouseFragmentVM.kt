package com.ssafy.forpawchain.viewmodel.fragment

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ssafy.forpawchain.model.domain.SearchResultDTO


class HouseFragmentVM : ViewModel() {
    companion object {
        val TAG: String? = this::class.qualifiedName
    }

    val searchEditText = MutableLiveData<String>()


    //추가 시작
    val todoLiveData = MutableLiveData<List<SearchResultDTO>>() //변경/관찰가능한 List

    //추가 끝
    private val data = arrayListOf<SearchResultDTO>()


    fun addTask(todo: SearchResultDTO) {
        data.add(todo)
        todoLiveData.value = data //todoLiveData를 add된 데이터로 변경
    }

    fun deleteTask(todo: SearchResultDTO) {
        data.remove(todo)
        todoLiveData.value = data //todoLiveData를 remove된 데이터로 변경, 이제 TodoLiveData로 UI값을 변경해줘야한다.
    }

    fun clearTask() {
        data.clear()
        todoLiveData.value = data
    }
}