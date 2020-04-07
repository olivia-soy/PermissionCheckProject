package net.soy.permissioncheckproject.base

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.min

abstract class BaseAdapter<T: BaseItem>(val context: Context): RecyclerView.Adapter<BaseViewHolder<T>>() {

    var items: MutableList<T>? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount(): Int {
        return items?.size?:0
    }

    abstract fun onCreateCustomholder(parent: ViewGroup, viewType: Int): BaseViewHolder<*>?

    @Suppress("UNCHECKED_CAST")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<T> {
        return onCreateCustomholder(parent, viewType) as BaseViewHolder<T>
    }
    override fun onBindViewHolder(holder: BaseViewHolder<T>, position: Int) {
        holder.onBindView(getItem(position), position)
    }

    override fun getItemViewType(position: Int): Int {
        return getItem(position)?.viewType?:0
    }

    private fun getItem(position: Int): T? {
        return if(itemCount > position) items?.get(position) else null
    }

    fun addItem(item: T){
        addItem(Integer.MAX_VALUE, item)
    }
    fun addItem(getPosition: Int? = Int.MAX_VALUE, item: T){
        if(items == null) items = ArrayList()
        val position = min(getPosition?:0, items?.size?:0)
        val mergeItems = ArrayList(items!!)
        mergeItems.add(position, item)
        items = mergeItems
    }

    fun removeItem(position: Int){
        items?.removeAt(position)
    }

    fun clear(){
        items?.clear()
    }
}