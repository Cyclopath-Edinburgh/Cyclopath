package com.example.cyclopath.ui.login

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class EmailPasswordActivity : AppCompatActivity() {

    private var auth: FirebaseAuth = Firebase.auth
    private var success: Boolean? = null
    private var login: Boolean? = null
    private var valid: Boolean? = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if(currentUser != null){
            currentUser.reload()
        }
    }

    fun createAccount(username: String?, email: String?, password: String?) {
        auth.createUserWithEmailAndPassword(email!!, password!!)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        val profileUpdates: UserProfileChangeRequest = UserProfileChangeRequest.Builder()
                                .setDisplayName(username).build()
                        user!!.updateProfile(profileUpdates)
                                .addOnCompleteListener(OnCompleteListener<Void?> { task ->
                                    if (task.isSuccessful) {
                                        success = true
                                    }
                                })
                    } else {
                        success = false
                    }
                }
    }

    fun signAccount(email: String?, password: String?) {
        auth.signInWithEmailAndPassword(email!!, password!!)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        login = true
                    } else {
                        login = false
                    }
                }
    }

    fun sendVerificationEmail(email: String?) {
        auth.currentUser?.sendEmailVerification()?.addOnCompleteListener(OnCompleteListener<Void?> { task ->
            if (task.isSuccessful) {
                success = true
            } else {
                success = false
            }
        })
    }

    fun resetPassword(email: String?) {
        auth.sendPasswordResetEmail(email!!)
                .addOnCompleteListener(OnCompleteListener<Void?> { task ->
                    if (task.isSuccessful) {
                        valid = true
                    }
                })
    }

    fun getSuccess(): Boolean {
        return success!!
    }

    fun getLogin(): Boolean {
        return login!!
    }

    fun getValid(): Boolean {
        return valid!!
    }



//    companion object {
//        private const val TAG = "hello"
//        private var success: Boolean? = null
//        private var login: Boolean? = null
//        private var mAuth: FirebaseAuth = FirebaseAuth.getInstance()
//        fun createAccount(username: String?, email: String?, password: String?) {
//            mAuth.createUserWithEmailAndPassword(email, password)
//                    .addOnCompleteListener(OnCompleteListener<Any?> { task ->
//                        if (task.isSuccessful) {
//                            // Sign in success, update UI with the signed-in user's information
//                            Log.d(TAG, "createUserWithEmail:success")
//                            val user: FirebaseUser = mAuth.getCurrentUser()
//                            val profileUpdates: UserProfileChangeRequest = Builder()
//                                    .setDisplayName(username).build()
//                            user.updateProfile(profileUpdates)
//                                    .addOnCompleteListener(OnCompleteListener<Void?> { task ->
//                                        if (task.isSuccessful) {
//                                            Log.d(TAG, "User profile updated.")
//                                            success = true
//                                        }
//                                    })
//                            //updateUI(user);
//                        } else {
//                            // If sign in fails, display a message to the user.
//                            Log.w(TAG, "createUserWithEmail:failure")
//                            success = false
//                            //Toast.makeText(EmailPasswordActivity.this, "Authentication failed.",
//                            //      Toast.LENGTH_SHORT).show();
//                            //updateUI(null);
//                        }
//                    })
//        }

//        fun signAccount(email: String?, password: String?) {
//            mAuth.signInWithEmailAndPassword(email, password)
//                    .addOnCompleteListener(OnCompleteListener<Any?> { task ->
//                        if (task.isSuccessful) {
//                            // Sign in success, update UI with the signed-in user's information
//                            Log.d(TAG, "signInWithEmail:success")
//                            val user: FirebaseUser = mAuth.getCurrentUser()
//                            login = true
//                            //updateUI(user);
//                        } else {
//                            // If sign in fails, display a message to the user.
//                            Log.w(TAG, "signInWithEmail:failure")
//                            login = false
//                            //Toast.makeText(EmailPasswordActivity.this, "Authentication failed.",
//                            //      Toast.LENGTH_SHORT).show();
//                            //updateUI(null);
//                        }
//                    })
//        }




    }
