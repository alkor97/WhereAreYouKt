package info.alkor.whereareyou.api.context

interface PermissionAccessor {
    fun isPermissionGranted(permission: String): Boolean
}
