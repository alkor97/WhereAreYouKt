package info.alkor.whereareyou.impl.context

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

class PermissionAccessor(private val ctx: Context) {
    fun isPermissionGranted(permission: String) = ContextCompat.checkSelfPermission(ctx, permission) == PackageManager.PERMISSION_GRANTED
}