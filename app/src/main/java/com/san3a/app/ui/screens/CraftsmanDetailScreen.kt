package com.san3a.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.san3a.app.data.Review
import com.san3a.app.data.UserProfile
import com.san3a.app.data.UserRepository
import com.san3a.app.ui.ContactActions
import com.san3a.app.ui.WhatsAppIcon
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CraftsmanDetailScreen(
    profile: UserProfile,
    currentUser: UserProfile,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ملف الحرفي") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(88.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = profile.name.trim().take(1),
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = profile.name,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(6.dp))
                    RatingLine(ratingAvg = profile.ratingAvg, count = profile.ratingCount)
                    Spacer(Modifier.height(8.dp))
                    AvailabilityBadge(available = profile.available)
                    Spacer(Modifier.height(16.dp))
                    DetailRow(icon = { Icon(Icons.Filled.Badge, null) }, label = "الحرفة", value = profile.craft)
                    Spacer(Modifier.height(8.dp))
                    DetailRow(icon = { Icon(Icons.Filled.LocationOn, null) }, label = "المنطقة", value = profile.region)
                    Spacer(Modifier.height(8.dp))
                    DetailRow(icon = { Icon(Icons.Filled.Phone, null) }, label = "الهاتف", value = profile.phone)
                }
            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = { ContactActions.call(context, profile.phone) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(Icons.Filled.Call, contentDescription = null)
                Spacer(Modifier.size(8.dp))
                Text("اتصال هاتفي", style = MaterialTheme.typography.labelLarge)
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = {
                    ContactActions.whatsapp(context, profile.whatsapp.ifBlank { profile.phone })
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = WhatsAppGreen,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                WhatsAppIcon()
                Spacer(Modifier.size(8.dp))
                Text("محادثة واتساب", style = MaterialTheme.typography.labelLarge)
            }

            Spacer(Modifier.height(24.dp))
            ReviewsSection(craftsman = profile, currentUser = currentUser)
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ReviewsSection(craftsman: UserProfile, currentUser: UserProfile) {
    val repository = remember { UserRepository() }
    val scope = rememberCoroutineScope()
    val reviews by repository.observeReviews(craftsman.uid)
        .collectAsStateWithLifecycle(initialValue = emptyList())

    var selectedStars by remember { mutableIntStateOf(5) }
    var comment by remember { mutableStateOf("") }
    var submitting by remember { mutableStateOf(false) }
    var reviewError by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "التقييمات والمراجعات",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(12.dp))

        if (currentUser.uid != craftsman.uid) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "قيّم هذا الحرفي",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(8.dp))
                    StarSelector(
                        rating = selectedStars,
                        onRatingChange = { selectedStars = it }
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = comment,
                        onValueChange = { comment = it },
                        label = { Text("اكتب مراجعتك (اختياري)") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = {
                            submitting = true
                            reviewError = null
                            scope.launch {
                                try {
                                    repository.submitReview(
                                        craftsman.uid,
                                        Review(
                                            userId = currentUser.uid,
                                            userName = currentUser.name,
                                            rating = selectedStars,
                                            comment = comment.trim(),
                                            createdAt = System.currentTimeMillis()
                                        )
                                    )
                                    comment = ""
                                    selectedStars = 5
                                } catch (e: Exception) {
                                    reviewError = e.localizedMessage ?: "تعذّر إرسال التقييم"
                                } finally {
                                    submitting = false
                                }
                            }
                        },
                        enabled = !submitting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        if (submitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("إرسال التقييم")
                        }
                    }
                    if (reviewError != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = reviewError ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        if (reviews.isEmpty()) {
            Text(
                text = "لا توجد مراجعات بعد. كن أول من يقيّم.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            reviews.forEach { review ->
                ReviewCard(review)
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun ReviewCard(review: Review) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = review.userName.ifBlank { "مستخدم" },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Row {
                    repeat(5) { index ->
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (index < review.rating) AmberStar
                            else MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
            if (review.comment.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = review.comment,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StarSelector(rating: Int, onRatingChange: (Int) -> Unit) {
    Row {
        for (index in 1..5) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = "نجمة $index",
                modifier = Modifier
                    .size(32.dp)
                    .clickable { onRatingChange(index) },
                tint = if (index <= rating) AmberStar
                else MaterialTheme.colorScheme.outline
            )
            Spacer(Modifier.size(4.dp))
        }
    }
}

@Composable
private fun DetailRow(
    icon: @Composable () -> Unit,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) { icon() }
        Spacer(Modifier.size(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(70.dp)
        )
        Text(
            text = value.ifBlank { "—" },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold
        )
    }
}
