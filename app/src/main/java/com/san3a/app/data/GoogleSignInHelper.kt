package com.san3a.app.data

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

class GoogleSignInHelper(private val context: Context) {

    /**
     * يعيد idToken الخاص بـ Google أو يرمي استثناءً يحمل سببًا واضحًا.
     */
    suspend fun getIdToken(webClientId: String): String {
        require(webClientId.isNotBlank()) { "معرّف عميل Google غير مضبوط" }

        val credentialManager = CredentialManager.create(context)
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val result = try {
            credentialManager.getCredential(context, request)
        } catch (e: GetCredentialException) {
            throw IllegalStateException(mapError(e), e)
        }

        val credential = result.credential
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            return GoogleIdTokenCredential.createFrom(credential.data).idToken
        }
        throw IllegalStateException("نوع بيانات الدخول غير مدعوم")
    }

    private fun mapError(e: GetCredentialException): String {
        val type = e.type.orEmpty()
        return when {
            type.contains("NO_CREDENTIAL") ->
                "لا يوجد حساب Google على الجهاز. أضف حسابًا من إعدادات الجهاز."
            type.contains("CANCELED") ->
                "تم إلغاء تسجيل الدخول."
            type.contains("INTERRUPTED") ->
                "تمت مقاطعة العملية. أعد المحاولة."
            type.contains("PROVIDER_CONFIGURATION") || type.contains("GET_CREDENTIAL") ->
                "إعدادات Google غير مكتملة. راجع البصمة SHA-1 ومعرّف Web client."
            else -> "تعذّر تسجيل الدخول بGoogle: ${e.message ?: type}"
        }
    }
}
