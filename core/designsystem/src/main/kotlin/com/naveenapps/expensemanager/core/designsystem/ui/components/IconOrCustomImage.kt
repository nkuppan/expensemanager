package com.naveenapps.expensemanager.core.designsystem.ui.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import coil.compose.AsyncImage
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme
import com.naveenapps.expensemanager.core.designsystem.ui.extensions.getDrawable
import java.io.File

/**
 * Single shared "photo or icon" content renderer, used everywhere an entity (category, account,
 * etc.) shows either a user-picked/captured [customImagePath] photo or its fallback vector [icon].
 *
 * A non-null [customImagePath] always renders as a plain (untinted, uncolored) circular photo
 * crop — see `IconAndColorComponent` for where this visual rule was decided. Callers only need to
 * provide the surrounding background/shape (a tinted circle, a rounded tile, etc.); this
 * composable is just the foreground content and fills whatever [modifier] it's given.
 */
@Composable
fun IconOrCustomImage(
    icon: String,
    modifier: Modifier = Modifier,
    customImagePath: String? = null,
    contentDescription: String? = null,
    tint: Color = Color.White,
) {
    val context = LocalContext.current

    if (customImagePath != null) {
        AsyncImage(
            model = File(customImagePath),
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = modifier.clip(EmTheme.shapes.pill),
        )
    } else {
        Image(
            modifier = modifier,
            imageVector = ImageVector.vectorResource(id = context.getDrawable(icon)),
            colorFilter = ColorFilter.tint(color = tint),
            contentDescription = contentDescription,
        )
    }
}
