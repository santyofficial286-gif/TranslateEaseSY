package com.santosh.hin2eng

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import com.santosh.hin2eng.databinding.ActivityMainBinding
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var translator: Translator
    private var tts: TextToSpeech? = null

    private val micPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) startVoiceRecognition() else
                Toast.makeText(this, "Microphone permission is needed for voice input", Toast.LENGTH_SHORT).show()
        }

    private val speechLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val text = result.data
                    ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    ?.firstOrNull()
                if (!text.isNullOrBlank()) {
                    binding.hindiInput.setText(text)
                    translate(text)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the offline Hindi -> English translator
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.HINDI)
            .setTargetLanguage(TranslateLanguage.ENGLISH)
            .build()
        translator = Translation.getClient(options)
        downloadModelIfNeeded()

        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) tts?.language = Locale.US
        }

        binding.micButton.setOnClickListener { checkMicPermissionAndStart() }

        binding.translateButton.setOnClickListener {
            val text = binding.hindiInput.text.toString().trim()
            if (text.isEmpty()) {
                Toast.makeText(this, "Type or speak something in Hindi first", Toast.LENGTH_SHORT).show()
            } else {
                translate(text)
            }
        }

        binding.speakButton.setOnClickListener {
            val text = binding.englishOutput.text.toString()
            if (text.isNotBlank() && text != "Translation will appear here") {
                tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
            }
        }
    }

    private fun downloadModelIfNeeded() {
        val conditions = DownloadConditions.Builder().requireWifi().build()
        translator.downloadModelIfNeeded(conditions)
            .addOnSuccessListener { /* model ready, offline translation now works */ }
            .addOnFailureListener {
                Toast.makeText(this, "Connect to Wi-Fi once to download the translation model", Toast.LENGTH_LONG).show()
            }
    }

    private fun translate(hindiText: String) {
        binding.progressBar.visibility = View.VISIBLE
        translator.translate(hindiText)
            .addOnSuccessListener { englishText ->
                binding.progressBar.visibility = View.GONE
                binding.englishOutput.text = englishText
            }
            .addOnFailureListener {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, "Translation failed. Check your internet connection for the first run.", Toast.LENGTH_LONG).show()
            }
    }

    private fun checkMicPermissionAndStart() {
        val granted = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED
        if (granted) startVoiceRecognition() else micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    private fun startVoiceRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "बोलिए…")
        }
        try {
            speechLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Voice recognition is not available on this device", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        translator.close()
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}
