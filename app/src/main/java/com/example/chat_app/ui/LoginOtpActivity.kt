package com.example.chat_app.ui

import android.R.attr.phoneNumber
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.chat_app.databinding.ActivityLoginOtpBinding
import com.example.chat_app.utils.showToast
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.TimeUnit


class LoginOtpActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginOtpBinding
    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private lateinit var verificationCode: String
    private lateinit var resendingToken: PhoneAuthProvider.ForceResendingToken
    var timeoutSeconds: Long = 60L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginOtpBinding.inflate(layoutInflater).also {
            setContentView(it.root)
        }

        val otpInput = binding.loginOtp
        val nextBtn = binding.loginNextBtn
        val resendOtpTextView = binding.resendOtpTextview

        val phoneNumber = intent.extras!!.getString("phone")

        sendOtp(phoneNumber, false)

        nextBtn.setOnClickListener {
            val enteredOtp: String = otpInput.getText().toString()
            val credential =
                PhoneAuthProvider.getCredential(verificationCode,enteredOtp)
            signIn(credential)
        }

        resendOtpTextView.setOnClickListener { v ->
            sendOtp(phoneNumber, true)
        }
    }

    private fun sendOtp(phoneNumber: String?, isResend: Boolean) {
        startResendTimer()
        setInProgress(true)
        val builder =
            PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(phoneNumber!!)
                .setTimeout(timeoutSeconds, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(object : OnVerificationStateChangedCallbacks() {
                    override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                        signIn(phoneAuthCredential)
                        setInProgress(false)
                    }

                    override fun onVerificationFailed(e: FirebaseException) {
                        showToast(applicationContext, "OTP verification failed")
                        setInProgress(false)
                    }

                    override fun onCodeSent(s: String, forceResendingToken: ForceResendingToken) {
                        super.onCodeSent(s, forceResendingToken)
                        verificationCode = s
                        resendingToken = forceResendingToken
                        showToast(applicationContext, "OTP sent successfully")
                        setInProgress(false)
                    }
                })
        if (isResend) {
            PhoneAuthProvider.verifyPhoneNumber(
                builder.setForceResendingToken(resendingToken).build()
            )
        } else {
            PhoneAuthProvider.verifyPhoneNumber(builder.build())
        }
    }

    fun setInProgress(inProgress: Boolean) {
        if (inProgress) {
            binding.loginProgressBar.visibility = View.VISIBLE
            binding.loginNextBtn.visibility = View.GONE
        } else {
            binding.loginProgressBar.visibility = View.GONE
            binding.loginNextBtn.visibility = View.VISIBLE
        }
    }

    fun signIn(phoneAuthCredential: PhoneAuthCredential?) {
        //login and go to next activity
        setInProgress(true)
        mAuth.signInWithCredential(phoneAuthCredential!!).addOnCompleteListener { task ->
            setInProgress(false)
            if (task.isSuccessful) {
                val intent = Intent(
                    this@LoginOtpActivity,
                    LoginPhoneNumberActivity::class.java
                )
                intent.putExtra("phone", phoneNumber)
                startActivity(intent)
            } else {
                showToast(applicationContext, "OTP verification failed")
            }
        }
    }

    private fun startResendTimer() {
        val resendOtpTextView = binding.resendOtpTextview
        resendOtpTextView.setEnabled(false)
        val timer = Timer()
        timer.schedule(object : TimerTask() {
            @SuppressLint("SetTextI18n")
            override fun run() {
                timeoutSeconds--
                resendOtpTextView.text = "Resend OTP in $timeoutSeconds seconds"
                if (timeoutSeconds <= 0) {
                    timeoutSeconds = 60L
                    timer.cancel()
                    runOnUiThread {
                        resendOtpTextView.setEnabled(true)
                    }
                }
            }
        }, 0, 1000)
    }
}