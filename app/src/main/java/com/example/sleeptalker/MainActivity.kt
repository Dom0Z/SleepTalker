@file:Suppress("DEPRECATION")

package com.example.sleeptalker

import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.sax.StartElementListener
import android.widget.Button
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    lateinit var mr : MediaRecorder
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        var path = Environment.getExternalStorageDirectory().toString()+"/myrec.3gpp"
        mr = MediaRecorder()





        Start.isEnabled = false
        Stop.isEnabled = false
        //https://www.youtube.com/watch?v=cqcWyOlDfUA&ab_channel=DrVipinClasses
        if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.RECORD_AUDIO,android.Manifest.permission.WRITE_EXTERNAL_STORAGE),111)
            Start.isEnabled = true

        Start.setOnClickListener{
            //https://stackoverflow.com/questions/11985518/android-record-sound-in-mp3-format
            mr.setAudioSource(MediaRecorder.AudioSource.MIC)
            mr.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            mr.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            mr.setOutputFile(path)
            mr.prepare()
            mr.start()
            Stop.isEnabled = true
            Start.isEnabled = false

        }
        Stop.setOnClickListener{
            mr.stop()
            Start.isEnabled = true
            Stop.isEnabled = false
        }
        Play.setOnClickListener {
            var mp = MediaPlayer()
            mp.setDataSource(path)
            mp.prepare()
            mp.start()
        }


    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode==111 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            Start.isEnabled =true
    }

}