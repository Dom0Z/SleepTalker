@file:Suppress("DEPRECATION")

package com.example.sleeptalker

import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import android.Manifest
import com.example.sleeptalker.permissionUtils.OnPermissionDeniedListener
import com.example.sleeptalker.permissionUtils.OnPermissionGrantedListener
import com.example.sleeptalker.permissionUtils.OnPermissionPermanentlyDeniedListener



class MainActivity : AppCompatActivity(),OnPermissionPermanentlyDeniedListener,
    OnPermissionGrantedListener,OnPermissionDeniedListener {


    var mediaRecorder: MediaRecorder? = null
    var mediaPlayer: MediaPlayer? = null
    var audioUri: Uri? = null
    var currentPosition: Int = 0
    var isRecordButtonClicked: Boolean = false
    var isRecordingStopped: Boolean = false


    lateinit var onPermissionDeniedListener: OnPermissionDeniedListener
    lateinit var onPermissionGrantedListener: OnPermissionGrantedListener
    lateinit var onPermissionPermanentlyDeniedListener: OnPermissionPermanentlyDeniedListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)




        StartRecord.setOnClickListener{
            isRecordButtonClicked = true
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED -> {
                    onPermissionGrantedListener.OnPermissionGranted()
                }
                shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO) == false -> {
                    onPermissionPermanentlyDeniedListener.OnPermissionPermanentlyDenied()
                }
                else -> {
                    checkSinglePermission(Manifest.permission.RECORD_AUDIO)
                }
            }

        }
        StopRecord.setOnClickListener{
            isRecordButtonClicked = false
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED -> {
                    onPermissionGrantedListener.OnPermissionGranted()
                }
                shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO) == false -> {
                    onPermissionPermanentlyDeniedListener.OnPermissionPermanentlyDenied()
                }
                else -> {
                    checkSinglePermission(Manifest.permission.RECORD_AUDIO)
                }
            }
        }
        Play.setOnClickListener {
            playAudio()
        }
        Pause.setOnClickListener {
            pauseAudio()
        }
        Resume.setOnClickListener {
            resumeAudio()
        }
        Stop.setOnClickListener {
            stopAudio()
        }
        onPermissionDeniedListener = this
        onPermissionGrantedListener = this
        onPermissionPermanentlyDeniedListener = this


    }

    private fun stopAudio() {
        TODO("Not yet implemented")
    }

    private fun resumeAudio() {
        TODO("Not yet implemented")
    }

    private fun pauseAudio() {
        TODO("Not yet implemented")
    }

    private fun playAudio() {
        TODO("Not yet implemented")
    }

    val requestSinglePermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        var isDenied = 2
        if(isGranted){

        }else{
            isDenied = 1
        }


        if (isDenied == 1) onPermissionDeniedListener.OnPermissionDenied()
        else if (isDenied == 0) onPermissionPermanentlyDeniedListener.OnPermissionPermanentlyDenied()
        else onPermissionGrantedListener.OnPermissionGranted()

    }
    fun checkSinglePermission(permission: String) {
        requestSinglePermission.launch(permission)
    }

    override fun OnPermissionDenied() {
        TODO("Not yet implemented")
    }

    override fun OnPermissionGranted() {
        TODO("Not yet implemented")
    }

    override fun OnPermissionPermanentlyDenied() {
        TODO("Not yet implemented")
    }


    /*override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode==111 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            Start.isEnabled =true
    }
    fun  checkPermission() : Boolean{
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            return Environment.isExternalStorageManager();
        }else{
            var readCheck = ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.READ_EXTERNAL_STORAGE)
            var writeCheck = ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            var audioCheck = ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.RECORD_AUDIO)

            return readCheck == PackageManager.PERMISSION_GRANTED && writeCheck == PackageManager.PERMISSION_GRANTED && audioCheck == PackageManager.PERMISSION_GRANTED
        }

    }

     */
   /* fun requestPermission() : Void{
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.addCategory("android.intent.category.DEFAULT");
                intent.data = Uri.parse(String.format("package:%s",{applicationContext.packageName}))
            }
            catch{

            }
        }

    }
 */



}