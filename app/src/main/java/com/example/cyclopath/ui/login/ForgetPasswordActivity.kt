package com.example.cyclopath.ui.login

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.Button
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.example.cyclopath.G
import com.example.cyclopath.R
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class ForgetPasswordActivity : AppCompatActivity() {

    private lateinit var email : TextInputEditText
    private lateinit var send : Button
    private lateinit var emailPasswordActivity: EmailPasswordActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forget_password)

        email = findViewById(R.id.email_input)
        send = findViewById(R.id.send)
        emailPasswordActivity = EmailPasswordActivity()

        send.setOnClickListener{
            emailPasswordActivity.resetPassword(email.text.toString())
            Handler().postDelayed({
                if (emailPasswordActivity.getValid()) {
                        Toast.makeText(this@ForgetPasswordActivity, "A reset password email has been sent.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@ForgetPasswordActivity, "Email address does not exists.", Toast.LENGTH_SHORT).show()
                    }

            }, 1000)
        }

        var callback: OnBackPressedCallback = object : OnBackPressedCallback(true /* enabled by default */) {
            override fun handleOnBackPressed() {
                val intent = Intent(this@ForgetPasswordActivity, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)
    }
}