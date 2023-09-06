package ru.netology.tablead.dialoghelper

import android.app.AlertDialog
import android.view.View
import android.widget.Toast
import ru.netology.tablead.MainActivity
import ru.netology.tablead.R
import ru.netology.tablead.accounthelper.AccountHelper
import ru.netology.tablead.databinding.SignDialogBinding


class Dialoghelper(act: MainActivity) {
    private val activity = act
    val accounthelper = AccountHelper(activity)

    fun createSignDialog(index: Int) {
        val builder = AlertDialog.Builder(activity)
        val binding = SignDialogBinding.inflate(activity.layoutInflater)
        val view = binding.root
        builder.setView(view)

        setDialogState(index, binding)

        val dialog = builder.create()

        binding.btSignUpIn.setOnClickListener {
           setOnClickSignUpIn(index, binding, dialog)
        }
        binding.btForgetPassword.setOnClickListener {
            setOnClickResetPassword(binding, dialog)
        }

        binding.button.setOnClickListener {
            accounthelper.signWithGoogle()
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun setOnClickResetPassword(binding: SignDialogBinding, dialog: AlertDialog?) {
       if (binding.edSignEmail.text.isNotEmpty()) {
           activity.myAuth.sendPasswordResetEmail(binding.edSignEmail.text.toString()).addOnCompleteListener {task ->
               if (task.isSuccessful)  {
                   Toast.makeText(activity, activity.resources.getString(R.string.reset_password), Toast.LENGTH_LONG).show()
               }
           }
           dialog?.dismiss()
       } else {
           binding.tvDialogMessage.text = activity.resources.getString(R.string.reset_password_message)
           binding.tvDialogMessage.visibility = View.VISIBLE
       }
    }

    private fun setOnClickSignUpIn(index: Int, binding: SignDialogBinding, dialog: AlertDialog?) {
        dialog?.dismiss()
        if (index == DialogConst.SIGN_UP_STATE) {
            accounthelper.sinUpWithEmail(
                binding.edSignEmail.text.toString(),
                binding.edSignPassword.text.toString()
            )
        } else {
            accounthelper.sinInWithEmail(
                binding.edSignEmail.text.toString(),
                binding.edSignPassword.text.toString()
            )
        }
    }

    private fun setDialogState(index: Int, binding: SignDialogBinding) {
        if (index == DialogConst.SIGN_UP_STATE) {
            binding.tvSignTitle.text = activity.resources.getString(R.string.ac_sign_up)
            binding.btSignUpIn.text = activity.resources.getString(R.string.sign_up_action)
        } else {
            binding.tvSignTitle.text = activity.resources.getString(R.string.ac_sign_in)
            binding.btSignUpIn.text = activity.resources.getString(R.string.sign_in_action)
            binding.btForgetPassword.visibility = View.VISIBLE
        }
    }
}