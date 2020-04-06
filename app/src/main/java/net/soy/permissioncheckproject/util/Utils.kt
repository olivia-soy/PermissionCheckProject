package net.soy.permissioncheckproject.util

import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import java.io.IOException

object Utils {

    private val TAG = Utils::class.java.simpleName

    /**
     * uri path 를 구하는 메소드
     * @param uri
     */
    private fun getPathFromURI(uri: Uri, context: Context) : String? {
        val path: String? = uri.path
        var columnIndex = 0
        val databaseUri: Uri
        val selection: String?
        val selectionArgs: Array<String>?
        val proj: Array<String>?
        var cursor: Cursor? = null
        try {
            if (path?.contains("/document/image:") == true) { // files selected from "Documents"
                databaseUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                selection = "_id=?"
                selectionArgs = arrayOf(DocumentsContract.getDocumentId(uri).split(":")[1])
            } else { // files selected from all other sources, especially on Samsung devices
                databaseUri = uri
                selection = null
                selectionArgs = null
            }
            proj = arrayOf(MediaStore.Images.Media.DATA)
            cursor = context.contentResolver.query(databaseUri, proj, selection, selectionArgs, null)

            if (cursor?.moveToFirst() == true) {
                columnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA)
            }
        } catch (e: Exception) {
            Log.e(TAG, e.message, e)
        }
        return cursor?.getString(columnIndex)
    }

    /**
     * 갤러리 사진을 bitmap 으로 바꾸고 회전시켜주는 메소드
     * @param imageUri 변환될 image uri
     */
    fun convertGalleryImage(imageUri: Uri, context: Context): Bitmap? {
        Log.w(TAG, "imageUri.path : ${imageUri?.path}")
        val imagePath = getPathFromURI(imageUri, context) // path 경로
        Log.w(TAG, "imagePath : $imagePath")
        val bitmap = BitmapFactory.decodeFile(imagePath) //경로를 통해 비트맵으로 전환
        val exif: ExifInterface
        try {
            exif = ExifInterface(imagePath)
        } catch (e: IOException) {
            return resizeBitmapImage(bitmap)
        }
        val exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        val exifDegree = exifOrientationToDegrees(exifOrientation)
        return resizeBitmapImage(rotate(bitmap, exifDegree.toFloat()))//이미지 뷰에 비트맵 넣기
    }

    /**
     * 사용자가 찍은 사진을 bitmap 으로 바꾸고 회전시켜주는 메소드
     * @param imageUri 변환될 image uri
     */
    fun convertImageCapture(imageUri: Uri?): Bitmap {
        Log.w(TAG, "imageUri.path : ${imageUri?.path}")
        val imagePath = imageUri?.path
        Log.w(TAG, "imagePath : $imagePath")
        val bitmap = BitmapFactory.decodeFile(imagePath)
        val exif: ExifInterface?
        try {
            // 이미지 회전
            exif = ExifInterface(imagePath)
        } catch (e: IOException) {
            return resizeBitmapImage(bitmap)
        }

        val exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        val exifDegree = exifOrientationToDegrees(exifOrientation)
        return resizeBitmapImage(rotate(bitmap, exifDegree.toFloat()))
    }

    /**
     * EXIF정보를 회전각도로 변환하는 메서드
     *
     * @param exifOrientation EXIF 회전각
     * @return 실제 각도
     */
    private fun exifOrientationToDegrees(exifOrientation: Int?): Int {
        return when(exifOrientation){
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
    }

    /**
     * 이미지를 회전시킵니다.
     *
     * @param bitmap 비트맵 이미지
     * @param degree 회전 각도
     * @return 회전된 이미지
     */
    private fun rotate(bitmap: Bitmap, degree: Float?): Bitmap {
        // Matrix 객체 생성
        val matrix = Matrix() // 회전 각도 셋팅
        degree?.let { matrix.postRotate(it) } // 이미지와 Matrix 를 셋팅해서 Bitmap 객체 생성
        return try {
            bitmap?.let { Bitmap.createBitmap(it, 0, 0, it.width, it.height, matrix, true) }
        } catch (e: OutOfMemoryError){
            bitmap
        }
    }

    private fun resizeBitmapImage(bitmap: Bitmap): Bitmap {

        val MAX_IMAGE_SIZE = 1024
        val width = bitmap?.width?:0
        val height = bitmap?.height?:0
        var newWidth = width
        var newHeight = height
        var rate: Float?

        if (width > height) {
            if (MAX_IMAGE_SIZE < width) {
                rate = MAX_IMAGE_SIZE / width.toFloat()
                newHeight = (height * rate).toInt()
                newWidth = MAX_IMAGE_SIZE
            }
        } else {
            if (MAX_IMAGE_SIZE < height) {
                rate = MAX_IMAGE_SIZE / height.toFloat()
                newWidth = (width * rate).toInt()
                newHeight = MAX_IMAGE_SIZE
            }
        }

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

}