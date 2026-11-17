package com.naveenapps.expensemanager.core.designsystem.ui.utils

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/** The two entry points [IconAndColorComponent]'s Photo sheet needs — everything else (launcher
 * registration, the pending capture Uri, the runtime permission check) is handled internally. */
data class ImagePickerActions(
    val onCaptureRequested: () -> Unit,
    val onGalleryRequested: () -> Unit,
)

/**
 * Wires up the camera-capture + gallery-pick flow shared by every "attach a custom photo" screen
 * (category, account, ...): registers the three launchers this needs (camera, camera permission,
 * gallery), tracks the pending capture destination between "camera launched" and "camera
 * returned", and requests the CAMERA permission on demand rather than upfront.
 *
 * @param createCaptureUri destination Uri for a full-resolution camera capture (see
 * `ImageStorageRepository.createImageCaptureUri`).
 * @param onImagePicked called with the resulting Uri once a gallery pick or camera capture
 * completes successfully.
 */
@Composable
fun rememberImagePickerActions(
    createCaptureUri: () -> Uri,
    onImagePicked: (Uri) -> Unit,
): ImagePickerActions {
    val context = LocalContext.current
    var pendingCaptureUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
    ) { success ->
        if (success) {
            pendingCaptureUri?.let(onImagePicked)
        }
        pendingCaptureUri = null
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            val uri = createCaptureUri()
            pendingCaptureUri = uri
            // No camera app (e.g. camera disabled by a device policy): nothing to open.
            runCatching { cameraLauncher.launch(uri) }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        uri?.let(onImagePicked)
    }

    return ImagePickerActions(
        onCaptureRequested = {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA,
            ) == PackageManager.PERMISSION_GRANTED
            if (hasPermission) {
                val uri = createCaptureUri()
                pendingCaptureUri = uri
                runCatching { cameraLauncher.launch(uri) }
            } else {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        },
        onGalleryRequested = {
            runCatching {
                galleryLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                )
            }
        },
    )
}
