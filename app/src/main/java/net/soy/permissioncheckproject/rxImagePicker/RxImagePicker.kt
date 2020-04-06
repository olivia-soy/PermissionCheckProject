package net.soy.permissioncheckproject.rxImagePicker

import android.annotation.TargetApi
import android.app.Activity.RESULT_OK
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.text.SimpleDateFormat
import java.util.*
import android.content.ComponentName
import android.os.Environment
import android.os.StrictMode
import androidx.core.content.FileProvider
import net.soy.permissioncheckproject.constants.Constants
import java.io.File
import java.io.IOException

/*
MIT License

Copyright (c) 2016 Sergey Glebov

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
class RxImagePicker : Fragment() {

    private lateinit var attachedSubject: PublishSubject<Boolean>
    private lateinit var publishSubject: PublishSubject<Uri>
    private lateinit var publishSubjectMultipleImages: PublishSubject<List<Uri>>
    private lateinit var canceledSubject: PublishSubject<Int>

    private var allowMultipleImages = false
    private var imageSource: Sources? = null
    private var chooserTitle: String? = null

    fun requestImage(source: Sources, chooserTitle: String?): Observable<Uri> {
        this.chooserTitle = chooserTitle
        return requestImage(source)
    }

    fun requestImage(source: Sources): Observable<Uri> {
        initSubjects()
        allowMultipleImages = false
        imageSource = source
        requestPickImage()
        return publishSubject.takeUntil(canceledSubject)
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    fun requestMultipleImages(): Observable <List<Uri>> {
        initSubjects()
        imageSource = Sources.MULTI_GALLERY
        allowMultipleImages = true
        requestPickImage()
        return publishSubjectMultipleImages.takeUntil(canceledSubject)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    private fun initSubjects(){
        publishSubject = PublishSubject.create()
        attachedSubject = PublishSubject.create()
        canceledSubject = PublishSubject.create()
        publishSubjectMultipleImages = PublishSubject.create()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (::attachedSubject.isInitialized.not() or
            ::publishSubject.isInitialized.not() or
            ::publishSubjectMultipleImages.isInitialized.not() or
            ::canceledSubject.isInitialized.not()){
            initSubjects()
        }
        attachedSubject.onNext(true)
        attachedSubject.onComplete()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            pickImage()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                SELECT_PHOTO -> handleGalleryResult(data)
                TAKE_PHOTO -> onImagePicked(cameraPictureUrl)
                CHOOSER -> if (isPhoto(data)) {
                    onImagePicked(cameraPictureUrl)
                } else {
                    handleGalleryResult(data)
                }
            }
        } else {
            canceledSubject.onNext(requestCode)
        }
    }

    private fun isPhoto(data: Intent?): Boolean {
        return data == null || data.data == null && data.clipData == null
    }

    private fun handleGalleryResult(data: Intent?) {
        if (allowMultipleImages) {
            val imageUris = ArrayList<Uri>()
            val clipData = data?.clipData
            if (clipData != null) {
                for (i in 0 until clipData.itemCount) {
                    imageUris.add(clipData.getItemAt(i).uri)
                }
            } else {
                data?.data?.let { imageUris.add(it) }
            }
            onImagesPicked(imageUris)
        } else {
            onImagePicked(data?.data)
        }
    }

    private fun requestPickImage() {
        if (!isAdded) {
            attachedSubject.subscribe { pickImage() }
        } else {
            pickImage()
        }
    }

    private fun pickImage() {

        var chooseCode = 0
        var pictureChooseIntent: Intent? = null

        when (imageSource) {
            Sources.CAMERA -> {
                pictureChooseIntent = createTakePictureIntent()
                chooseCode = TAKE_PHOTO
            }
            Sources.GALLERY -> {
                pictureChooseIntent = createPickFromGalleryIntent()
                chooseCode = SELECT_PHOTO
            }
            Sources.MULTI_GALLERY -> {
                pictureChooseIntent = createMultiPickFromGalleryIntent()
                chooseCode = SELECT_PHOTO
            }
            Sources.DOCUMENTS -> {
                pictureChooseIntent = createPickFromDocumentsIntent()
                chooseCode = SELECT_PHOTO
            }
            Sources.CHOOSER -> {
                pictureChooseIntent = createChooserIntent(chooserTitle)
                chooseCode = CHOOSER
            }
        }

        startActivityForResult(pictureChooseIntent, chooseCode)
    }

    private fun createChooserIntent(chooserTitle: String?): Intent {
        cameraPictureUrl = createImageUri()
        val cameraIntents = ArrayList<Intent>()
        val captureIntent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
        val packageManager = context?.packageManager
        val camList = packageManager?.queryIntentActivities(captureIntent, 0)
        if (camList != null) {
            for (res in camList) {
                val packageName = res.activityInfo.packageName
                val intent = Intent(captureIntent)
                intent.component = ComponentName(res.activityInfo.packageName, res.activityInfo.name)
                intent.setPackage(packageName)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraPictureUrl)
                context?.let { grantWritePermission(it, intent, cameraPictureUrl) }
                cameraIntents.add(intent)
            }
        }
        val galleryIntent = createPickFromDocumentsIntent()
        val chooserIntent = Intent.createChooser(galleryIntent, chooserTitle)
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toTypedArray())

        return chooserIntent
    }


    private fun createTakePictureIntent(): Intent? {

        val builder: StrictMode.VmPolicy.Builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
        var intent: Intent? = null
        context?.let { context ->
            createImageFile()?.let {
                val outUri = FileProvider.getUriForFile(context, Constants.AUTHORITY, it)
                intent = Intent()
                cameraPictureUrl = Uri.fromFile(createImageFile())
                intent?.action = MediaStore.ACTION_IMAGE_CAPTURE
                intent?.putExtra(MediaStore.EXTRA_OUTPUT, outUri)
                intent?.putExtra(MediaStore.EXTRA_OUTPUT, cameraPictureUrl)
                intent?.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }
        return intent
    }
    /**
     * 이미지를 임시 저장할 파일 생성
     */
    var file: File? = null

    private fun createImageFile(): File? {
        val storageDir = context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        try {
            file = File.createTempFile(timeStamp, ".jpg", storageDir)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return file
    }

    private fun createPickFromGalleryIntent(): Intent {
        var intent = Intent(Intent.ACTION_PICK)
        intent.data = MediaStore.Images.Media.INTERNAL_CONTENT_URI
        intent.type = "image/*"
        return intent
    }

    private fun createMultiPickFromGalleryIntent(): Intent {
        val pictureChooseIntent = Intent()
        pictureChooseIntent.action = Intent.ACTION_GET_CONTENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            pictureChooseIntent.type = "image/*"
            pictureChooseIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, allowMultipleImages)
        }
        return Intent.createChooser(pictureChooseIntent, "Select Picture")
    }

    private fun createPickFromDocumentsIntent(): Intent {
        val pictureChooseIntent: Intent
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            pictureChooseIntent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            pictureChooseIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, allowMultipleImages)
            pictureChooseIntent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        } else {
            pictureChooseIntent = Intent(Intent.ACTION_GET_CONTENT)
        }
        pictureChooseIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
        pictureChooseIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        pictureChooseIntent.type = "image/*"
        return pictureChooseIntent
    }

    private fun createImageUri(): Uri? {
        val contentResolver = activity?.contentResolver
        val cv = ContentValues()
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        cv.put(MediaStore.Images.Media.TITLE, timeStamp)
        return contentResolver?.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv)
    }

    private fun grantWritePermission(context: Context, intent: Intent, uri: Uri?) {
        val resInfoList = context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        for (resolveInfo in resInfoList) {
            val packageName = resolveInfo.activityInfo.packageName
            context.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    private fun onImagesPicked(uris: List<Uri>) {
        publishSubjectMultipleImages.onNext(uris)
        publishSubjectMultipleImages.onComplete()
    }

    private fun onImagePicked(uri: Uri?) {
        uri?.let { publishSubject.onNext(it) }
        publishSubject.onComplete()
    }

    companion object {

        private const val SELECT_PHOTO = 100
        private const val TAKE_PHOTO = 101
        private const val CHOOSER = 102

        private val TAG = RxImagePicker::class.java.simpleName
        private var cameraPictureUrl: Uri? = null

        fun with(fragmentManager: FragmentManager): RxImagePicker {
            var rxImagePickerFragment = fragmentManager.findFragmentByTag(TAG) as RxImagePicker?
            if (rxImagePickerFragment == null) {
                rxImagePickerFragment = RxImagePicker()
                fragmentManager.beginTransaction()
                        .add(rxImagePickerFragment, TAG)
                        .commit()
            }
            return rxImagePickerFragment
        }
    }

}