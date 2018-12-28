package info.alkor.whereareyou.impl.context

import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat
import info.alkor.whereareyou.api.context.PermissionAccessor

class PermissionAccessorImpl(private val ctx: Context) : PermissionAccessor {
    override fun isPermissionGranted(permission: String) = ContextCompat.checkSelfPermission(ctx, permission) == PackageManager.PERMISSION_GRANTED
}