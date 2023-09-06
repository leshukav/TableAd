package ru.netology.tablead.dialoghelper

import android.app.Activity
import android.app.AlertDialog
import ru.netology.tablead.databinding.ProgressDialogLayoutBinding


object ProgressDialog {

    fun createDialog(activity: Activity): AlertDialog {

        val builder = AlertDialog.Builder(activity)
        val binding = ProgressDialogLayoutBinding.inflate(activity.layoutInflater)
        val view = binding.root
        builder.setView(view)

        val dialog = builder.create()
        dialog.setCancelable(false)
        dialog.show()
        return dialog
    }
}