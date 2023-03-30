package com.example.cyclopath.ui.login

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.cyclopath.G
import com.example.cyclopath.R
import com.example.cyclopath.ui.MainActivity
import com.example.cyclopath.ui.TncActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*


class SignUpActivity : AppCompatActivity() {

    var db: FirebaseFirestore? = null
    var sp: SharedPreferences? = null
    private lateinit var mAuthListener : AuthStateListener

    private lateinit var usernametext : TextInputEditText
    private lateinit var emailtext : TextInputEditText
    private lateinit var passwordtext : TextInputEditText
    private lateinit var password2text : TextInputEditText
    private lateinit var checkbox : CheckBox
    private lateinit var submit : Button
    private lateinit var login : Button
    private lateinit var resend : Button
    private lateinit var userList : ArrayList<Array<String>>
    private lateinit var emailPasswordActivity: EmailPasswordActivity
    private lateinit var bar : ProgressBar
    private lateinit var info : ImageView
    private var signed = false
    private var done = false

    private lateinit var username: String
    private lateinit var email: String
    private lateinit var password: String
    private lateinit var today: String

    var storage = Firebase.storage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        if (!isNetworkAvailable(this)) {
            Toast.makeText(this@SignUpActivity, "Please connect to the Internet.", Toast.LENGTH_LONG).show()
        }

        userList = ArrayList<Array<String>>()
        emailPasswordActivity = EmailPasswordActivity()
        db = FirebaseFirestore.getInstance()
        getUserData(db!!)
        sp = getSharedPreferences("user_data", MODE_PRIVATE)

        val lay = findViewById<ConstraintLayout>(R.id.constraintLayout)
        lay.foreground.alpha = 0

        usernametext = findViewById(R.id.username_input)
        emailtext = findViewById(R.id.email_input)
        passwordtext = findViewById(R.id.password_input)
        password2text = findViewById(R.id.password2_input)
        checkbox = findViewById(R.id.checkbox)
        submit = findViewById(R.id.signup)
        login = findViewById(R.id.login)
        resend = findViewById(R.id.resend)
        bar = findViewById(R.id.progressBar)
        info = findViewById(R.id.info)

        submit.setOnClickListener{
            if (!isNetworkAvailable(this)) {
                Toast.makeText(this@SignUpActivity, "Please connect to the Internet.", Toast.LENGTH_LONG).show()
            } else {
                val username = usernametext.text.toString()
                val email = emailtext.text.toString()
                val password = passwordtext.text.toString()
                val password2 = password2text.text.toString()
                if ((username.length < 6) or (username.length > 20)) {
                    Toast.makeText(this, "Username should be 6-20 characters.", Toast.LENGTH_SHORT).show()
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(this, "The email address is invalid.", Toast.LENGTH_SHORT).show()
                } else if ((password.length < 6) or (password.length > 20)) {
                    Toast.makeText(this, "Password should be 6-20 characters.", Toast.LENGTH_SHORT).show()
                } else if (!password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#\$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?])[a-zA-Z0-9!@#\$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]+$".toRegex())) {
                    Toast.makeText(this, "Password must contains at least 1 uppercase letter, 1 lowercase letter, 1 number and 1 symbol.", Toast.LENGTH_LONG).show()
                } else if (password2 != password) {
                    Toast.makeText(this, "The password and confirm password do not match.", Toast.LENGTH_SHORT).show()
                } else if (!checkbox.isChecked) {
                    Toast.makeText(this, "Please agree to the terms and conditions.", Toast.LENGTH_SHORT).show()
                } else if (!checkUsername(username)) {
                    Toast.makeText(this, "The username has been used.", Toast.LENGTH_SHORT).show()
                } else if (!checkEmail(email)) {
                    Toast.makeText(this, "The email has been used.", Toast.LENGTH_SHORT).show()
                } else {
                    this.username = username
                    this.email = email
                    this.password = password
                    signup(username,email,password)
                }
            }
        }

