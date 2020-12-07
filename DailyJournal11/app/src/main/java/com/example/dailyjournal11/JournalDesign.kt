package com.example.dailyjournal11

import android.Manifest
import android.R.attr
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.*
import android.media.AudioManager.OnAudioFocusChangeListener
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.util.Log
import android.widget.*
import androidx.core.net.toUri
import androidx.core.view.marginBottom
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.net.URI
import java.util.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.collections.ArrayList


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
    private lateinit var mDeleteButton: Button

    private lateinit var mAudioFilename: String
    private var mRecorder: MediaRecorder? = null
    private var mPlayer: MediaPlayer? = null
    private lateinit var mAudioManager: AudioManager

    private val mPermissions =
        arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE)

    private lateinit var mPlay: ToggleButton
    private lateinit var mRecord: ToggleButton
    private lateinit var mDeleteAudio: Button
    private lateinit var mPlaybackAttributes: AudioAttributes
    private lateinit var mFocusRequest: AudioFocusRequest
    private val mFocusLock = Any()
    private var mPlaybackDelayed: Boolean = false
    private var mResumeOnFocusGain: Boolean = false

    private lateinit var mScrollView: LinearLayout
    private lateinit var mImageUris: ArrayList<Uri>

    private lateinit var mDate: String
    private lateinit var mId: String
    private lateinit var uid: String


    private lateinit var mStorage: StorageReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.design_layout)

        uid = intent.getStringExtra("username")!!

        val databaseJournals = FirebaseDatabase.getInstance().getReference("journals/$uid")
        mStorage = FirebaseStorage.getInstance().getReference()

        mPictureButton = findViewById(R.id.picture_button)
        mSubmitButton = findViewById(R.id.submitButton)
        mBackButton = findViewById(R.id.BackButton)
        mEditText = findViewById(R.id.body_text)
        mDeleteButton = findViewById(R.id.delete_button)

        var deleteAudioBoolean = false

        val givenIntent = intent

        //the code written to record and playback audio in this project is taken from an example used in
        //CMSC 436 lecture as it directly reflected what we needed in this project.
        //the example used was: AudioVideoAudioRecording
        mAudioFilename = application.getExternalFilesDir(null)?.absolutePath + "/audioFile.3gp"
        mRecord = findViewById(R.id.record_audio_button)
        mPlay = findViewById(R.id.play_audio_button)
        mDeleteAudio = findViewById(R.id.delete_audio_button)

        if (checkSelfPermission(mPermissions[0]) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(mPermissions[1]) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(mPermissions, APP_PERMS_REQ)
        }else {
            continueApp()
        }

        mRecord.setOnCheckedChangeListener {_, isChecked ->
            mPlay.isEnabled = !isChecked

            Log.i(TAG, "start/stop recording")
            onRecordPressed(isChecked)
        }

        mPlay.setOnCheckedChangeListener {_, isChecked ->
            mRecord.isEnabled = !isChecked

            Log.i(TAG, "start/stop playing")
            onPlayPressed(isChecked)
        }

        mDeleteAudio.setOnClickListener {

            val file = File(mAudioFilename)
            file.delete()

            deleteAudioBoolean = true
            mDeleteAudio.isEnabled = false
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
        mImageUris = ArrayList<Uri>()

        mPictureButton.setOnClickListener {
            Log.i(TAG, "Picture button clicked")
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_IMAGE)
        }

        //check if the journal is existing already or if newJournal button was pressed
        if(givenIntent.extras!!.getBoolean("ISNEWJOURNAL") == false){
            val text = givenIntent.getStringExtra("TEXT").toString()
            mEditText.setText(text)
            mDate = givenIntent.getStringExtra("DATE")!!
            mId = givenIntent.getStringExtra("ID")!!


            //audio download from firebase storage
            val file = File(mAudioFilename)
            val downloadAudio = mStorage.child("$uid/${mId}_${mDate}_audio.3gp")

            downloadAudio.getFile(file).addOnSuccessListener {
                //handle success
                Log.i(TAG, "audio download success")
                mDeleteAudio.isEnabled = true
            }.addOnFailureListener{
                //handle failure
                Log.i(TAG, "audio download failure")
                mDeleteAudio.isEnabled = false
            }

            //image download from firebase storage
            var done = 0
            for(i in 0..10) { //download ten images
                if(done == 1) {
                    break
                }
                val imageReference = mStorage.child("$uid/${mId}_${mDate}_/document/$i.jpg")
                val imageFilename =
                    application.getExternalFilesDir(null)?.absolutePath + "/image$i.jpg"

                val localFile = File(imageFilename)
                imageReference.getFile(localFile).addOnSuccessListener {
                    //handle success of download
                    Log.i(TAG, "Download successful $i")
                    var imageToAdd = ImageView(applicationContext)
                    imageToAdd.setImageURI(Uri.fromFile(localFile))
                    imageToAdd.maxWidth = 400
                    imageToAdd.maxHeight = 400
                    imageToAdd.adjustViewBounds = true
                    imageToAdd.setPadding(10, 0, 10, 0)
                    //ximageToAdd.layout
                    //mScrollView.dividerPadding = 0
                    var oof = mScrollView.layoutParams
                    mScrollView.addView(imageToAdd)
                }.addOnFailureListener {
                    //handle failure operation
                    Log.i(TAG, "Download failed $i")
                    done = 1
                }
            }

        } else {
            val file = File(mAudioFilename)
            file.delete()

            mDeleteAudio.isEnabled = false

            mId = databaseJournals.push().key!!
            mDate = givenIntent.getStringExtra("DATE")!!
        }

        mSubmitButton.setOnClickListener {
            Log.i(TAG, "submit button clicked")


            if (deleteAudioBoolean == true){
                //delete audio if audio button is pressed
                val audioReference = mStorage.child("$uid/${mId}_${mDate}_audio.3gp")

                audioReference.delete().addOnSuccessListener {
                    //handle success
                    Log.i(TAG, "audio deletion success")
                }.addOnFailureListener{
                    Log.i(TAG, "audio deletion failure")
                }
            } else {
                //audio upload to firebase storage
                val file = Uri.fromFile(File(mAudioFilename))
                val audioReference = mStorage.child("$uid/${mId}_${mDate}_audio.3gp")

                val uploadAudio = audioReference.putFile(file)

                uploadAudio.addOnFailureListener{
                    //handle failure of upload
                    Log.i(TAG, "Upload failure")
                }.addOnSuccessListener {
                    //handle success of upload
                    Log.i(TAG, "Upload successful")
                }
            }


            var jdata = JournalData(mId!!, mDate, mEditText.text.toString())

            databaseJournals.child(mId!!).setValue(jdata)

            //image upload
            var i = 0
            for (uri in mImageUris) {
                val file = File(uri.path)
                val imageReference = mStorage.child("$uid/${mId}_${mDate}_/document/${i}.jpg")
                val uploadImages = imageReference.putFile(uri)

                uploadImages.addOnFailureListener{
                    //handle failure of upload
                    Log.i(TAG, "upload failure")
                }.addOnSuccessListener {
                    //handle success of upload
                    Log.i(TAG, "Upload successful")
                }
                ++i
            }

            //return with value
            setResult(436)
            finish()
        }

        mDeleteButton.setOnClickListener{
            Log.i(TAG, "delete button clicked")

            //remove and delete audio
            val audioReference = mStorage.child("$uid/${mId}_${mDate}_audio.3gp")

            audioReference.delete().addOnSuccessListener {
                //handle success
                Log.i(TAG, "audio deletion success")
            }.addOnFailureListener{
                Log.i(TAG, "audio deletion failure")
            }

            val file = File(mAudioFilename)
            file.delete()

            //remove and delete images


            //remove and delete realtime data
            databaseJournals.child(mId!!).removeValue()



            for (i in 0..10) {
                //val file = File(uri.path)
                val imageReference = mStorage.child("$uid/${mId}_${mDate}_/document/${i}.jpg")

                imageReference.delete().addOnFailureListener{
                    //handle failure of upload
                    Log.i(TAG, "delete image failure")
                }.addOnSuccessListener {
                    //handle success of upload
                    Log.i(TAG, "delete image successful")
                }
            }

            finish()
        }

        mBackButton.setOnClickListener {
            setResult(-1) //appease Kotlin runtime null check
            finish()
        }
    }

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
            mDeleteAudio.isEnabled = true
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
                        "No recoring.",
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


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Unit {
        if (requestCode == SELECT_IMAGE && resultCode != 0) {
            val inputStream = applicationContext.contentResolver.openInputStream(data!!.data!!)

            //put the Uris in the list to be access on submit
            mImageUris.add(data!!.data!!)

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
            Log.i(TAG, "get image cancelled")
        }
    }






}