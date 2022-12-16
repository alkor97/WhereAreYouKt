package info.alkor.whereareyou.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PackageInfoFlags
import android.os.Build
import androidx.core.app.ActivityCompat
import info.alkor.whereareyou.R
import info.alkor.whereareyou.impl.context.AppContext

class PermissionRequester(private val context: Context) {

    private val permissionAccessor by lazy { (context.applicationContext as AppContext).permissionAccessor }

    init {
        getRequiredPermissions()
    }

    fun ensurePermissionsGranted(activity: Activity) = requestNotGrantedPermissions(activity)

    fun canLocate() = permissionAccessor.isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)

    private fun requestNotGrantedPermissions(activity: Activity) {
        showExplanations(activity, *getRequiredPermissions().filter { !permissionAccessor.isPermissionGranted(it) }.toTypedArray())
    }

    private fun showExplanations(activity: Activity, vararg permissions: String) {
        if (permissions.isNotEmpty()) {
            val listEntrySeparator = "\n" + context.getString(R.string.permission_entry_list_entry_prefix)
            val message = permissions.map { permission ->
                usedPermissions[permission]?.let {
                    context.getString(it.rationale)
                } ?: permission
            }.distinct().joinToString(listEntrySeparator, context.getString(R.string.permission_required) + listEntrySeparator)

            if (message.isNotEmpty()) {
                with(AlertDialog.Builder(activity)) {
                    setTitle(R.string.permission_needed_title)
                    setMessage(message)
                    setPositiveButton(android.R.string.ok) { _, _ ->
                        doRequestPermissions(activity, *permissions)
                    }
                    create()
                    show()
                }
            }
        }
    }

    private fun doRequestPermissions(activity: Activity, vararg permissions: String) {
        ActivityCompat.requestPermissions(activity, permissions, 1)
    }

    private data class Details(val rationale: Int, val since: Int = Build.VERSION_CODES.BASE)
    companion object {
        @SuppressLint("InlinedApi")
        private val usedPermissions = hashMapOf(
                Manifest.permission.ACCESS_FINE_LOCATION to Details(R.string.location_permission_needed),
                Manifest.permission.ACCESS_COARSE_LOCATION to Details(R.string.location_permission_needed),
                Manifest.permission.RECEIVE_SMS to Details(R.string.receive_sms_permission_needed),
                Manifest.permission.READ_CONTACTS to Details(R.string.read_contacts_permission_needed),
                Manifest.permission.SEND_SMS to Details(R.string.send_sms_permission_needed),
                Manifest.permission.READ_PHONE_STATE to Details(R.string.read_phone_state_permission_needed),
                Manifest.permission.ACCESS_BACKGROUND_LOCATION to Details(R.string.location_permission_needed, Build.VERSION_CODES.Q),
                Manifest.permission.FOREGROUND_SERVICE to Details(R.string.foreground_service_permission_needed, Build.VERSION_CODES.P),
                Manifest.permission.POST_NOTIFICATIONS to Details(R.string.post_notifications_permission_needed, Build.VERSION_CODES.TIRAMISU)
        )
    }

    private fun getRequiredPermissions() = context.packageManager
            .getPackageInfoCompat(context.packageName, PackageManager.GET_PERMISSIONS)
            .requestedPermissions
            .filter {
                Build.VERSION.SDK_INT >= (usedPermissions[it]?.since ?: Build.VERSION.SDK_INT)
            }
}

fun PackageManager.getPackageInfoCompat(packageName: String, flags: Int = 0): PackageInfo =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getPackageInfo(packageName, PackageInfoFlags.of(flags.toLong()))
    } else {
        @Suppress("DEPRECATION") getPackageInfo(packageName, flags)
    }
