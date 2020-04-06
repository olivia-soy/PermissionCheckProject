package net.soy.permissioncheckproject.rxImagePicker

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import net.soy.permissioncheckproject.util.Utils
import java.io.File
import java.io.IOException
import java.io.InputStream

object RxImageConverters {

    fun uriToFile(context: Context, uri: Uri, file: File): Observable<File> {
        return Observable.create(ObservableOnSubscribe<File> { emitter ->
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                file.copyInputStreamToFile(inputStream)
                emitter.onNext(file)
                emitter.onComplete()
            } catch (e: Exception) {
                Log.e(RxImageConverters::class.java.simpleName, "Error converting uri", e)
                emitter.onError(e)
            }
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
    }

    fun uriToBitmap(source: Sources, context: Context, uri: Uri): Observable<Bitmap> {
        return Observable.create(ObservableOnSubscribe<Bitmap> { emitter ->
            try {
                val bitmap = when(source){
                    Sources.GALLERY -> Utils.convertGalleryImage(uri, context)
                    Sources.CAMERA -> Utils.convertImageCapture(uri)
                    else -> Utils.convertImageCapture(uri)
                }
                bitmap?.let{emitter.onNext(bitmap)}
                emitter.onComplete()
            } catch (e: IOException) {
                Log.e(RxImageConverters::class.java.simpleName, "Error converting uri", e)
                emitter.onError(e)
            }
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
    }

    //gallery multi
    fun uriToBitmap(context: Context, uris: List<Uri>): Observable<List<Bitmap>> {
        return Observable.create(ObservableOnSubscribe<List<Bitmap>> { emitter ->
            try {
                val bitmaps: ArrayList<Bitmap> = ArrayList()
                for(i in uris.indices){
//                    Utils.convertGalleryImage(uris[i], context)?.let{bitmaps.add(it)}
                }
                emitter.onNext(bitmaps)
                emitter.onComplete()
            } catch (e: IOException) {
                Log.e(RxImageConverters::class.java.simpleName, "Error converting uri", e)
                emitter.onError(e)
            }
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
    }

    private fun File.copyInputStreamToFile(inputStream: InputStream?) {
        inputStream.use { input ->
            this.outputStream().use { fileOut ->
                input?.copyTo(fileOut)
            }
        }
    }
}