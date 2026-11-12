package com.san3a.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.san3a.app.data.LocationProvider
import com.san3a.app.data.UserProfile
import com.san3a.app.data.UserRepository
import com.san3a.app.data.distanceKm
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CraftsmanItem(
    val profile: UserProfile,
    val distanceKm: Double?
)

data class HomeUi(
    val items: List<CraftsmanItem> = emptyList(),
    val loading: Boolean = true,
    val nearMe: Boolean = false
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = UserRepository()
    private val locationProvider = LocationProvider(application)

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _craft = MutableStateFlow<String?>(null)
    val craft: StateFlow<String?> = _craft.asStateFlow()

    private val _region = MutableStateFlow<String?>(null)
    val region: StateFlow<String?> = _region.asStateFlow()

    private val _nearMe = MutableStateFlow(false)
    val nearMe: StateFlow<Boolean> = _nearMe.asStateFlow()

    private val _userLocation = MutableStateFlow<Pair<Double, Double>?>(null)

    private val source = repo.observeCraftsmen()

    private val filteredFlow = combine(source, _query, _craft, _region) { list, q, c, r ->
        list.filter { p ->
            (c == null || p.craft == c) &&
                (r == null || p.region == r) &&
                (
                    q.isBlank() ||
                        p.name.contains(q, ignoreCase = true) ||
                        p.craft.contains(q, ignoreCase = true) ||
                        p.region.contains(q, ignoreCase = true)
                    )
        }
    }

    val ui: StateFlow<HomeUi> =
        combine(filteredFlow, _nearMe, _userLocation) { filtered, near, loc ->
            val items = filtered.map { profile ->
                val distance = if (loc != null && profile.hasLocation) {
                    distanceKm(loc.first, loc.second, profile.latitude, profile.longitude)
                } else {
                    null
                }
                CraftsmanItem(profile, distance)
            }

            val sorted = when {
                near && loc != null -> items.sortedWith(
                    compareBy(
                        { it.distanceKm == null },
                        { it.distanceKm ?: Double.MAX_VALUE },
                        { !it.profile.available }
                    )
                )

                else -> items.sortedWith(
                    compareByDescending<CraftsmanItem> { it.profile.available }
                        .thenByDescending { it.profile.ratingAvg }
                        .thenBy { it.profile.name }
                )
            }

            HomeUi(items = sorted, loading = false, nearMe = near && loc != null)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUi(loading = true)
        )

    fun onQuery(value: String) {
        _query.value = value
    }

    fun setCraft(value: String?) {
        _craft.value = if (value == _craft.value) null else value
    }

    fun setRegion(value: String?) {
        _region.value = if (value == _region.value) null else value
    }

    fun enableNearMe() {
        _nearMe.value = true
        viewModelScope.launch {
            val location = locationProvider.currentLocation()
            if (location == null) {
                _nearMe.value = false
            } else {
                _userLocation.value = location
            }
        }
    }

    fun disableNearMe() {
        _nearMe.value = false
        _userLocation.value = null
    }

    fun toggleNearMe() {
        if (_nearMe.value) disableNearMe() else enableNearMe()
    }
}
