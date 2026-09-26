package com.noxscreen.app.support

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

object SupportHelper {
    const val WHATSAPP_ACCOUNT_NAME = "NoXScreen Support"
    const val WHATSAPP_PHONE_NUMBER = "+252637864155"
    const val WHATSAPP_URL = "https://wa.me/252637864155"
    const val WHATSAPP_INITIAL_MESSAGE = "Hello, I need support with NoXScreen."

    private const val WHATSAPP_PACKAGE = "com.whatsapp"
    private const val WHATSAPP_BUSINESS_PACKAGE = "com.whatsapp.w4b"

    fun buildWhatsAppUri(includeDefaultMessage: Boolean = true): Uri {
        val baseUri = Uri.parse(WHATSAPP_URL)
        return if (includeDefaultMessage) {
            baseUri.buildUpon()
                .appendQueryParameter("text", WHATSAPP_INITIAL_MESSAGE)
                .build()
        } else {
            baseUri
        }
    }

    fun openWhatsAppSupport(context: Context) {
        val uriWithMessage = buildWhatsAppUri(includeDefaultMessage = true)
        val fallbackUri = Uri.parse(WHATSAPP_URL)

        // 1. Try opening standard WhatsApp app directly
        if (tryLaunchApp(context, uriWithMessage, WHATSAPP_PACKAGE)) {
            return
        }

        // 2. Try opening WhatsApp Business app if installed
        if (tryLaunchApp(context, uriWithMessage, WHATSAPP_BUSINESS_PACKAGE)) {
            return
        }

        // 3. Fallback: open https://wa.me/252637864155 (with message) in browser
        if (tryLaunchBrowser(context, uriWithMessage)) {
            return
        }

        // 4. Final fallback: open exact https://wa.me/252637864155 in browser
        tryLaunchBrowser(context, fallbackUri)
    }

    private fun tryLaunchApp(context: Context, uri: Uri, packageName: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage(packageName)
                if (context !is Activity) {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
            context.startActivity(intent)
            true
        } catch (e: ActivityNotFoundException) {
            false
        } catch (e: Exception) {
            false
        }
    }

    private fun tryLaunchBrowser(context: Context, uri: Uri): Boolean {
        return try {
            val browserIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                if (context !is Activity) {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
            context.startActivity(browserIntent)
            true
        } catch (e: Exception) {
            false
        }
    }
}
