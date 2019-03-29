package info.alkor.whereareyou.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AlertDialog
import info.alkor.whereareyou.api.context.AppContext
import info.alkor.whereareyoukt.R

class PermissionRequester(private val context: Context) {

    private val permissionAccessor by lazy { (context.applicationContext as AppContext).permissionAccessor }

    init {
        for (permission in getRequestedPermissions()) {
            if (permission !in permissionRationale) {
                throw IllegalStateException("Permission $permission not mapped.")
            }
            getRequestCode(permission)
        }
    }

    fun ensurePermissionsGranted(activity: Activity) = requestPermissions(activity, *permissionRationale.keys.toTypedArray())

    fun canLocate() = permissionAccessor.isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)

    private fun requestPermissions(activity: Activity, vararg permissions: String) {
        val missingPermissions = permissions.filter {
            !permissionAccessor.isPermissionGranted(it)
        }
        for (permission in missingPermissions) {
            //if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
            showExplanation(activity, permission)
            /*} else {
                doRequestPermissions(activity, permission)
            }*/
            break
        }
    }

    private fun showExplanation(activity: Activity, vararg permissions: String) {
        permissions.map { permission ->
            Pair(permission, permissionRationale[permission])
        }.forEach { (permission, messageId) ->
            with(AlertDialog.Builder(activity)) {
                setTitle(R.string.permission_needed_title)
                setMessage(messageId!!)
                setPositiveButton(android.R.string.ok) { _, _ ->
                    doRequestPermissions(activity, permission)
                }
                create()
                show()
            }
        }
    }

    private fun doRequestPermissions(activity: Activity, permission: String) {
        ActivityCompat.requestPermissions(activity, arrayOf(permission), getRequestCode(permission))
    }

    companion object {
        val permissionRationale = hashMapOf(
                Manifest.permission.ACCESS_FINE_LOCATION to R.string.location_permission_needed,
                Manifest.permission.RECEIVE_SMS to R.string.receive_sms_permission_needed,
                Manifest.permission.READ_CONTACTS to R.string.read_contacts_permission_needed,
                Manifest.permission.SEND_SMS to R.string.send_sms_permission_needed,
                Manifest.permission.READ_PHONE_STATE to R.string.read_phone_state_permission_needed
        )
    }

    private fun getRequestCode(permission: String) = when (permission) {
        Manifest.permission.ACCESS_FINE_LOCATION -> 1
        Manifest.permission.RECEIVE_SMS -> 2
        Manifest.permission.READ_CONTACTS -> 4
        Manifest.permission.SEND_SMS -> 8
        Manifest.permission.READ_PHONE_STATE -> 16
        else -> throw IllegalArgumentException("Permission $permission has no unique request code.")
    }

    private fun getRequestedPermissions() = context.packageManager
            .getPackageInfo(context.packageName, PackageManager.GET_PERMISSIONS)
            .requestedPermissions
}
