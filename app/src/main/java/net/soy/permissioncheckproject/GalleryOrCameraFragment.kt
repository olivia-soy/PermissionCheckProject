package net.soy.permissioncheckproject

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import net.soy.permissioncheckproject.permission.PermissionCheck
import net.soy.permissioncheckproject.rxImagePicker.RxImageConverters
import net.soy.permissioncheckproject.rxImagePicker.RxImagePicker
import net.soy.permissioncheckproject.rxImagePicker.Sources

class GalleryOrCameraFragment : Fragment() {

    companion object {
        private val TAG = GalleryOrCameraFragment::class.java.simpleName
    }
    private var mPermissionCheck: PermissionCheck? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_main, container, false) }

    private fun init() {

        //[permission Check]
        mPermissionCheck = fragmentManager?.let {
            context?.let { context ->
                PermissionCheck.with(it, object : PermissionCheck.PermissionListener{
                    override fun onError() {
                        Log.e(TAG, "onError()")
                    }

                    override fun onSuccess(permission: String) {
                        when(permission){
                            Manifest.permission.CAMERA -> pickImageFromSource(Sources.CAMERA)
                            Manifest.permission.READ_EXTERNAL_STORAGE -> pickImageFromSource(Sources.GALLERY)
                        }
                    }
                }, context)
            }
        }
    }

    //[RxImagePicker]
    private fun pickImageFromSource(source: Sources) {
        fragmentManager?.let { fragmentManager ->
            when (source) {
                Sources.MULTI_GALLERY -> {
                    RxImagePicker.with(fragmentManager).requestMultipleImages()
                        .flatMap { uri ->
                            context?.let{context -> RxImageConverters.uriToBitmap(context, uri)}
                        }
                        .subscribe({
                            for(i in it.indices){
//                                requestInquiryImageUploads(it[i])
                            }
                        }, {
                        })
                } else -> {
                RxImagePicker.with(fragmentManager).requestImage(source)
                    .flatMap { uri ->
                        context?.let { RxImageConverters.uriToBitmap(source, it, uri) }
                    }
                    .subscribe({
//                        requestInquiryImageUploads(it)
                    }, {
                    })
            }
            }
        }
    }


}