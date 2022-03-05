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
import android.annotation.TargetApi
import android.content.*
import android.database.Cursor
import android.os.Build
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.sleeptalker.permissionUtils.OnPermissionDeniedListener
import com.example.sleeptalker.permissionUtils.OnPermissionGrantedListener
import com.example.sleeptalker.permissionUtils.OnPermissionPermanentlyDeniedListener
import java.io.File
import java.io.FileDescriptor


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
    private fun stopRecord() {
        if (mediaRecorder != null) {
            mediaRecorder?.stop()
            mediaRecorder?.release()
            mediaRecorder = null
            isRecordingStopped = true
            Toast.makeText(this, "Recording is stopped", Toast.LENGTH_LONG).show()
        }
    }

    private fun stopAudio() {
        if (mediaPlayer != null) {
            if (mediaPlayer!!.isPlaying) {
                currentPosition = 0
                mediaPlayer?.stop()
                mediaPlayer?.release()
                mediaPlayer = null
                Toast.makeText(this, "Recording playing stopped", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun resumeAudio() {
        if (mediaPlayer != null) {
            mediaPlayer?.start()
            mediaPlayer?.seekTo(currentPosition)
            Toast.makeText(this, "Recording playing resumed", Toast.LENGTH_LONG).show()
        }
    }

    private fun pauseAudio() {
        if (mediaPlayer != null) {
            if (mediaPlayer?.isPlaying!!) {
                currentPosition = mediaPlayer?.currentPosition!!
                mediaPlayer?.pause()
                Toast.makeText(this, "Recording playing paused", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun playAudio() {
        if (isRecordingStopped) {
            try {
                mediaPlayer = MediaPlayer()
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    mediaPlayer!!.setDataSource(getFilePath())
                    //00445262
                } else {
                    mediaPlayer!!.setDataSource(getFileDescriptor2())
                }
                mediaPlayer!!.prepare()
                mediaPlayer!!.start()
                Toast.makeText(this, "Recording is playing", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    override fun onPause() {
        if(!isRecordingStopped){
            stopRecord()
        }else{
            stopAudio()
        }
        super.onPause()
    }
    private fun recordAudio() {
        try {
            isRecordingStopped = false
            mediaRecorder = MediaRecorder()
            mediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
            mediaRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                mediaRecorder!!.setOutputFile(getFilePath())
            } else {
                mediaRecorder!!.setOutputFile(getFileDescriptor())
            }
            mediaRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            mediaRecorder!!.prepare()
            mediaRecorder!!.start()
            Toast.makeText(this, "Recording is started", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    fun checkSinglePermission(permission: String) {
        requestSinglePermission.launch(permission)
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

    private fun getFilePath(): String {
        var directory: File? =
            getAppSpecificAlbumStorageDir(this, Environment.DIRECTORY_MUSIC, "DomoDemo")
        var file = File(directory, "test_audio.mp3")
        return file.absolutePath
    }
    fun getAppSpecificAlbumStorageDir(context: Context, albumName: String, subAlbumName: String): File? {
        // Get the Audio directory that's inside the app-specific directory on
        // external storage.
        val file: File = File(
            context.getExternalFilesDir(
                albumName
            ), subAlbumName
        )
        if (!file?.mkdirs()) {
            Log.e("fssfsf", "Directory not created")
        }

        return file
    }
    private fun getFileDescriptor(): FileDescriptor {
        var displayName: String? = null
        var relativePath: String? = null
        var media_id: String? = null
        val filePathColumn =
            arrayOf(MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DISPLAY_NAME)
        var uri = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        val selection = "${MediaStore.Audio.Media.DISPLAY_NAME} = ?"
        val selectionArgs = arrayOf("test_domo_audio.mp3")
        var cursor: Cursor? = null
        try {
            cursor = getContentResolver().query(uri, filePathColumn, selection, selectionArgs, null)
            if (cursor != null) {
                //cursor.moveToFirst()
                while (cursor.moveToNext()) {
                    var idColumn: Int = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                    var nameColumn: Int =
                        cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
                    val uri: Uri = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
                    relativePath =
                        ContentUris.withAppendedId(uri, cursor.getLong(idColumn)).toString()
                    displayName = cursor.getString(nameColumn)
                    media_id = cursor.getLong(idColumn).toString()

                }
            }
        } finally {
            if (cursor != null) {
                cursor.close()
            }
        }
        var mimeType = "audio/*"
        var resolver: ContentResolver = applicationContext.contentResolver
        var audioCollection: Uri = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        val values = ContentValues().apply {
            put(MediaStore.Audio.Media.DISPLAY_NAME, "test_Domo_audio.mp3")
            //put(MediaStore.Audio.Media.TITLE, "test_audio")
            put(MediaStore.Audio.Media.MIME_TYPE, mimeType)
            put(MediaStore.Audio.Media.RELATIVE_PATH, getAudioDirectoryPath())
            //put(MediaStore.Audio.Media.IS_PENDING, 1)
        }
        if (!relativePath.isNullOrEmpty()) {
            audioUri = Uri.parse(relativePath)
        } else {
            audioUri = resolver.insert(audioCollection, values)
        }
        var parcelFileDescriptor: ParcelFileDescriptor =
            resolver.openFileDescriptor(audioUri!!, "wt")!!
        return parcelFileDescriptor.fileDescriptor
    }
    private fun getFileDescriptor2(): FileDescriptor {
        var parcelFileDescriptor: ParcelFileDescriptor =
            contentResolver.openFileDescriptor(audioUri!!, "r")!!
        return parcelFileDescriptor.fileDescriptor
    }
    fun getAudioDirectoryPath(): String{
        return Environment.DIRECTORY_MUSIC + File.separator + "DomoDemo" + File.separator
    }
    fun ShowPrompt(isPermanentlyDenied: Boolean) {
        val alertBuilder = AlertDialog.Builder(this)
        alertBuilder.setCancelable(true)
        alertBuilder.setTitle("Permission necessary")
        alertBuilder.setMessage("Allow this app to access Photos and videos?")
        alertBuilder.setPositiveButton(
            android.R.string.yes,
            object : DialogInterface.OnClickListener {
                @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                override fun onClick(dialog: DialogInterface, which: Int) {
                    //hasPermissions(context,permissions);
                    if (isPermanentlyDenied) {
                        showSettings()
                    } else {
                        checkPermissions()
                    }
                }
            })
        val alert = alertBuilder.create()
        alert.show()
    }

    fun showSettings() {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }
    fun checkPermissions() {
        requestMultiplePermissions.launch(
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO,
            )
        )

    }

    val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            var isDenied = 2
            permissions.entries.forEach {
                if (it.value == false) {
                    var showRationale1: Boolean = shouldShowRequestPermissionRationale(it.key)
                    if (!showRationale1) {
                        isDenied = 0
                    } else {
                        isDenied = 1
                    }
                }
                Log.e("DEBUG", "${it.key} = ${it.value}")
            }
            if (isDenied == 1) onPermissionDeniedListener.OnPermissionDenied()
            else if (isDenied == 0) onPermissionPermanentlyDeniedListener.OnPermissionPermanentlyDenied()
            else onPermissionGrantedListener.OnPermissionGranted()

        }





    override fun OnPermissionDenied() {
        ShowPrompt(false)
    }

    override fun OnPermissionGranted() {
        if (isRecordButtonClicked) {
            if (this.packageManager.hasSystemFeature(PackageManager.FEATURE_MICROPHONE)) {
                recordAudio()
            } else {
                Toast.makeText(this, "No Microphone available on this device.", Toast.LENGTH_LONG)
                    .show()
            }
        } else {
            if (this.packageManager.hasSystemFeature(PackageManager.FEATURE_MICROPHONE)) {
                stopRecord()
            } else {
                Toast.makeText(this, "No Microphone available on this device.", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    override fun OnPermissionPermanentlyDenied() {
        ShowPrompt(true)
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