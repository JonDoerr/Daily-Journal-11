package com.example.dailyjournal11

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

private lateinit var mAudioButton: Button
private lateinit var mPictureButton: Button
private lateinit var mSubmitButton: Button
private lateinit var mBackButton: Button
private lateinit var mEditText: EditText

class JournalDesign : Activity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.design_layout)

        mAudioButton = findViewById(R.id.audio_button)
        mPictureButton = findViewById(R.id.picture_button)
        mSubmitButton = findViewById(R.id.submitButton)
        mBackButton = findViewById(R.id.BackButton)
        mEditText = findViewById(R.id.body_text)

        mAudioButton.setOnClickListener {
            Toast.makeText(applicationContext, "audio Button clicked", Toast.LENGTH_SHORT).show() //TODO remove this
        }

        mPictureButton.setOnClickListener {
            Toast.makeText(applicationContext, "picture Button clicked", Toast.LENGTH_SHORT).show() //TODO remove this
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
}