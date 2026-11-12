package com.san3a.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.san3a.app.data.AuthRepository
import com.san3a.app.data.LocationProvider
import com.san3a.app.data.UserProfile
import com.san3a.app.data.UserRepository
import com.san3a.app.data.normalizePhone
import com.san3a.app.data.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SetupUiState(
    val role: UserRole = UserRole.CUSTOMER,
    val name: String = "",
    val phone: String = "",
    val whatsapp: String = "",
    val craft: String = "",
    val region: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val saving: Boolean = false,
    val error: String? = null
) {
    val hasLocation: Boolean get() = latitude != 0.0 || longitude != 0.0
}

class SetupViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = UserRepository()
    private val auth = AuthRepository()
    private val locationProvider = LocationProvider(application)

    private val _ui = MutableStateFlow(SetupUiState())
    val ui: StateFlow<SetupUiState> = _ui.asStateFlow()

    fun setRole(role: UserRole) {
        _ui.value = _ui.value.copy(role = role, error = null)
    }

    fun onName(value: String) {
        _ui.value = _ui.value.copy(name = value, error = null)
    }

    fun onPhone(value: String) {
        _ui.value = _ui.value.copy(phone = normalizePhone(value), error = null)
    }

    fun onWhatsapp(value: String) {
        _ui.value = _ui.value.copy(whatsapp = normalizePhone(value), error = null)
    }

    fun onCraft(value: String) {
        _ui.value = _ui.value.copy(craft = value, error = null)
    }

    fun onRegion(value: String) {
        _ui.value = _ui.value.copy(region = value, error = null)
    }

    fun captureLocation() {
        viewModelScope.launch {
            val location = locationProvider.currentLocation()
            if (location != null) {
                _ui.value = _ui.value.copy(
                    latitude = location.first,
                    longitude = location.second,
                    error = null
                )
            } else {
                _ui.value = _ui.value.copy(error = "تعذّر تحديد الموقع، تأكد من تفعيل GPS والصلاحية")
            }
        }
    }

    fun save() {
        val current = _ui.value
        val user = auth.currentUser()
        if (user == null) {
            _ui.value = current.copy(error = "انتهت الجلسة، سجّل الدخول من جديد")
            return
        }
        if (current.name.isBlank()) {
            _ui.value = current.copy(error = "أدخل الاسم")
            return
        }
        val authPhone = normalizePhone(user.phoneNumber.orEmpty())
        val phone = authPhone.ifBlank { current.phone }
        if (phone.isBlank()) {
            _ui.value = current.copy(error = "أدخل رقم الجوال")
            return
        }
        if (current.role == UserRole.CRAFTSMAN && current.craft.isBlank()) {
            _ui.value = current.copy(error = "اختر الحرفة")
            return
        }
        if (current.role == UserRole.CRAFTSMAN && current.region.isBlank()) {
            _ui.value = current.copy(error = "اختر منطقة العمل")
            return
        }

        val whatsapp = current.whatsapp.ifBlank { phone }

        _ui.value = current.copy(saving = true, error = null)
        viewModelScope.launch {
            try {
                val profile = UserProfile(
                    uid = user.uid,
                    name = current.name.trim(),
                    phone = phone,
                    whatsapp = whatsapp,
                    role = current.role.value,
                    craft = if (current.role == UserRole.CRAFTSMAN) current.craft else "",
                    region = if (current.role == UserRole.CRAFTSMAN) current.region else "",
                    available = true,
                    createdAt = System.currentTimeMillis(),
                    latitude = current.latitude,
                    longitude = current.longitude
                )
                repo.saveProfile(profile)
                _ui.value = _ui.value.copy(saving = false, error = null)
            } catch (e: Exception) {
                _ui.value = _ui.value.copy(saving = false, error = e.localizedMessage ?: "تعذّر الحفظ")
            }
        }
    }

}
