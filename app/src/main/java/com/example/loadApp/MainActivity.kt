package com.example.loadApp

import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.app.DownloadManager
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.view.animation.AccelerateInterpolator
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.content.ContextCompat


class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0

    private var url: URLS? = null
    private var textId: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbarMain))

        val motion = findViewById<MotionLayout>(R.id.Motion_layout)

    //ANIMATE ACTIVITY
        ValueAnimator().apply {
            setValues(PropertyValuesHolder.ofFloat("MotionValue", 0f, 1f))
            duration = 1500
            interpolator = AccelerateInterpolator()
            addUpdateListener {
                motion.progress = it.getAnimatedValue("MotionValue") as Float
            }
        }.start()

        findViewById<RadioGroup>(R.id.radioGroup).setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rd_Glide -> {
                    url = URLS.Glide
                    textId = R.string.Glide
                }
                R.id.rd_LoadApp -> {
                    url = URLS.LoadApp
                    textId = R.string.LoadApp
                }
                R.id.rd_Retrofit -> {
                    url = URLS.Retrofit
                    textId = R.string.Retrofit
                }
            }
        }

        createChannel(this, CHANNEL_ID, CHANNEL_NAME)

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))


        findViewById<LoadingButton>(R.id.custom_button).setOnClickListener {
            ButtonState.Clicked
            download()
        }
    }


    private fun sendNotification(statusId: Int) {
        val notificationManager = ContextCompat.getSystemService(
            this, NotificationManager::class.java
        ) as NotificationManager
        notificationManager.sendCompleteDownloadNotification(this, textId, statusId)
    }


    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)

            if (id == downloadID) {

                val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager

                val downloads = downloadManager.query(DownloadManager.Query().setFilterById(id))

                if (downloads.moveToFirst()) {
                    val index = downloads.getColumnIndex(DownloadManager.COLUMN_STATUS)

                    when (downloads.getInt(index)) {
                        DownloadManager.STATUS_SUCCESSFUL -> sendNotification(R.string.Success)
                        DownloadManager.STATUS_FAILED -> sendNotification(R.string.Failed)
                    }
                } else {
                    sendNotification(R.string.Failed)
                }
                ButtonState.Completed
            }
        }
    }


    private fun download() {
        ButtonState.Loading
        if (url != null) {
            val request =
                DownloadManager.Request(Uri.parse(url!!.value))
                    .setTitle(getString(R.string.app_name))
                    .setDescription(getString(R.string.app_description))
                    .setRequiresCharging(false)
                    .setAllowedOverMetered(true)
                    .setAllowedOverRoaming(true)
            val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager

            downloadID = downloadManager.enqueue(request)
        } else Toast.makeText(this, "Please select the file to download", Toast.LENGTH_SHORT).show()
    }

    override fun onPause() {
        super.onPause()
        finish()
    }

}


private enum class URLS(val value: String) {
    Glide("https://github.com/bumptech/glide"),
    LoadApp("https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter"),
    Retrofit("https://github.com/square/retrofit")
}
