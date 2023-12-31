package com.ssafy.forpawchain.behind.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.JsonObject
import com.ssafy.forpawchain.R
import com.ssafy.forpawchain.databinding.FragmentDiagnosisDetailBinding
import com.ssafy.forpawchain.model.domain.DianosisNewDTO
import com.ssafy.forpawchain.model.domain.HistoryDTO
import com.ssafy.forpawchain.model.service.AdoptService
import com.ssafy.forpawchain.model.service.IpfsService
import com.ssafy.forpawchain.util.ImageLoader
import com.ssafy.forpawchain.viewmodel.adapter.DiagnosisDetailRecyclerViewAdapter
import com.ssafy.forpawchain.viewmodel.adapter.DiagnosisNewRecyclerViewAdapter
import com.ssafy.forpawchain.viewmodel.adapter.DiagnosisRecyclerViewAdapter
import com.ssafy.forpawchain.viewmodel.fragment.AdoptViewFragmentVM
import com.ssafy.forpawchain.viewmodel.fragment.DiagnosisDetailFragmentVM
import com.ssafy.forpawchain.viewmodel.fragment.PawFragmentVM
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DiagnosisDetailFragment : Fragment() {
    private var _binding: FragmentDiagnosisDetailBinding? = null
    private lateinit var viewModel: DiagnosisDetailFragmentVM
    private lateinit var navController: NavController

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    companion object {
        val TAG: String? = this::class.qualifiedName
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDiagnosisDetailBinding.inflate(inflater, container, false)
        activity?.let {
            viewModel = ViewModelProvider(it).get(DiagnosisDetailFragmentVM::class.java)
            binding.viewModel = viewModel
            binding.lifecycleOwner = this
        }

        val root: View = binding.root
        val recyclerView = binding.recycler

        recyclerView.adapter = DiagnosisDetailRecyclerViewAdapter()
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.setHasFixedSize(true)


        viewModel.todoLiveData.observe(
            requireActivity()
        ) { //viewmodel에서 만든 변경관찰 가능한todoLiveData를 가져온다.
            (binding.recycler.adapter as DiagnosisDetailRecyclerViewAdapter).setData(it) //setData함수는 TodoAdapter에서 추가하겠습니다.

        }


        val bundle = arguments
        if (bundle != null && bundle.getSerializable("item") != null) {
            val value = bundle.getSerializable("item") as HistoryDTO // bundle 로 의료기록 불러오기
            viewModel.title.postValue(value.title)
            viewModel.body.postValue(value.body)
            viewModel.date.postValue(value.date)
            viewModel.name.postValue(value.writer)
            for (item in value.extra) {
                viewModel.addTask(
                    DianosisNewDTO(
                        MutableLiveData(item.title),
                        MutableLiveData(item.body)
                    )
                )
            }

            lifecycleScope.launch {
                withContext(Dispatchers.IO) {

                    ImageLoader().loadDrawableFromUrl("http://j8a207.p.ssafy.io:8080/api/file/${value.hash}") { drawable ->
                        viewModel.image.postValue(drawable)
                    }
                }
            }
        }



        if (bundle != null && bundle.getSerializable("code") != null) {
            val code = bundle.getSerializable("code") as String
            viewModel.searchEditText.postValue(code.toString())
        }



        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(requireView())

    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.clearTask()
//        _binding = null
    }
}