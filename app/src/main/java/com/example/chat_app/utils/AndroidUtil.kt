package com.example.chat_app.utils

import android.content.Context
import android.content.Intent
import android.widget.Toast


fun showToast(context: Context?, message: String?) {
    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
}