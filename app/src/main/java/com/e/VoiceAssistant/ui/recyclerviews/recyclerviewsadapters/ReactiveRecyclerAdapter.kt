package com.e.VoiceAssistant.ui.recyclerviews.recyclerviewsadapters

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.e.VoiceAssistant.ui.recyclerviews.recyclerviewsadapters.ReactiveRecylerAdapter.ReactiveViewHolder
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.view.detaches
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject


class ReactiveRecylerAdapter<T>(observable: Observable<List<T>>,
                                private val viewHolderFactory: ReactiveViewHolderFactory<T>
                                ) : RecyclerView.Adapter<ReactiveViewHolder<T>>() {
    private val observable: Observable<List<T>>
    private var currentList: List<T>
    private val mViewClickSubject: PublishSubject<T> =
        PublishSubject.create()

    val viewClickedObservable: Observable<T>
        get() = mViewClickSubject

    override fun onCreateViewHolder(parent: ViewGroup, pViewType: Int): ReactiveViewHolder<T> {
        val viewAndHolder =
            viewHolderFactory.createViewAndHolder(parent, pViewType)
        val viewHolder = viewAndHolder.viewHolder
        viewAndHolder.view.clicks()
            .takeUntil(parent.detaches())
            .map{viewHolder.currentItem}
            .subscribe({mViewClickSubject})
        return viewHolder
    }

    override fun onBindViewHolder(holder: ReactiveViewHolder<T>, position: Int) {
        val item = currentList[position]
        holder.currentItem = item
    }

    override fun getItemCount(): Int {
        return currentList.size
    }

    abstract class ReactiveViewHolder<T>(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract var currentItem: T
    }

    interface ReactiveViewHolderFactory<T> {
        class ViewAndHolder<T>(val view: View, val viewHolder: ReactiveViewHolder<T>)

        fun createViewAndHolder(parent: ViewGroup?, pViewType: Int): ViewAndHolder<T>
    }

    init {
        currentList = emptyList()
        this.observable = observable
        this.observable.observeOn(AndroidSchedulers.mainThread()).subscribe { items: List<T> ->
            currentList = items
            notifyDataSetChanged()
        }
    }
}