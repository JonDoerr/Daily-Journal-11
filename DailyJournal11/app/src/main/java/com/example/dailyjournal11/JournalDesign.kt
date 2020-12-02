package com.example.dailyjournal11

import android.Manifest
import android.R.attr
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.*
import android.media.AudioManager.OnAudioFocusChangeListener
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.*
import androidx.core.view.marginBottom
import java.io.File
import java.io.IOException
import kotlin.random.Random


class JournalDesign : Activity(), OnAudioFocusChangeListener {

    companion object {
        private const val TAG = "journalDesign"
        private const val APP_PERMS_REQ = 123
        private const val SELECT_IMAGE = 9137
    }

    private lateinit var mPictureButton: Button
    private lateinit var mSubmitButton: Button
    private lateinit var mBackButton: Button
    private lateinit var mEditText: EditText

    private lateinit var mAudioFilename: String
    private var mRecorder: MediaRecorder? = null
    private var mPlayer: MediaPlayer? = null
    private lateinit var mAudioManager: AudioManager

    //TODO - can remove write_external_storage once database has been implemented
    private val mPermissions =
        arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE)

    private lateinit var mPlay: ToggleButton
    private lateinit var mRecord: ToggleButton
    private lateinit var mPlaybackAttributes: AudioAttributes
    private lateinit var mFocusRequest: AudioFocusRequest
    private val mFocusLock = Any()
    private var mPlaybackDelayed: Boolean = false
    private var mResumeOnFocusGain: Boolean = false

    private lateinit var mScrollView: LinearLayout



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.design_layout)


        mPictureButton = findViewById(R.id.picture_button)
        mSubmitButton = findViewById(R.id.submitButton)
        mBackButton = findViewById(R.id.BackButton)
        mEditText = findViewById(R.id.body_text)



//      audio section start in oncreate()
        mAudioFilename = application.getExternalFilesDir(null)?.absolutePath + "/audioFile.3gp" //TODO this needs to be transfered to a database format instead of external files
        mRecord = findViewById(R.id.record_audio_button)
        mPlay = findViewById(R.id.play_audio_button)

        if (checkSelfPermission(mPermissions[0]) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(mPermissions[1]) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(mPermissions, APP_PERMS_REQ)
        }else {
            continueApp()
        }

        mRecord.setOnCheckedChangeListener {_, isChecked ->
            mPlay.isEnabled = !isChecked

            Toast.makeText(applicationContext, "start/stop recording", Toast.LENGTH_SHORT).show()
            onRecordPressed(isChecked)
        }

        mPlay.setOnCheckedChangeListener {_, isChecked ->
            mRecord.isEnabled = !isChecked

            Toast.makeText(applicationContext, "start/stop playing", Toast.LENGTH_SHORT).show()
            onPlayPressed(isChecked)
        }

        mAudioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        mPlaybackAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build()

        mFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
            .setAudioAttributes(mPlaybackAttributes)
            .setAcceptsDelayedFocusGain(true)
            .setWillPauseWhenDucked(true)
            .setOnAudioFocusChangeListener(this, Handler(Looper.getMainLooper()))
            .build()

        //Init image resources

        mScrollView = findViewById(R.id.LinearScrollView)


        mPictureButton.setOnClickListener {
            Toast.makeText(applicationContext, "picture Button clicked", Toast.LENGTH_SHORT).show() //TODO remove this
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_IMAGE)
        }


        mSubmitButton.setOnClickListener {
            Toast.makeText(applicationContext, "submit Button clicked", Toast.LENGTH_SHORT).show() //TODO remove
            //TODO return with value
            val intent = Intent().putExtra("body", mEditText.text.toString())
            setResult(436, intent)
            finish()
        }

        mBackButton.setOnClickListener {
            setResult(-1, Intent()) //appease Kotlin runtime null check
            finish()
        }
    }


