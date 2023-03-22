package com.ssafy.forpawchain.behind.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.ssafy.basictemplate.util.ActivityCode
import com.ssafy.basictemplate.util.eventObserve
import com.ssafy.forpawchain.databinding.FragmentCreateDoctorHistoryBinding
import com.ssafy.forpawchain.viewmodel.fragment.CreateDoctorHistoryFragmentVM


class CreateDoctorHistoryFragment : Fragment() {
    private lateinit var viewModel: CreateDoctorHistoryFragmentVM
    private var _binding: FragmentCreateDoctorHistoryBinding? = null
    private lateinit var navController: NavController

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    companion object {
        val TAG: String? = this::class.qualifiedName

        // 갤러리 권한 요청
        const val OPEN_GALLERY = 1

    }


    @SuppressLint("ResourceAsColor")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateDoctorHistoryBinding.inflate(inflater, container, false)
        activity?.let {
            viewModel = ViewModelProvider(it).get(CreateDoctorHistoryFragmentVM::class.java)
            binding.viewModel = viewModel
            binding.lifecycleOwner = this
        }

        val root: View = binding.root

        binding.imageView.setOnClickListener() {
            // 이미지 선택을 위한 Intent 생성
            // 이미지 선택을 위한 Intent 생성
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*" // 이미지 파일만 선택 가능하도록 설정


            // 이미지 선택 액티비티 실행

            // 이미지 선택 액티비티 실행
            startActivityForResult(intent, OPEN_GALLERY)
        }

        initObserve()

        return root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == OPEN_GALLERY) {
                var currentImageUrl: Uri? = data?.data
                try {
                    val bitmap = MediaStore.Images.Media.getBitmap(
                        requireContext().contentResolver,
                        currentImageUrl
                    )
                    viewModel.path.postValue(currentImageUrl.toString())
                    Log.d(TAG, "이미지 불러오기 완료$currentImageUrl")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun initObserve() {
        viewModel.openEvent.eventObserve(this) { obj ->

            when (obj) {
                ActivityCode.DONE -> {
                    navController.navigateUp()
                }
                else -> {
                    null
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(requireView())

    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}