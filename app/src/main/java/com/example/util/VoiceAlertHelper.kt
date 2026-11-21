package com.example.util

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class VoiceAlertHelper(context: Context) {

    private var tts: TextToSpeech? = null
    private var isReady = false

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            isReady = (status == TextToSpeech.SUCCESS)
        }
    }

    fun speakMedicationReminder(
        medName: String,
        dosage: String,
        memberName: String,
        isSinhala: Boolean
    ) {
        val engine = tts ?: return
        if (!isReady) return

        val sinhalaLocale = Locale("si", "LK")
        val canSpeakSinhala = engine.isLanguageAvailable(sinhalaLocale) >= TextToSpeech.LANG_AVAILABLE

        val message = if (isSinhala && canSpeakSinhala) {
            engine.language = sinhalaLocale
            val memberSi = DomainLabels.memberLabel(memberName, true)
            "කරුණාකර $memberSi ගේ $medName, $dosage බෙහෙත් වේල දැන් ලබා ගන්න."
        } else {
            engine.language = Locale.US
            if (isSinhala) {
                "Medication reminder for $memberName. Please take $medName, $dosage now."
            } else {
                "Medication reminder for $memberName. Please take your $medName, $dosage now."
            }
        }

        engine.setSpeechRate(0.92f)
        engine.speak(message, TextToSpeech.QUEUE_FLUSH, null, "med_alert_${System.currentTimeMillis()}")
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
