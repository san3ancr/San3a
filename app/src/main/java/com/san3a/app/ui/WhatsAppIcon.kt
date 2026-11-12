package com.san3a.app.ui

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.san3a.app.R

@Composable
fun WhatsAppIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color.White
) {
    Icon(
        painter = painterResource(id = R.drawable.ic_whatsapp),
        contentDescription = null,
        tint = tint,
        modifier = modifier
    )
}
