package com.san3a.app.ui.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.san3a.app.BuildConfig
import com.san3a.app.data.AuthRepository
import com.san3a.app.data.GoogleSignInHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class AuthStep { PHONE, CODE }

class AuthViewModel : ViewModel() {

    private val repo = AuthRepository()

    private val _phone = MutableStateFlow("+966")
    val phone: StateFlow<String> = _phone.asStateFlow()

    private val _code = MutableStateFlow("")
    val code: StateFlow<String> = _code.asStateFlow()

    private val _step = MutableStateFlow(AuthStep.PHONE)
    val step: StateFlow<AuthStep> = _step.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    private var verificationId: String? = null

    fun onPhoneChange(value: String) {
        _phone.value = value.filter { it == '+' || it.isDigit() }.take(15)
        _error.value = null
    }

    fun onCodeChange(value: String) {
        _code.value = value.filter { it.isDigit() }.take(6)
        _error.value = null
    }

    fun backToPhone() {
        _step.value = AuthStep.PHONE
        _code.value = ""
        verificationId = null
        _error.value = null
    }

    fun signInWithGoogle(activity: Activity) {
        val webClientId = resolveWebClientId(activity)
        if (webClientId.isBlank()) {
            _error.value = "لم يتم ضبط معرّف عميل Google (GOOGLE_WEB_CLIENT_ID). راجع دليل النشر."
            return
        }
        _busy.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                val idToken = GoogleSignInHelper(activity).getIdToken(webClientId)
                repo.signInWithGoogleIdToken(idToken).addOnCompleteListener { task ->
                    _busy.value = false
                    if (!task.isSuccessful) {
                        _error.value = task.exception?.localizedMessage ?: "فشل الدخول بحساب Google"
                    }
                }
            } catch (e: Exception) {
                _busy.value = false
                _error.value = e.localizedMessage ?: "تعذّر الدخول بحساب Google"
            }
        }
    }

    private fun resolveWebClientId(activity: Activity): String {
        if (BuildConfig.GOOGLE_WEB_CLIENT_ID.isNotBlank()) {
            return BuildConfig.GOOGLE_WEB_CLIENT_ID
        }
        return try {
            val resId = activity.resources.getIdentifier(
                "default_web_client_id", "string", activity.packageName
            )
            if (resId != 0) activity.getString(resId) else ""
        } catch (e: Exception) {
            ""
        }
    }

    fun sendCode(activity: Activity) {
        val number = _phone.value.trim()
        if (number.length < 10) {
            _error.value = "أدخل رقم هاتف صحيح مع رمز الدولة، مثال: +9665xxxxxxxx"
            return
        }
        _busy.value = true
        _error.value = null
        _code.value = ""
        verificationId = null
        repo.startVerification(activity, number, object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                repo.signInWithCredential(credential).addOnCompleteListener { task ->
                    _busy.value = false
                    if (!task.isSuccessful) {
                        _error.value = task.exception?.localizedMessage ?: "فشل الدخول التلقائي"
                    }
                }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                _busy.value = false
                _error.value = e.localizedMessage ?: "تعذّر إرسال رمز التحقق"
            }

            override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
                _busy.value = false
                verificationId = id
                _step.value = AuthStep.CODE
            }
        })
    }

    fun verifyCode() {
        val id = verificationId
        if (id == null) {
            _error.value = "انتهت صلاحية الرمز، أعد الإرسال"
            return
        }
        if (_code.value.length < 6) {
            _error.value = "أدخل الرمز المكوّن من ٦ أرقام"
            return
        }
        _busy.value = true
        repo.signInWithCode(id, _code.value.trim()).addOnCompleteListener { task ->
            _busy.value = false
            if (!task.isSuccessful) {
                _error.value = task.exception?.localizedMessage ?: "الرمز غير صحيح"
            }
        }
    }
}
