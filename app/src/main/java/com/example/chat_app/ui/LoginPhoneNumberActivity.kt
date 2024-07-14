package com.example.chat_app.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.chat_app.databinding.ActivityLoginPhoneNumberBinding


class LoginPhoneNumberActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginPhoneNumberBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginPhoneNumberBinding.inflate(layoutInflater).also {
            setContentView(it.root)
        }
        binding.loginProgressBar.setVisibility(View.GONE)
        val countryCodePicker = binding.loginCountrycode
        val phoneInput = binding.loginMobileNumber

        binding.loginCountrycode.registerCarrierNumberEditText(phoneInput)
        binding.sendOtpBtn.setOnClickListener { v ->
            if (!countryCodePicker.isValidFullNumber()) {
                phoneInput.setError("Phone number not valid")
                return@setOnClickListener
            }
            val intent =
                Intent(
                    this,
                    LoginOtpActivity::class.java
                )
            intent.putExtra("phone", countryCodePicker.getFullNumberWithPlus())
            startActivity(intent)
        }
    }
}