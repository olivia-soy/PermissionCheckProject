package net.soy.permissioncheckproject.permission

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import net.soy.permissioncheckproject.R
import net.soy.permissioncheckproject.constants.Constants.Companion.PERMISSIONS_REQUEST_CAMERA
import net.soy.permissioncheckproject.constants.Constants.Companion.PERMISSIONS_REQUEST_READ_EXT_STORAGE

/**
 * 권한설정 클래스
 */
class PermissionCheck : Fragment() {

    /**
     * 카메라 또는 갤러리 선택 alert 창
     */
    fun cameraOrGallerySelectAlert(){
        Log.w(TAG, "cameraOrGallerySelectAlert()")
        val alertDialog = AlertDialog.Builder(mContext)
        alertDialog.setMessage(getString(R.string.permission_select_camera_gallery))
        alertDialog.setNegativeButton(R.string.btn_permission_camera) { _, _ ->requestCameraPermission()}
        alertDialog.setPositiveButton(R.string.btn_permission_gallery) { _, _ -> requestReadExternalStoragePermission() }
        alertDialog.show()
    }

    /**
     * 권한설정 허용 요청 alert 창
     */
    private fun showPopupForPermission() {
        Log.w(TAG, "showPopupForPermission()")
        val alertDialog = AlertDialog.Builder(mContext)
        when(mFromClass){
            FromClass.DEFAULT -> alertDialog.setMessage(getString(R.string.permission_grant_to_access_camera_gallery))
//            FromClass.MYMENU -> alertDialog.setMessage(getString(R.string.mymenu_profile_upload_permission_caution))
        }
        alertDialog.setPositiveButton(getString(R.string.btn_permission_setting)) { _, _ -> goSettingPermission() }
        alertDialog.setNegativeButton(getString(R.string.btn_cancel)) { _, _ -> mListener?.onError()}
        alertDialog.show()
    }

    /**
     * READ_EXTERNAL_STORAGE 권한 확인
     */
    fun requestReadExternalStoragePermission() {
        Log.d(TAG, "requestReadExternalStoragePermission()")
            if (ContextCompat.checkSelfPermission(mContext,  Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "#### READ_EXTERNAL_STORAGE, Permission 1")
                activity?.let {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        Log.e(TAG, "#### READ_EXTERNAL_STORAGE, Permission 2")
                        showPopupForPermission()
                    } else {
                        Log.e(TAG, "#### READ_EXTERNAL_STORAGE, Permission 3")
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_READ_EXT_STORAGE)
                        }
                    }
                }
            } else {
                Log.e(TAG, "#### READ_EXTERNAL_STORAGE, Permission 4")
                mListener?.onSuccess(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
    }

    /**
     * 권한설정으로 이동 하는 메소드
     */
    private fun goSettingPermission() {
        Log.w(TAG, "goSettingPermission()")
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.parse("package:" + mContext.packageName))
        mContext.startActivity(intent)
    }

    /**
     * CAMERA 권한 확인
     */
    private fun requestCameraPermission() {
        Log.w(TAG, "requestCameraPermission()")
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "#### CAMERA, Permission 1")
            activity?.let {
                if (ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.CAMERA)) {
                    Log.e(TAG, "#### CAMERA, Permission 2")
                    showPopupForPermission()
                } else {
                    Log.e(TAG, "#### CAMERA, Permission 3")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(arrayOf(Manifest.permission.CAMERA), PERMISSIONS_REQUEST_CAMERA)
                    }
                }
            }
        } else {
            Log.e(TAG, "#### CAMERA, Permission 4")
            mListener?.onSuccess(Manifest.permission.CAMERA)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        Log.w(TAG, "onRequestPermissionsResult(), requestCode : $requestCode, permissions : $permissions, grantResults : $grantResults")
        when (requestCode) {
            PERMISSIONS_REQUEST_READ_EXT_STORAGE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mListener?.onSuccess(Manifest.permission.READ_EXTERNAL_STORAGE)
                } else {
                    showPopupForPermission()
                }
                return
            }
            PERMISSIONS_REQUEST_CAMERA -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mListener?.onSuccess(Manifest.permission.CAMERA)
                } else {
                    showPopupForPermission()
                }
                return
            }
        }
    }

    companion object {

        private val TAG = PermissionCheck::class.java.simpleName
        var mListener: PermissionListener? = null
        lateinit var mContext: Context
        var mFromClass: FromClass? = FromClass.DEFAULT

        fun with(fragmentManager: FragmentManager, listener: PermissionListener, context: Context, fromClass : FromClass? = FromClass.DEFAULT): PermissionCheck {
            mListener = listener
            mContext = context
            mFromClass = fromClass
            var permissionCheckFragment = fragmentManager.findFragmentByTag(TAG) as PermissionCheck?
            if (permissionCheckFragment == null) {
                permissionCheckFragment = PermissionCheck()
                fragmentManager.beginTransaction()
                        .add(permissionCheckFragment, TAG)
                        .commit()
            }
            return permissionCheckFragment
        }
    }

    interface PermissionListener {
        fun onSuccess(permission: String) {}
        fun onError() {}
    }
}