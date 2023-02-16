package com.example.cyclopath.ui.login

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import com.example.cyclopath.G
import com.example.cyclopath.MainActivity
import com.example.cyclopath.R
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener {

    var db: FirebaseFirestore? = null
    var sp: SharedPreferences? = null
    private var auth: FirebaseAuth = Firebase.auth
    val RC_SIGN_IN = 1

    private lateinit var usernametext : TextInputEditText
    private lateinit var passwordtext : TextInputEditText
    private lateinit var forgetpassword : TextView
    private lateinit var login : Button
    private lateinit var facebook : Button
    private lateinit var google : Button
    private lateinit var signup : TextView
    private lateinit var userList : ArrayList<Array<String>>
    private lateinit var emailPasswordActivity: EmailPasswordActivity
    private lateinit var bar : ProgressBar
    private var done = false

    private var firebaseAuth: FirebaseAuth? = null
    private var authStateListener: FirebaseAuth.AuthStateListener? = null
    var googleApiClient: GoogleApiClient? = null
    var name: String? = null
    var email: String? = null
    var idToken: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        usernametext = findViewById(R.id.username_input)
        passwordtext = findViewById(R.id.password_input)
        forgetpassword = findViewById(R.id.forget_password)
        login = findViewById(R.id.login)
        facebook = findViewById(R.id.facebook)
        google = findViewById(R.id.google)
        signup = findViewById(R.id.su)
        bar = findViewById(R.id.progressBar)

        if (!isNetworkAvailable(this)) {
            Toast.makeText(this@LoginActivity, "Please connect to the Internet.", Toast.LENGTH_LONG).show()
        }

        userList = ArrayList<Array<String>>()
        emailPasswordActivity = EmailPasswordActivity()
        db = FirebaseFirestore.getInstance()
        getUserData(db!!)
        sp = getSharedPreferences("user_data", MODE_PRIVATE)

        if (sp!!.getString("username","empt") != "empt") {
            val intent = Intent(this@LoginActivity, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        forgetpassword.setOnClickListener{
            val intent = Intent(this@LoginActivity, ForgetPasswordActivity::class.java)
            startActivity(intent)
        }

        login.setOnClickListener{
            if (!isNetworkAvailable(this)) {
                Toast.makeText(this@LoginActivity, "Please connect to the Internet.", Toast.LENGTH_LONG).show()
            } else {
                val username = usernametext.text.toString()
                val password = passwordtext.text.toString()
                if (username.length == 0) {
                    Toast.makeText(applicationContext, "Please insert username.", Toast.LENGTH_SHORT).show()
                } else if (password.length == 0) {
                    Toast.makeText(applicationContext, "Please insert password.", Toast.LENGTH_SHORT).show()
                } else {
                    login(username, password)
                }
            }
        }

        signup.setOnClickListener{
            var intent = Intent(this,SignUpActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        firebaseAuth = FirebaseAuth.getInstance()
        //this is where we start the Auth state Listener to listen for whether the user is signed in or not
        authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            // Get signedIn user
            val user = firebaseAuth.currentUser
            //if user is signed in, we call a helper method to save the user details to Firebase
            if (user != null) {

            } else {
            }
        }
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.webclientID)) //you can also use R.string.default_web_client_id
                .requestEmail()
                .build()
        googleApiClient = GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build()
        google!!.setOnClickListener(View.OnClickListener {
            val intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient!!)
            startActivityForResult(intent, RC_SIGN_IN)
        })

    }

    fun login(username: String?, password: String) {
        val docRef = db!!.collection("users").document(username!!)
        docRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document.exists()) {
                    emailPasswordActivity.signAccount(document.get("email").toString(), password)
                    bar.visibility = View.VISIBLE
                    Handler().postDelayed({
                        bar.visibility = View.INVISIBLE
                        done = emailPasswordActivity.getLogin()
                        if (done) {
                            val user = Firebase.auth.currentUser
                            user!!.reload()
                            if (user.isEmailVerified()) {
                                Toast.makeText(applicationContext, "Successfully login!", Toast.LENGTH_SHORT).show()
                                sp!!.edit().putString("username", username).apply()
                                sp!!.edit().putString("email", document.get("email").toString()).apply()
                                val intent = Intent(this, MainActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                            } else {
                                Toast.makeText(applicationContext, "Please activate your email address.", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this@LoginActivity, "Password incorrect.", Toast.LENGTH_SHORT).show()
                        }
                    }, 3000)
                } else {
                    Toast.makeText(applicationContext, "Username not found.", Toast.LENGTH_SHORT).show()
                }
            }
        }
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

    override fun onConnectionFailed(@NonNull connectionResult: ConnectionResult) {}
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data!!)
            handleSignInResult(result)
        }
    }

    private fun handleSignInResult(result: GoogleSignInResult?) {
        if (result!!.isSuccess) {
            val account = result.signInAccount
            idToken = account!!.idToken
            name = account.displayName
            email = account.email
            val docRef = db!!.collection("users").document(name!!)
            docRef.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    if (!document.exists()) {
                        val user: MutableMap<String, Any> = HashMap()
                        user["email"] = email!!
                        db!!.collection("users").document(name!!)
                                .set(user)
                                .addOnSuccessListener {
                                    G.user.name = name
                                    G.user.isLoggedIn
                                }
                                .addOnFailureListener { }
                    }
                    sp!!.edit().putString("username", name).apply()
                    sp!!.edit().putString("email", email).apply()
                    val credential = GoogleAuthProvider.getCredential(idToken, null)
                    firebaseAuthWithGoogle(credential)
                }
            }
        } else {
            // Google Sign In failed, update UI appropriately
            Toast.makeText(this, "Login Unsuccessful", Toast.LENGTH_SHORT).show()
        }
    }

    private fun firebaseAuthWithGoogle(credential: AuthCredential) {
        firebaseAuth!!.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        if (!googleApiClient!!.isConnected) {
                            googleApiClient!!.connect()
                        }
                        Toast.makeText(applicationContext, "Successfully login!", Toast.LENGTH_SHORT).show()
                        gotoMain()
                    } else {
                        task.exception!!.printStackTrace()
                        Toast.makeText(this@LoginActivity, "Authentication failed.",
                                Toast.LENGTH_SHORT).show()
                    }
                }
    }

    private fun gotoMain() {
        val intent = Intent(this@LoginActivity, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onStart() {
        super.onStart()
        if (authStateListener != null) {
            FirebaseAuth.getInstance().signOut()
        }
        firebaseAuth!!.addAuthStateListener(authStateListener!!)
    }

    override fun onStop() {
        super.onStop()
        if (authStateListener != null) {
            firebaseAuth!!.removeAuthStateListener(authStateListener!!)
        }
    }

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        return connectivityManager.activeNetworkInfo != null && connectivityManager.activeNetworkInfo!!.isConnected
    }

//    TODO, facebook
}