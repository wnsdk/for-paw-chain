package com.ssafy.forpawchain.behind.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.ssafy.forpawchain.R
import com.ssafy.forpawchain.behind.dialog.AdopteeSetDialog
import com.ssafy.forpawchain.behind.dialog.PermissionDialog
import com.ssafy.forpawchain.behind.dialog.PermissionSetDialog
import com.ssafy.forpawchain.databinding.FragmentPermissionPawBinding
import com.ssafy.forpawchain.model.domain.MyPawListDTO
import com.ssafy.forpawchain.model.domain.PermissionUserDTO
import com.ssafy.forpawchain.model.interfaces.IPermissionDelete
import com.ssafy.forpawchain.viewmodel.adapter.PermissionPawListAdapter
import com.ssafy.forpawchain.viewmodel.fragment.PermissionPawFragmentVM
import kotlinx.coroutines.launch


class PermissionPawFragment : Fragment() {
    private lateinit var viewModel: PermissionPawFragmentVM
    private var _binding: FragmentPermissionPawBinding? = null
    private lateinit var navController: NavController

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    companion object {
        val TAG: String? = this::class.qualifiedName
    }

    @SuppressLint("ResourceAsColor")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPermissionPawBinding.inflate(inflater, container, false)
        activity?.let {
            viewModel = ViewModelProvider(it).get(PermissionPawFragmentVM::class.java)
            binding.viewModel = viewModel
            binding.lifecycleOwner = this
        }

        val bundle = arguments

        lifecycleScope.launch {
            bundle?.getSerializable("item")?.let {
                val item = it as (MyPawListDTO)
                viewModel.name.postValue(item.name.value)
                viewModel.code.postValue("#" + item.code.value.toString())

                item.code.value?.let { it1 -> viewModel.initData(it1) }
//                viewModel.
            }
        }
        binding.floatingBtn.setOnClickListener { view ->
            val dialog = PermissionSetDialog(requireContext(), object : IPermissionDelete {
                override fun onDeleteBtnClick() {
                    Log.d(TAG, "set 권한 부여")
                }
            })

            dialog.show()

            Log.d(PawFragment.TAG, "열람 권한 부여")
        }

        binding.adoptBtn.setOnClickListener { view ->
            val dialog = AdopteeSetDialog(requireContext(), object : IPermissionDelete {
                override fun onDeleteBtnClick() {
                    Log.d(TAG, "권한 양도")
                }
            })

            dialog.show()

        }

        val recyclerView = binding.recycler
        val searchList = mutableListOf<PermissionUserDTO>()

        recyclerView.adapter = PermissionPawListAdapter(
            searchList
        ) {
            // del
            val dialog = PermissionDialog(requireContext(), object : IPermissionDelete {
                override fun onDeleteBtnClick() {
                    viewModel.deleteTask(it)
                }
            })

            dialog.show()
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.setHasFixedSize(true)

//        viewModel.addTask(
//            PermissionUserDTO(
//                resources.getDrawable(R.drawable.icon_default),
//                "김아무개",
//                "#123421"
//            )
//        )
//
//        viewModel.addTask(
//            PermissionUserDTO(
//                resources.getDrawable(R.drawable.icon_default),
//                "홍길동",
//                "#543532"
//            )
//        )
//
//        viewModel.addTask(
//            PermissionUserDTO(
//                resources.getDrawable(R.drawable.icon_default),
//                "사용자",
//                "#000000"
//            )
//        )
//
//        viewModel.addTask(
//            PermissionUserDTO(
//                resources.getDrawable(R.drawable.icon_default),
//                "최진우",
//                "#123432"
//            )
//        )

        viewModel.todoLiveData.observe(
            requireActivity()
        ) { //viewmodel에서 만든 변경관찰 가능한todoLiveData를 가져온다.
            (binding.recycler.adapter as PermissionPawListAdapter).setData(it) //setData함수는 TodoAdapter에서 추가하겠습니다.

        }


        val root: View = binding.root
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
