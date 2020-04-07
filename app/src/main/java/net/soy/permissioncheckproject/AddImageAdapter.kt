package net.soy.permissioncheckproject

import android.content.Context
import androidx.fragment.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import net.soy.permissioncheckproject.base.BaseAdapter
import net.soy.permissioncheckproject.base.BaseItem
import net.soy.permissioncheckproject.base.BaseViewHolder

class AddImageAdapter(private val mFragment: Fragment, context: Context): BaseAdapter<BaseItem>(context) {

    companion object {
        private val TAG = AddImageAdapter::class.java.simpleName
        const val VIEW_IMAGE_ADD: Int = 1000
        const val VIEW_IMAGE: Int = 1001
        const val VIEW_TYPE_MORE: Int = 1002
    }
    var imgUrlList : ArrayList<String>? = null

    override fun onCreateCustomholder(parent: ViewGroup, viewType: Int): BaseViewHolder<*>? {
        return when(viewType){
            VIEW_IMAGE_ADD -> {
                ImageAddViewHolder(this, LayoutInflater.from(parent.context).inflate(R.layout.item_image_add, parent, false))
            }
            VIEW_IMAGE -> {
                ImageViewHolder(this, LayoutInflater.from(parent.context).inflate(R.layout.item_image_delete, parent, false))
            }
            else -> null
        }
    }

    fun init(){
        Log.w(TAG, "init()")
        imgUrlList = ArrayList()
        clear()
        addItem(RecyclerItem(VIEW_IMAGE_ADD, ""))
        notifyDataSetChanged()
    }

    fun addImage(imgUrl: String?){
        Log.w(TAG,"addImage(), imgUrl : $imgUrl")
        imgUrlList?.add(imgUrl?:"")
        addItem(RecyclerItem(VIEW_IMAGE, imgUrl))
        notifyDataSetChanged()
    }

    fun removeImage(position: Int){
        Log.w(TAG,"removeItem(), position : $position")
        imgUrlList?.removeAt(position-1)
        removeItem(position)
        notifyDataSetChanged()
    }


    class ImageViewHolder(adapter: BaseAdapter<*>, itemView: View): BaseViewHolder<RecyclerItem>(adapter, itemView){

        private var ivImage: ImageView = itemView.findViewById(R.id.iv_image)
        private var ivImageDelete: ImageView = itemView.findViewById(R.id.iv_image_delete)
        private var mFragment = (adapter is AddImageAdapter).run{(adapter as AddImageAdapter).mFragment}

        override fun onBindView(item: RecyclerItem?, position: Int) {
            super.onBindView(item, position)
            Glide.with(mFragment).load(item?.title).into(ivImage)
            ivImageDelete.setOnClickListener {
                if(mFragment is GalleryOrCameraFragment) {
                    (mFragment as GalleryOrCameraFragment).mAddImageAdapter?.removeImage(position)
                }
            }
        }
    }

    class ImageAddViewHolder(adapter: BaseAdapter<*>, itemView: View): BaseViewHolder<RecyclerItem>(adapter, itemView){

        private var ivImage: ImageView = itemView.findViewById(R.id.iv_image)

        private var mFragment = (adapter is AddImageAdapter).run{(adapter as AddImageAdapter).mFragment}

        override fun onBindView(item: RecyclerItem?, position: Int) {
            super.onBindView(item, position)

            ivImage.setOnClickListener {
                if(mFragment is GalleryOrCameraFragment) {
                    (mFragment as GalleryOrCameraFragment).showSelectAlert()
                }
            }

        }
    }
}