package com.example.cyclopath.ui.profile

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.cyclopath.R
import com.example.cyclopath.ui.TncActivity
import com.example.cyclopath.ui.login.LoginActivity
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.text.SimpleDateFormat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import java.util.*


class ProfileFragment : Fragment() {

    private lateinit var name : TextView
    private lateinit var logout : Button
    private lateinit var tnc : TextView
    private var sp : SharedPreferences? = null

    private lateinit var currentWeek: TextView
    lateinit var left: ImageView
    lateinit var right: ImageView
    private lateinit var distanceData: LineDataSet
    private lateinit var durationData: LineDataSet
    private lateinit var alldistanceData: LineData
    private lateinit var alldurationData: LineData
    private lateinit var distanceChart: LineChart
    private lateinit var durationChart: LineChart

    val ONE_MEGABYTE: Long = 1024 * 1024
    var storage = Firebase.storage
    var sdf = SimpleDateFormat("yyyy.MM.dd")
    var sdf2 = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
    var day = ArrayList<String>()
    var distanceList = HashMap<String, String>()
    var dulist = HashMap<String, String>()
    var timeList = HashMap<String, Double>()
    lateinit var todayDistance : TextView
    lateinit var todayDuration : TextView

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_profile, container, false)

        val lay = root.findViewById<ConstraintLayout>(R.id.constraintLayout)
        lay.foreground.alpha = 0

        sp = context?.getSharedPreferences("user_data", AppCompatActivity.MODE_PRIVATE)
        val date = sp!!.getString("startdate", "2022.10.20")

        name = root.findViewById(R.id.username)
        logout = root.findViewById(R.id.logout)
        tnc = root.findViewById(R.id.tnc)

        name.text = sp!!.getString("username", "user")

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("311764064674-trb86n5mk72kf3chqmvg2i04jtvj77n0.apps.googleusercontent.com") //you can also use R.string.default_web_client_id
                .requestEmail()
                .build()

        logout.setOnClickListener {
            val inflater = context?.getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val popupView: View = inflater.inflate(R.layout.popup_logout, null)

            val popupWindow = PopupWindow(popupView, 700, 400)

            popupWindow.isFocusable = true

//            lay.foreground.alpha = 120

            popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0)

            val yesb = popupWindow.contentView.findViewById<Button>(R.id.logout_yes)
            yesb.setOnClickListener {
                popupWindow.dismiss()
                sp!!.edit().clear().apply()
                FirebaseAuth.getInstance().signOut()
                Firebase.auth.signOut()
                activity?.let { it1 -> GoogleSignIn.getClient(it1, gso).signOut() }

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

        tnc.setOnClickListener {
            val intent = Intent(context, TncActivity::class.java)
            startActivity(intent)
        }

        retrieveData()

        day = ArrayList<String>()
        day.add("Mon")
        day.add("Tue")
        day.add("Wed")
        day.add("Thu")
        day.add("Fri")
        day.add("Sat")
        day.add("Sun")

        // Get the start date and end date of current week
        val today = Calendar.getInstance().time
        val startdate = sdf.parse(date!!)
        val cal: Calendar = Calendar.getInstance()
        cal.set(Calendar.DATE, today.date)
        cal.set(Calendar.MONTH, today.month)
        cal.set(Calendar.YEAR, today.year+1900)
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        var start = sdf.format(cal.time)
        cal.add(Calendar.DATE, 6)
        var end = sdf.format(cal.time)
        var current = "$start - $end"
        cal.add(Calendar.DATE, -6)

        currentWeek = root.findViewById(R.id.history_title1)
        currentWeek.text = current

        // Change the start date and end date to the previous week
        left = root.findViewById(R.id.history_left)
        left.setOnClickListener {
            if (startdate.before(sdf.parse(start))) {
                cal.add(Calendar.DATE, -1)
                end = sdf.format(cal.time)
                cal.add(Calendar.DATE, -6)
                start = sdf.format(cal.time)
                current = "$start - $end"
                currentWeek.text = current
                timeList = HashMap<String, Double>()
                setupDistanceChart(root, start)
                setupDurationCharts(root, start)
            }
        }

        // Change the start date and end date to the coming week
        right = root.findViewById(R.id.history_right)
        right.setOnClickListener {
            val today = Calendar.getInstance()
            if (today.time.after(sdf.parse(end))) {
                cal.add(Calendar.DATE, 13)
                end = sdf.format(cal.time)
                cal.add(Calendar.DATE, -6)
                start = sdf.format(cal.time)
                current = "$start - $end"
                currentWeek.text = current
                timeList = HashMap<String, Double>()
                setupDistanceChart(root, start)
                setupDurationCharts(root, start)
            }
        }

        Handler(Looper.getMainLooper()).postDelayed(
                {
                    if (distanceList[sdf.format(today)] != null) {
                        todayDistance = root.findViewById(R.id.profile_distance)
                        var di = distanceList[sdf.format(today)]!!.toFloat()
                        var distance = String.format("%.3f KM",di/1000)
                        todayDistance.text = "Distance Travelled: $distance"

                        todayDuration = root.findViewById(R.id.profile_duration)
                        var d = dulist[sdf.format(today)]!!.toFloat()
                        val hours = d / 3600
                        val minutes = (d % 3600) / 60
                        val seconds = d % 60
                        var duration = ""
                        if (hours >= 1) {
                            duration = String.format("%.0fH %.0fM %.0fS", hours, minutes, seconds)
                        } else if (minutes >= 1) {
                            duration = String.format("%.0fM %.0fS", minutes, seconds)
                        } else {
                            duration = String.format("%.2fS", seconds)
                        }
                        todayDuration.text = "Duration: $duration"

                    }
                    setupDistanceChart(root, start)
                    setupDurationCharts(root, start)
                },
                500
        )

        return root
    }

    fun setupDistanceChart(root: View, start: String) {
        distanceChart = root.findViewById<LineChart>(R.id.history_distancechart)

        val entries_res_accel_x = ArrayList<Entry>()

        distanceData = LineDataSet(entries_res_accel_x, null)

        distanceData.setDrawCircles(true)
        distanceData.setCircleColor(Color.BLACK)
        distanceData.circleHoleColor = Color.BLACK
        distanceData.disableDashedLine()
        distanceChart.axisLeft.setDrawGridLines(false)
        distanceChart.xAxis.setDrawGridLines(false)
        distanceChart.axisRight.setDrawGridLines(false)
        distanceChart.legend.isEnabled = false
        distanceChart.description = null
        distanceChart.axisRight.setDrawLabels(false)
        distanceChart.xAxis.position = XAxis.XAxisPosition.BOTTOM

        activity?.let {
            ContextCompat.getColor(
                    it,
                    R.color.black
            )
        }?.let {
            distanceData.color = it
        }

        var i = 0
        var temp_cal = Calendar.getInstance()
        val arr = start.split(".")
        temp_cal.set(Calendar.YEAR, arr[0].toInt())
        temp_cal.set(Calendar.MONTH, (arr[1].toInt()) - 1)
        temp_cal.set(Calendar.DATE, arr[2].toInt())
        while (i < 7) {
            if (distanceList.keys.contains(sdf.format(temp_cal.time))) {
                distanceData.addEntry(distanceList[sdf.format(temp_cal.time)]?.let {
                    Entry(
                            i.toFloat(),
                            it.toFloat()/1000
                    )
                })
            } else {
                distanceData.addEntry(Entry(i.toFloat(), 0F))
            }
            temp_cal.add(Calendar.DATE, 1)
            i++
        }

        val dataSetsRes = ArrayList<ILineDataSet>()
        dataSetsRes.add(distanceData)

        alldistanceData = LineData(dataSetsRes)
//        alldistanceData.setValueFormatter(DataFormatter())
        distanceChart.data = alldistanceData
        distanceChart.xAxis.valueFormatter = xFormatter(day)

        distanceChart.invalidate()

    }

    /**
     * Set up the line chart according to the calorie data of the specific week
     * Retrieve the calorie data from the step list obtained from the Firebase storage
     */
    fun setupDurationCharts(root: View, start: String) {
        durationChart = root.findViewById<LineChart>(R.id.history_durationchart)

        val entries_res_accel_x = ArrayList<Entry>()

        durationData = LineDataSet(entries_res_accel_x, null)

        durationData.setDrawCircles(true)
        durationData.setCircleColor(Color.BLACK)
        durationData.circleHoleColor = Color.BLACK
        durationData.disableDashedLine()
        durationChart.axisLeft.setDrawGridLines(false)
        durationChart.xAxis.setDrawGridLines(false)
        durationChart.axisRight.setDrawGridLines(false)
        durationChart.legend.isEnabled = false
        durationChart.description = null
        durationChart.axisRight.setDrawLabels(false)
        durationChart.xAxis.position = XAxis.XAxisPosition.BOTTOM

        activity?.let {
            ContextCompat.getColor(
                    it,
                    R.color.black
            )
        }?.let {
            durationData.color = it
        }

        var i = 0
        var temp_cal = Calendar.getInstance()
        val arr = start.split(".")
        temp_cal.set(Calendar.YEAR, arr[0].toInt())
        temp_cal.set(Calendar.MONTH, (arr[1].toInt()) - 1)
        temp_cal.set(Calendar.DATE, arr[2].toInt())
        while (i < 7) {
            if (dulist.keys.contains(sdf.format(temp_cal.time))) {
                durationData.addEntry(dulist[sdf.format(temp_cal.time)]?.let {
                    Entry(
                            i.toFloat(),
                            it.toFloat()/60
                    )
                })
            } else {
                durationData.addEntry(Entry(i.toFloat(), 0F))
            }
            temp_cal.add(Calendar.DATE, 1)
            i++
        }

        val dataSetsRes = ArrayList<ILineDataSet>()
        dataSetsRes.add(durationData)

        alldurationData = LineData(dataSetsRes)
        //alldurationData.setValueFormatter(DataFormatter())
        durationChart.data = alldurationData
        durationChart.xAxis.valueFormatter = xFormatter(day)

        durationChart.invalidate()

    }

    fun retrieveData() {
        val username = sp!!.getString("username","user")
        val storageRef = storage.reference
        var dataRef = storageRef.child("distanceData/$username.txt")
        dataRef.getBytes(ONE_MEGABYTE).addOnSuccessListener { it ->
            val data = String(it).lines()
            data.forEach {
                if (it != "") {
                    val strs = it.split(",").toTypedArray()
                    distanceList[strs[0]] = strs[1]
                    dulist[strs[0]] = strs[2]
                    println(it)
                }
            }
        }.addOnFailureListener {

        }
    }

    class xFormatter(private val xValsDateLabel: ArrayList<String>) : ValueFormatter() {

        override fun getFormattedValue(value: Float): String {
            return value.toString()
        }

        override fun getAxisLabel(value: Float, axis: AxisBase): String {
            if (value.toInt() >= 0 && value.toInt() <= xValsDateLabel.size - 1) {
                return xValsDateLabel[value.toInt()]
            } else {
                return ("").toString()
            }
        }
    }

    class DataFormatter : ValueFormatter() {
        override fun getPointLabel(entry: Entry?): String {
            return (entry?.y)?.toInt().toString()
        }
    }
}