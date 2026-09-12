package com.czcz.myapp.ui

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.czcz.myapp.R


@Composable
fun Avatar(
    url: String?,
    size: Dp,
    modifier: Modifier = Modifier,
    contentDescription: String = "头像"
) {
    val default = painterResource(R.drawable.ic_default_avatar)
    AsyncImage(
        model = url?.takeIf { it.isNotBlank() },
        contentDescription = contentDescription,
        placeholder = default,
        error = default,
        fallback = default,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
    )
}
