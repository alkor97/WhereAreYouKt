package info.alkor.whereareyou.ui

import android.Manifest
import android.app.Activity
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AlertDialog
import info.alkor.whereareyou.api.context.AppContext
import info.alkor.whereareyoukt.R

class PermissionRequester(private val activity: Activity) {

    private val permissionAccessor by lazy { (activity.applicationContext as AppContext).permissionAccessor }

    companion object {
        val permissionRationale = hashMapOf(
                Manifest.permission.ACCESS_FINE_LOCATION to R.string.location_permission_needed,
                Manifest.permission.RECEIVE_SMS to R.string.receive_sms_permission_needed,
                Manifest.permission.READ_CONTACTS to R.string.read_contacts_permission_needed
        )
    }

    fun ensurePermissionsGranted() = requestPermissions(*permissionRationale.keys.toTypedArray())

    fun canLocate() = permissionAccessor.isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)

    private fun requestPermissions(vararg permissions: String) {
        permissionRationale
        val missingPermissions = permissions.filter {
            !permissionAccessor.isPermissionGranted(it)
        }
        for (permission in missingPermissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                showExplanation(permission)
            } else {
                doRequestPermissions(permission)
            }
        }
    }

    private fun showExplanation(vararg permissions: String) {
        permissions.map { permission ->
            Pair(permission, permissionRationale[permission])
        }.forEach { (permission, messageId) ->
            with(AlertDialog.Builder(activity)) {
                setTitle(R.string.permission_needed_title)
                setMessage(messageId!!)
                setPositiveButton(android.R.string.ok) { _, _ ->
                    doRequestPermissions(permission)
                }
                create()
                show()
            }
        }
    }

    private fun doRequestPermissions(permission: String) {
        ActivityCompat.requestPermissions(activity, arrayOf(permission), getRequestCode(permission))
    }

    private fun getRequestCode(permission: String) = when (permission) {
        Manifest.permission.ACCESS_FINE_LOCATION -> 1
        Manifest.permission.RECEIVE_SMS -> 2
        Manifest.permission.READ_CONTACTS -> 4
        else -> 0
    }
}
