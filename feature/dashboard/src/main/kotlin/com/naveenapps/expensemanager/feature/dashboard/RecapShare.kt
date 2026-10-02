package com.naveenapps.expensemanager.feature.dashboard

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import androidx.core.content.FileProvider
import androidx.core.graphics.toColorInt
import com.naveenapps.expensemanager.core.model.MonthlyRecap
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.roundToInt

/** Play Store listing of the release app (not the ".debug" package a debug build runs as). */
private const val PLAY_STORE_URL =
    "https://play.google.com/store/apps/details?id=com.naveenapps.expensemanager"

// Website palette (dark theme), so shared images look like the brand.
private const val BG = "#0E0F14"
private const val CARD = "#181A24"
private const val ACCENT = "#22C97A"
private const val INK = "#F0F1F5"
private const val INK_2 = "#A8AABC"
private const val TRACK = "#262833"

private const val WIDTH = 1080
private const val HEIGHT = 1350

/** Localised "September 2026" for the recap's month. */
internal fun MonthlyRecap.monthLabel(): String =
    SimpleDateFormat("LLLL yyyy", Locale.getDefault()).format(month)

/**
 * Draws the shareable recap image. Privacy first: it shows each category's *share* of spending
 * and how many days were logged — never an amount — so people can post it without exposing
 * what they spend.
 */
internal fun renderRecapImage(context: Context, recap: MonthlyRecap): File {
    val bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawColor(BG.toColorInt())

    val pad = 88f
    val text = Paint(Paint.ANTI_ALIAS_FLAG)
    val fill = Paint(Paint.ANTI_ALIAS_FLAG)

    // Eyebrow + title
    text.apply { color = ACCENT.toColorInt(); textSize = 40f; typeface = Typeface.DEFAULT_BOLD }
    canvas.drawText(context.getString(R.string.recap_image_eyebrow).uppercase(), pad, 190f, text)
    text.apply { color = INK.toColorInt(); textSize = 84f }
    canvas.drawText(recap.monthLabel(), pad, 300f, text)

    // Days-logged headline
    text.apply { color = INK_2.toColorInt(); textSize = 44f; typeface = Typeface.DEFAULT }
    canvas.drawText(
        context.getString(R.string.recap_image_days_logged, recap.daysLogged),
        pad,
        380f,
        text,
    )

    // Category card
    val cardTop = 450f
    val rowHeight = 150f
    val cardBottom = cardTop + 80f + rowHeight * recap.topCategories.size
    fill.color = CARD.toColorInt()
    canvas.drawRoundRect(RectF(pad - 24f, cardTop, WIDTH - pad + 24f, cardBottom), 48f, 48f, fill)

    text.apply { color = INK_2.toColorInt(); textSize = 36f }
    canvas.drawText(context.getString(R.string.recap_image_where_it_went), pad + 24f, cardTop + 76f, text)

    val barLeft = pad + 24f
    val barRight = WIDTH - pad - 24f
    recap.topCategories.forEachIndexed { index, item ->
        val y = cardTop + 80f + rowHeight * index + 70f
        val name = item.category.titleResId?.let { context.getString(it) } ?: item.category.name
        val percent = "${(item.share * 100).roundToInt()}%"

        text.apply { color = INK.toColorInt(); textSize = 44f; typeface = Typeface.DEFAULT_BOLD }
        canvas.drawText(name, barLeft, y, text)
        val percentWidth = text.measureText(percent)
        canvas.drawText(percent, barRight - percentWidth, y, text)

        // Bar in the category's own colour, on a dark track.
        val barTop = y + 26f
        fill.color = TRACK.toColorInt()
        canvas.drawRoundRect(RectF(barLeft, barTop, barRight, barTop + 20f), 10f, 10f, fill)
        fill.color = runCatching { item.category.storedIcon.backgroundColor.toColorInt() }
            .getOrDefault(ACCENT.toColorInt())
        val end = barLeft + (barRight - barLeft) * item.share.coerceIn(0.02f, 1f)
        canvas.drawRoundRect(RectF(barLeft, barTop, end, barTop + 20f), 10f, 10f, fill)
    }

    // Footer: brand + call to action
    text.apply { color = ACCENT.toColorInt(); textSize = 48f; typeface = Typeface.DEFAULT_BOLD }
    canvas.drawText(context.getString(R.string.recap_image_brand), pad, HEIGHT - 150f, text)
    text.apply { color = INK_2.toColorInt(); textSize = 34f; typeface = Typeface.DEFAULT }
    canvas.drawText(context.getString(R.string.recap_image_footer), pad, HEIGHT - 95f, text)

    val dir = File(context.cacheDir, RECAP_CACHE_DIR).apply { mkdirs() }
    val file = File(dir, "recap_${recap.monthKey}.png")
    file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    bitmap.recycle()
    return file
}

/** Opens the share sheet with the recap image plus a short message and the store link. */
internal fun shareRecapImage(context: Context, file: File, recap: MonthlyRecap) {
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(
            Intent.EXTRA_TEXT,
            context.getString(R.string.recap_share_text, recap.monthLabel(), PLAY_STORE_URL),
        )
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    try {
        context.startActivity(
            Intent.createChooser(intent, context.getString(R.string.recap_share_chooser))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    } catch (_: ActivityNotFoundException) {
        // No app can receive a share; nothing useful to do.
    }
}

/** Must match the cache-path in app/src/main/res/xml/provider_paths.xml. */
internal const val RECAP_CACHE_DIR = "recaps"
