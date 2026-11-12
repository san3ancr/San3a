package com.san3a.app.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.san3a.app.data.AuthRepository
import com.san3a.app.data.UserProfile
import com.san3a.app.data.UserRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface SessionState {
    data object Loading : SessionState
    data object LoggedOut : SessionState
    data class NeedsProfile(val uid: String, val phone: String) : SessionState
    data class Ready(val profile: UserProfile) : SessionState
}

class SessionViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepo = AuthRepository()
    private val userRepo = UserRepository()

    private val _state = MutableStateFlow<SessionState>(SessionState.Loading)
    val state: StateFlow<SessionState> = _state.asStateFlow()

    private var profileJob: Job? = null

    init {
        viewModelScope.launch {
            authRepo.authStateFlow().collect { user ->
                profileJob?.cancel()
                if (user == null) {
                    _state.value = SessionState.LoggedOut
                } else {
                    _state.value = SessionState.Loading
                    viewModelScope.launch {
                        val prefs = getApplication<Application>()
                            .getSharedPreferences("san3a_prefs", Context.MODE_PRIVATE)
                        val pending = prefs.getString("pending_fcm_token", null)
                        if (!pending.isNullOrBlank()) {
                            runCatching { userRepo.updateFcmToken(user.uid, pending) }
                            prefs.edit().remove("pending_fcm_token").apply()
                        }
                    }
                    profileJob = viewModelScope.launch {
                        userRepo.observeProfile(user.uid).collect { profile ->
                            _state.value = if (profile == null || profile.name.isBlank()) {
                                SessionState.NeedsProfile(user.uid, user.phoneNumber.orEmpty())
                            } else {
                                SessionState.Ready(profile)
                            }
                        }
                    }
                }
            }
        }
    }

    fun signOut() {
        authRepo.signOut()
    }
}
