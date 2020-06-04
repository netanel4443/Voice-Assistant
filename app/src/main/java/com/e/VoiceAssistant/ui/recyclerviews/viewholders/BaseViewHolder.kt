package com.e.VoiceAssistant.ui.recyclerviews.viewholders

import android.content.Intent
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.e.VoiceAssistant.ui.recyclerviews.BindRecyclerViewHolder
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

open class BaseViewHolder(val view: View):RecyclerView.ViewHolder(view),BindRecyclerViewHolder {
    open val clickDisposable=CompositeDisposable()
    override fun bind(position: Int) {}

}