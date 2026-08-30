package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object AppActionHelper {

    /**
     * Opens the device phone dialer with the given phone or helpline number.
     */
    fun openDialer(
        context: Context,
        phoneNumber: String,
        onSuccess: ((String) -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        try {
            // Clean phone string to remove alphabetic labels while keeping digits, +, and -
            val cleaned = phoneNumber
                .replace(Regex("[^0-9+]"), "")
                .trim()

            val effectiveNumber = if (cleaned.isNotEmpty()) cleaned else phoneNumber.filter { it.isDigit() || it == '+' }
            
            if (effectiveNumber.isBlank()) {
                val err = "No valid phone number found"
                onError?.invoke(err)
                Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                return
            }

            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$effectiveNumber")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            onSuccess?.invoke("Opening dialer for $effectiveNumber")
        } catch (e: Exception) {
            val errMsg = "Unable to open phone dialer: ${e.localizedMessage ?: "Unknown error"}"
            onError?.invoke(errMsg)
            Toast.makeText(context, errMsg, Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Opens an external web URL securely in the browser.
     */
    fun openWebUrl(
        context: Context,
        url: String,
        onSuccess: ((String) -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        try {
            var formattedUrl = url.trim()
            if (!formattedUrl.startsWith("http://") && !formattedUrl.startsWith("https://")) {
                formattedUrl = "https://$formattedUrl"
            }
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(formattedUrl)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            onSuccess?.invoke("Opening $formattedUrl")
        } catch (e: Exception) {
            val errMsg = "Unable to open link ($url): ${e.localizedMessage ?: "No web browser app found"}"
            onError?.invoke(errMsg)
            Toast.makeText(context, errMsg, Toast.LENGTH_SHORT).show()
        }
    }
}
