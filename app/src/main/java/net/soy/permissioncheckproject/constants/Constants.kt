package net.soy.permissioncheckproject.constants

import net.soy.permissioncheckproject.BuildConfig


interface Constants {
    companion object {
        const val PERMISSIONS_REQUEST_READ_EXT_STORAGE = 1000
        const val PERMISSIONS_REQUEST_CAMERA = 1001

        const val AUTHORITY = BuildConfig.APPLICATION_ID + ".provider"
    }
}