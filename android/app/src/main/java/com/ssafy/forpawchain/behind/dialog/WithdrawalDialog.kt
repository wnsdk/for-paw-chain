package com.ssafy.forpawchain.behind.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.ssafy.forpawchain.databinding.DialogPermissionDeleteBinding
import com.ssafy.forpawchain.databinding.DialogWithdrawalBinding
import com.ssafy.forpawchain.model.interfaces.IPermissionDelete

class WithdrawalDialog(context: Context, dialogInterface: IPermissionDelete) :
    Dialog(context) {
    private var mBinding: DialogWithdrawalBinding? = null
    private val binding get() = mBinding!!

    private var dialogInterface: IPermissionDelete? = null

    init {
        this.dialogInterface = dialogInterface
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DialogWithdrawalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 배경 투명하게
//        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        binding.deleteBtn.setOnClickListener {
            this.dialogInterface?.onDeleteBtnClick()
            dismiss()

        }
        binding.cancelBtn.setOnClickListener {
            dismiss()
        }
    }
}