        login.setOnClickListener {
            val user = Firebase.auth.currentUser
            user!!.reload()
            bar.visibility = View.VISIBLE
            Handler().postDelayed({
                if (user!!.isEmailVerified) {
                    Toast.makeText(this, "Directing to Main Page.", Toast.LENGTH_SHORT).show()
                    Handler().postDelayed({
                        sp!!.edit().putString("username", username).apply()
                        sp!!.edit().putString("email", email).apply()
                        sp!!.edit().putString("startdate",today).apply()
                        val intent = Intent(this, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }, 1000)
                } else {
                    bar.visibility = View.INVISIBLE
                    Toast.makeText(applicationContext, "Please activate your email address.", Toast.LENGTH_SHORT).show()
                }
            }, 1000)

        }

        resend.setOnClickListener{
            emailPasswordActivity.sendVerificationEmail(emailtext.text.toString())
            Toast.makeText(this@SignUpActivity, "An activation email has been sent to your email address.", Toast.LENGTH_SHORT).show()

        }

//        var callback: OnBackPressedCallback = object : OnBackPressedCallback(true /* enabled by default */) {
//            override fun handleOnBackPressed() {
//                val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
//                val popupView: View = inflater.inflate(R.layout.popup_signup, null)
//
//                val popupWindow = PopupWindow(popupView, 700, 400)
//
//                lay.foreground.alpha = 120
//
//                popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0)
//
//                val yesb = popupWindow.contentView.findViewById<Button>(R.id.exit_yes)
//                yesb.setOnClickListener {
//                    popupWindow.dismiss()
//                    val intent = Intent(this@SignUpActivity, LoginActivity::class.java)
//                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//                    startActivity(intent)
//                }
//
//                val nob = popupWindow.contentView.findViewById<Button>(R.id.exit_no)
//                nob.setOnClickListener {
//                    popupWindow.dismiss()
//                    lay.foreground.alpha = 0
//                }
//            }
//        }
//        onBackPressedDispatcher.addCallback(this, callback)

        info.setOnClickListener {
            val intent = Intent(this@SignUpActivity, TncActivity::class.java)
            startActivity(intent)
        }
    }

    fun checkUsername(username: String): Boolean {
        for (i in userList.indices) {
            if (userList.get(i).get(0) == username) {
                return false
            }
        }
        return true
    }

    fun checkEmail(email: String): Boolean {
        for (i in userList.indices) {
            if (userList.get(i).get(1) == email) {
                return false
            }
        }
        return true
    }

    fun signup(username : String?, email: String, password: String) {
        emailPasswordActivity.createAccount(username, email, password)
        signed = true
        bar.visibility = View.VISIBLE
        Handler().postDelayed({
            done = emailPasswordActivity.getSuccess()
            if (done) {
                emailPasswordActivity.sendVerificationEmail(email)
                resend.visibility = View.VISIBLE
                login.visibility = View.VISIBLE
                submit.visibility = View.INVISIBLE
                bar.visibility = View.INVISIBLE
                Toast.makeText(applicationContext, "Successfully sign up!", Toast.LENGTH_SHORT).show()
                Toast.makeText(applicationContext, "An activation email has been sent to your email address.", Toast.LENGTH_SHORT).show()
                Handler().postDelayed({
                    done = emailPasswordActivity.getSuccess()
                    if (done) {
                        val sdf = SimpleDateFormat("yyyy.MM.dd")
                        val cal = Calendar.getInstance()
                        today = sdf.format(cal.time).toString()
                        val user: MutableMap<String, Any> = HashMap()
                        user["email"] = email
                        user["startdate"] = today
                        db!!.collection("users").document(username!!)
                                .set(user)
                                .addOnSuccessListener {
                                    G.user.name = username
                                    G.user.isLoggedIn
                                }
                        val storageRef = storage.reference
                        val dataRef = storageRef.child("distanceData/$username.txt")
                        val data = ByteArrayOutputStream()
                        data.write("\n".toByteArray())
                        val ddata = data.toByteArray()
                        dataRef.putBytes(ddata)
                    }
                }, 1000)
            } else {
                Toast.makeText(this, "Sign up unsuccessfully. Please try agian.", Toast.LENGTH_SHORT).show()
                bar.visibility = View.INVISIBLE
            }
        }, 3000)
    }

    fun getUserData(db: FirebaseFirestore) {
        val colRef = db.collection("users")
        colRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                for (document in task.result) {
                    val u = arrayOf(document.id!!, document.getString("email")!!)
                    userList.add(u)
                }

            }
        }
    }

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        return connectivityManager.activeNetworkInfo != null && connectivityManager.activeNetworkInfo!!.isConnected
    }

}