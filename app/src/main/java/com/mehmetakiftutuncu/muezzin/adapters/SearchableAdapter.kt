package com.mehmetakiftutuncu.muezzin.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.arlib.floatingsearchview.FloatingSearchView
import com.mehmetakiftutuncu.muezzin.fragments.SelectionFragment
import java.util.*

abstract class SearchableAdapter<I: Any, VH: RecyclerView.ViewHolder>(protected open val items: List<I>,
                                                                      protected open val listener: SelectionFragment.OnSelectedListener<I>): RecyclerView.Adapter<VH>(), FloatingSearchView.OnQueryChangeListener {
    private var isSearching: Boolean = false
    private var matchedItems: List<I> = emptyList()

    abstract val itemLayoutId: Int

    abstract fun hold(ctx: Context, view: View, listener: SelectionFragment.OnSelectedListener<I>): VH

    abstract fun set(holder: VH, item: I)

    abstract fun search(query: String): List<I>

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val ctx = parent.context
        val view = LayoutInflater.from(ctx).inflate(itemLayoutId, parent, false)
        return hold(ctx, view, listener)
    }

    override fun onBindViewHolder(holder: VH, position: Int) =
        set(holder, currentItems()[position])

    override fun getItemCount(): Int = currentItems().size

    override fun onSearchTextChanged(oldQuery: String?, newQuery: String?) {
        if (oldQuery ?: "" == newQuery ?: "") {
            return
        }

        val q = newQuery?.trim() ?: ""

        if (q.isEmpty()) {
            isSearching = false
            matchedItems = emptyList()
            return
        }

        isSearching = true
        matchedItems = search(q)

        notifyDataSetChanged()
    }

    private fun currentItems() = if (isSearching) matchedItems else items

    companion object {
        val tr = Locale("tr", "TR")
    }
}