package net.soy.permissioncheckproject

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_gallery_or_camera.*
import net.soy.permissioncheckproject.permission.PermissionCheck
import net.soy.permissioncheckproject.rxImagePicker.RxImageConverters
import net.soy.permissioncheckproject.rxImagePicker.RxImagePicker
import net.soy.permissioncheckproject.rxImagePicker.Sources

class GalleryOrCameraFragment : Fragment() {

    companion object {
        private val TAG = GalleryOrCameraFragment::class.java.simpleName
    }
    private var mPermissionCheck: PermissionCheck? = null
    var mAddImageAdapter: AddImageAdapter? = null
    private var mHorizontalLayoutManager : LinearLayoutManager? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_gallery_or_camera, container, false) }

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
                            //multi select gallery 를 원할경우 Source.MULTI_GALLERY 로 변경
                            Manifest.permission.READ_EXTERNAL_STORAGE -> pickImageFromSource(Sources.GALLERY)
                        }
                    }
                }, context)
            }
        }

        context?.let{mAddImageAdapter = AddImageAdapter(this, it)}
        mHorizontalLayoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        rv_thumbnail.layoutManager = mHorizontalLayoutManager
        rv_thumbnail.adapter = mAddImageAdapter

        mAddImageAdapter?.init()
    }

    /**
     * 갤러리, 사진촬영 선택 alert
     */
    fun showSelectAlert(){
        Log.w(TAG, "showSelectAlert()")
        mPermissionCheck?.cameraOrGallerySelectAlert()
    }

    //[RxImagePicker]
    private fun pickImageFromSource(source: Sources) {
        fragmentManager?.let { fragmentManager ->
            when (source) {
                Sources.MULTI_GALLERY -> {
                    RxImagePicker.with(fragmentManager).requestMultipleImages()
                            // convert 과정이 필요할 경우 fltMap 사용
//                        .flatMap { uri ->
//                            context?.let{context -> RxImageConverters.uriToBitmap(context, uri)}
//                        }
                        .subscribe({
                            for(i in it.indices){
                                mAddImageAdapter?.addImage(it[i].toString())
                            }
                        }, {
                        })
                } else -> {
                RxImagePicker.with(fragmentManager).requestImage(source)
//                    .flatMap { uri ->
//                        context?.let { RxImageConverters.uriToBitmap(source, it, uri) }
//                    }
                    .subscribe({
                        mAddImageAdapter?.addImage(it.toString())
//                        requestInquiryImageUploads(it)
                    }, {
                    })
            }
            }
        }
    }


}