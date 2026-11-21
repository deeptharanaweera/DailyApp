package com.example.util

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

data class ReceiptOcrResult(
    val detectedTitle: String?,
    val detectedTotal: Double?,
    val rawText: String
)

object ReceiptOcrHelper {

    /**
     * Runs 100% on-device offline Google ML Kit Text Recognition on a receipt image URI
     * and parses the most likely total amount (Rs./LKR/Net/Total) and merchant name.
     */
    suspend fun scanReceipt(context: Context, uri: Uri): ReceiptOcrResult {
        val image = runCatching { InputImage.fromFilePath(context, uri) }.getOrNull()
            ?: return ReceiptOcrResult(null, null, "")

        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        val fullText = suspendCancellableCoroutine<String> { continuation ->
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    if (continuation.isActive) continuation.resume(visionText.text)
                }
                .addOnFailureListener {
                    if (continuation.isActive) continuation.resume("")
                }
        }
        recognizer.close()

        if (fullText.isBlank()) {
            return ReceiptOcrResult(null, null, "")
        }

        val lines = fullText.lines().map { it.trim() }.filter { it.isNotEmpty() }
        val merchant = lines.firstOrNull { line ->
            line.length >= 3 && line.any { it.isLetter() } &&
                !line.lowercase().contains("receipt") &&
                !line.lowercase().contains("invoice") &&
                !line.lowercase().contains("tax")
        }?.take(40)

        val total = extractLikelyTotal(lines)
        return ReceiptOcrResult(
            detectedTitle = merchant,
            detectedTotal = total,
            rawText = fullText
        )
    }

    private fun extractLikelyTotal(lines: List<String>): Double? {
        val numberRegex = Regex("""(\d{1,3}(?:,\d{3})*(?:\.\d{1,2})?|\d+(?:\.\d{1,2})?)""")
        val priorityKeywords = listOf("grand total", "net total", "total due", "net amount", "total", "amount", "lkr", "rs")

        // First pass: check lines with total/currency keywords from bottom up (totals appear near bottom)
        for (keyword in priorityKeywords) {
            for (line in lines.asReversed()) {
                val lower = line.lowercase()
                if (lower.contains(keyword) && !lower.contains("subtotal") && !lower.contains("item")) {
                    val nums = numberRegex.findAll(line)
                        .mapNotNull { it.value.replace(",", "").toDoubleOrNull() }
                        .filter { it in 1.0..10_000_000.0 }
                        .toList()
                    if (nums.isNotEmpty()) {
                        return nums.maxOrNull()
                    }
                }
            }
        }

        // Second pass: look for largest plausible decimal currency number
        val decimalRegex = Regex("""(\d{1,3}(?:,\d{3})*\.\d{2}|\d+\.\d{2})""")
        val decimalCandidates = lines.flatMap { line ->
            decimalRegex.findAll(line)
                .mapNotNull { it.value.replace(",", "").toDoubleOrNull() }
                .filter { it in 1.0..10_000_000.0 }
                .toList()
        }
        return decimalCandidates.maxOrNull()
    }
}
