package com.example.cyclopath.ui

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.cyclopath.R
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStreamReader

class TncActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tnc)

        val tnc = findViewById<TextView>(R.id.tnc)

        try {
            val inputStreamReader = InputStreamReader(assets.open("tnc.txt"))
            val bufferedReader = BufferedReader(inputStreamReader)
            var receiveString: String? = ""
            val stringBuilder = StringBuilder()
            while (bufferedReader.readLine().also { receiveString = it } != null) {
                stringBuilder.append("\n").append(receiveString)
            }
            assets.open("tnc.txt").close()
            tnc.setText(stringBuilder.toString())
        } catch (e: FileNotFoundException) {
        } catch (e: IOException) {
        }
    }
}