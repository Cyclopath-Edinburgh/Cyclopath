package com.example.cyclopath.ui.profile

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.example.cyclopath.R
import com.example.cyclopath.ui.login.LoginActivity
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class ProfileFragment : Fragment() {

    private lateinit var name : TextView
    private lateinit var logout : Button
    private var sp : SharedPreferences? = null

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_profile, container, false)

        val lay = root.findViewById<ConstraintLayout>(R.id.constraintLayout)
        lay.foreground.alpha = 0

        sp = context?.getSharedPreferences("user_data", AppCompatActivity.MODE_PRIVATE)

        name = root.findViewById(R.id.username)
        logout = root.findViewById(R.id.logout)

        name.text = sp!!.getString("username","user")

        var loginActivity = LoginActivity()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("311764064674-trb86n5mk72kf3chqmvg2i04jtvj77n0.apps.googleusercontent.com") //you can also use R.string.default_web_client_id
                .requestEmail()
                .build()

        logout.setOnClickListener{
            val inflater = context?.getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val popupView: View = inflater.inflate(R.layout.popup_logout, null)

            val popupWindow = PopupWindow(popupView, 700, 400)

            lay.foreground.alpha = 120

            popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0)

            val yesb = popupWindow.contentView.findViewById<Button>(R.id.logout_yes)
            yesb.setOnClickListener {
                popupWindow.dismiss()
                sp!!.edit().clear().apply()
                FirebaseAuth.getInstance().signOut()
                Firebase.auth.signOut()
                activity?.let { it1 -> GoogleSignIn.getClient(it1,gso).signOut() }

                val intent = Intent(context, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }

            val nob = popupWindow.contentView.findViewById<Button>(R.id.logout_no)
            nob.setOnClickListener {
                popupWindow.dismiss()
                lay.foreground.alpha = 0
            }

        }

        return root

    }
}