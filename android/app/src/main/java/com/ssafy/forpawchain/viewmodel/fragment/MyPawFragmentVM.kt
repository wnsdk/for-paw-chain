package com.ssafy.forpawchain.viewmodel.fragment

import android.graphics.drawable.Drawable
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.ssafy.forpawchain.model.domain.MyPawListDTO
import com.ssafy.forpawchain.model.service.PetService
import com.ssafy.forpawchain.util.ImageLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MyPawFragmentVM : ViewModel() {
    companion object {
        val TAG: String? = this::class.qualifiedName

    }

    //추가 시작
    val todoLiveData = MutableLiveData<List<MyPawListDTO>>() //변경/관찰가능한 List

    //추가 끝
    private val data = arrayListOf<MyPawListDTO>()

    fun addTask(todo: MyPawListDTO) {
        data.add(todo)
        todoLiveData.value = data //todoLiveData를 add된 데이터로 변경
    }

    fun deleteTask(todo: MyPawListDTO) {
        data.remove(todo)
        todoLiveData.value = data //todoLiveData를 remove된 데이터로 변경, 이제 TodoLiveData로 UI값을 변경해줘야한다.
    }

    fun clearTask() {
        data.clear()
        todoLiveData.value = data
    }

    suspend fun initData() {
        val response = withContext(Dispatchers.IO) {
            PetService().getMyPets().enqueue(object :
                Callback<JsonObject> {
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    if (response.isSuccessful) {
                        // 정상적으로 통신이 성고된 경우
                        var result: JsonObject? = response.body()
                        val items = result?.get("content") as JsonArray
                        if (result != null) {
                            for (item in items.asJsonArray) {
                                var item1 = item as JsonObject
                                if (item1["profile"].toString() == "null") {
                                    addTask(
                                        MyPawListDTO(
                                            MutableLiveData<String>(item1["pid"].asString),
                                            null,
                                            MutableLiveData<String>(item1["name"].asString),
                                            MutableLiveData<String>(if (item1["sex"].asString.equals("MALE")) "남아" else "여아"),
                                            MutableLiveData<String>(if (item1["type"].asString.equals("DOG")) "개과" else if (item1["type"].asString.equals("CAT")) "고양이과" else "기타"),
                                            MutableLiveData<String>(item1["kind"].asString),
                                            MutableLiveData<String>(if (item1["spayed"].asString.equals("false")) "Ⅹ" else "○"),
                                        )
                                    )
                                } else {
                                    ImageLoader().loadDrawableFromUrl(item1["profile"].asString) { drawable ->
                                        addTask(
                                            MyPawListDTO(
                                                MutableLiveData<String>(item1["pid"].asString),
                                                MutableLiveData<Drawable>(drawable),
                                                MutableLiveData<String>(item1["name"].asString),
                                                MutableLiveData<String>(if (item1["sex"].asString.equals("MALE")) "남아" else "여아"),
                                                MutableLiveData<String>(if (item1["type"].asString.equals("DOG")) "개과" else if (item1["type"].asString.equals("CAT")) "고양이과" else "기타"),
                                                MutableLiveData<String>(item1["kind"].asString),
                                                MutableLiveData<String>(if (item1["spayed"].asString.equals("false")) "Ⅹ" else "○"),
                                            )
                                        )
                                    }
                                }
                            }
                        }
                        Log.d(PawFragmentVM.TAG, "onResponse 성공: $result");
                    } else {
                        // 통신이 실패한 경우(응답코드 3xx, 4xx 등)
                        Log.d(TAG, "onResponse 실패")
                    }
                }

                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    // 통신 실패 (인터넷 끊킴, 예외 발생 등 시스템적인 이유)
                    Log.d(TAG, "onFailure 에러: " + t.message.toString());
                }
            })
        }
    }
}