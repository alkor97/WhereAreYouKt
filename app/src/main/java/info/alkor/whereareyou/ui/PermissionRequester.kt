package info.alkor.whereareyou.ui

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import info.alkor.whereareyou.R
import info.alkor.whereareyou.impl.context.AppContext

class PermissionRequester(private val context: Context) {

    private val permissionAccessor by lazy { (context.applicationContext as AppContext).permissionAccessor }

    init {
        for (permission in getRequiredPermissions()) {
            if (permission !in permissionRationale) {
                throw IllegalStateException("Permission $permission not mapped.")
            }
        }
    }

    fun ensurePermissionsGranted(activity: Activity) = requestNotGrantedPermissions(activity)

    fun canLocate() = permissionAccessor.isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)

    private fun requestNotGrantedPermissions(activity: Activity) {
        showExplanations(activity, *getRequiredPermissions().filter { !permissionAccessor.isPermissionGranted(it) }.toTypedArray())
    }

    private fun showExplanations(activity: Activity, vararg permissions: String) {
        if (!permissions.isEmpty()) {
            val listEntrySeparator = "\n" + context.getString(R.string.permission_entry_list_entry_prefix)
            val message = permissions.mapNotNull { permission ->
                permissionRationale[permission]?.let {
                    context.getString(it)
                }
            }.distinct().joinToString(listEntrySeparator, context.getString(R.string.permission_required) + listEntrySeparator)

            if (!message.isEmpty()) {
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

    companion object {
        val permissionRationale = hashMapOf(
                Manifest.permission.ACCESS_FINE_LOCATION to R.string.location_permission_needed,
                Manifest.permission.ACCESS_COARSE_LOCATION to R.string.location_permission_needed,
                "android.permission.ACCESS_BACKGROUND_LOCATION" to R.string.location_permission_needed,
                Manifest.permission.RECEIVE_SMS to R.string.receive_sms_permission_needed,
                Manifest.permission.READ_CONTACTS to R.string.read_contacts_permission_needed,
                Manifest.permission.SEND_SMS to R.string.send_sms_permission_needed,
                Manifest.permission.READ_PHONE_STATE to R.string.read_phone_state_permission_needed
        )
    }

    private fun getRequiredPermissions() = context.packageManager
            .getPackageInfo(context.packageName, PackageManager.GET_PERMISSIONS)
            .requestedPermissions
}
