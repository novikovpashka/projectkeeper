package com.novikovpashka.projectkeeper.extensions

import android.content.DialogInterface
import android.content.Intent
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.novikovpashka.projectkeeper.presentation.startactivity.StartActivity

fun MaterialAlertDialogBuilder.logOutDialog(): MaterialAlertDialogBuilder {
    return MaterialAlertDialogBuilder(this.context)
        .setMessage("Log out?")
        .setNegativeButton("Stay") { dialog: DialogInterface, _: Int -> dialog.cancel() }
        .setPositiveButton("Log out") { _: DialogInterface?, _: Int ->
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this.context, StartActivity::class.java)
            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            this.context.startActivity(intent)
        }
}