//    audio section methods start

    //record button pressed
    private fun onRecordPressed(shouldStartRecording: Boolean){
        if (shouldStartRecording){
            startRecording()
        } else{
            stopRecording()
        }
    }

    //start recording
    private fun startRecording(){
        mRecorder = MediaRecorder()
        mRecorder?.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(mAudioFilename)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            try {
                prepare()
                start()
            } catch (e: IOException) {
                Log.e(TAG, "Couldn't prepare and start MediaRecorder")
            }
        }
    }

    //stop recording
    private fun stopRecording() {
        mRecorder?.apply {
            stop()
            release()
            mRecorder = null
        }
    }

    //play button pressed
    private fun onPlayPressed(shouldStartPlaying: Boolean) {

        if (shouldStartPlaying) {
            startPlaying()
        } else {
            stopPlaying()
        }
    }

    private fun startPlaying() {

        mPlayer = MediaPlayer()
        mPlayer?.apply {
            setAudioAttributes(mPlaybackAttributes)
            setOnCompletionListener {
                Log.i(TAG, "Reached oncompletionlistener")
                mPlay.performClick()
                mPlay.isChecked = false
            }
            try {
                if (File(mAudioFilename).exists()) {
                    apply {
                        setDataSource(mAudioFilename)
                        prepare()
                        requestAudioFocus()
                    }
                } else {
                    Toast.makeText(
                        this@JournalDesign,
                        "No recoring made",
                        Toast.LENGTH_LONG
                    ).show()
                    mPlay.isChecked = false
                    continueApp()
                }

            } catch (e: IOException) {
                Log.e(TAG, "Couldn't prepare and start MediaPlayer")
            }
            mAudioManager.abandonAudioFocusRequest(mFocusRequest)
        }
    }

    private fun requestAudioFocus() {
        // requesting audio focus
        val res = mAudioManager.requestAudioFocus(mFocusRequest)
        synchronized(mFocusLock) {
            when (res) {
                AudioManager.AUDIOFOCUS_REQUEST_FAILED -> mPlaybackDelayed = false
                AudioManager.AUDIOFOCUS_REQUEST_GRANTED -> {
                    mPlaybackDelayed = false
                    startPlayback()
                }
                AudioManager.AUDIOFOCUS_REQUEST_DELAYED -> mPlaybackDelayed = true
            }
        }
    }

    private fun startPlayback() {
        mPlayer?.start()
    }

    private fun stopPlaying() {
        mPlayer?.apply {
            if (isPlaying) {
                stop()
                mAudioManager.abandonAudioFocusRequest(mFocusRequest)
            }
            release()
            mPlayer = null
        }
    }


    private fun pausePlaying() {
        mPlayer?.apply {
            if (isPlaying) {
                pause()
            }
        }
    }

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> if (mPlaybackDelayed || mResumeOnFocusGain) {
                synchronized(mFocusLock) {
                    mPlaybackDelayed = false
                    mResumeOnFocusGain = false
                }
                startPlayback()
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                synchronized(mFocusLock) {
                    mResumeOnFocusGain = false
                    mPlaybackDelayed = false
                }
                pausePlaying()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT, AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                synchronized(mFocusLock) {
                    mResumeOnFocusGain = mPlayer!!.isPlaying
                    mPlaybackDelayed = false
                }
                pausePlaying()
            }
        }
    }

    public override fun onPause() {
        super.onPause()

        mRecorder?.apply {
            release()
            mRecorder = null
        }

        mPlayer?.apply {
            release()
            mPlayer = null
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (APP_PERMS_REQ == requestCode) {
            var permsFailed = false
            for (result in grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(
                        applicationContext,
                        "need permissions",
                        Toast.LENGTH_LONG
                    ).show()
                    permsFailed = true
                    break
                }
            }
            if (!permsFailed) {
                continueApp()
            }
        }
    }


    private fun continueApp(){
        mPlay.isEnabled = true
        mRecord.isEnabled = true
    }
//    audio section methods end

    //Picture section
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Unit {
        if (requestCode == SELECT_IMAGE && resultCode != 0) {
            val inputStream = applicationContext.contentResolver.openInputStream(data!!.data!!)

            var imageToAdd = ImageView(applicationContext)
            imageToAdd.setImageURI(data!!.data!!)
            imageToAdd.maxWidth = 400
            imageToAdd.maxHeight = 400
            imageToAdd.adjustViewBounds = true
            imageToAdd.setPadding(10, 0, 10, 0)
            //ximageToAdd.layout
            //mScrollView.dividerPadding = 0
            var oof = mScrollView.layoutParams
            mScrollView.addView(imageToAdd)
        }
        else {
            Toast.makeText(applicationContext, "get image cancelled", Toast.LENGTH_SHORT).show()
        }
    }
}