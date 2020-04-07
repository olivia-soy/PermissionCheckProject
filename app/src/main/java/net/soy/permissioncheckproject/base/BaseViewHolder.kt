package net.soy.permissioncheckproject.base

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import org.jetbrains.annotations.NotNull

abstract class BaseViewHolder<T>(val adapter: BaseAdapter<*>?, itemView: View): RecyclerView.ViewHolder(itemView) {

    val context: Context?
        get() = adapter?.context

    open fun onBindView(@NotNull item: T?, position: Int){

    }
}