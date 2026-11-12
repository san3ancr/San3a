package com.san3a.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.san3a.app.data.AuthRepository
import com.san3a.app.data.UserProfile
import com.san3a.app.data.UserRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileViewModel : ViewModel() {

    private val repo = UserRepository()
    private val auth = AuthRepository()

    fun toggleAvailability(profile: UserProfile) {
        viewModelScope.launch {
            runCatching { repo.updateAvailability(profile.uid, !profile.available) }
        }
    }

    fun signOut() {
        auth.signOut()
    }

    fun deleteAccount() {
        viewModelScope.launch {
            val user = auth.currentUser() ?: return@launch
            runCatching { repo.deleteProfile(user.uid) }
            runCatching { auth.deleteCurrentUser()?.await() }
            auth.signOut()
        }
    }
}
