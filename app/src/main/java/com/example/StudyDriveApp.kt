package com.example

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.recaptcha.RecaptchaAppCheckProviderFactory

/**
 * Application entry point. Initializes Firebase App Check so Firestore/Storage
 * requests from this client carry a valid attestation token.
 *
 * - Debug builds: Debug provider (approve tokens in Firebase Console → App Check → Apps → debug tokens).
 * - Release builds: reCAPTCHA (Android) provider using the site key below.
 *   Create an Android reCAPTCHA Enterprise/Classic site key in Google Cloud Console and paste it here.
 */
class StudyDriveApp : Application() {

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        initAppCheck()
    }

    private fun initAppCheck() {
        try {
            val firebaseApp = FirebaseApp.getInstance()
            val appCheck = FirebaseAppCheck.getInstance(firebaseApp)
            if (BuildConfig.DEBUG) {
                appCheck.installAppCheckProviderFactory(
                    DebugAppCheckProviderFactory.getInstance()
                )
            } else {
                if (RECAPTCHA_SITE_KEY.isBlank()) {
                    Log.w(TAG, "RECAPTCHA_SITE_KEY is not set; App Check attestation disabled for release builds. " +
                            "Set it before shipping, or enforcement stays off.")
                    return
                }
                appCheck.installAppCheckProviderFactory(
                    RecaptchaAppCheckProviderFactory.getInstance(RECAPTCHA_SITE_KEY)
                )
            }
            Log.i(TAG, "Firebase App Check initialized (${if (BuildConfig.DEBUG) "debug" else "recaptcha"} provider)")
        } catch (e: Exception) {
            // Never crash the app because of attestation setup problems.
            Log.e(TAG, "Failed to initialize App Check: ${e.message}", e)
        }
    }

    companion object {
        private const val TAG = "StudyDriveApp"

        /**
         * reCAPTCHA Android site key from Firebase Console → App Check → Apps → register app.
         * TODO: replace with the real site key before releasing.
         */
        private const val RECAPTCHA_SITE_KEY = ""
    }
}
