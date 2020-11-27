package com.mehmetakiftutuncu.muezzin.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.arlib.floatingsearchview.FloatingSearchView
import com.mehmetakiftutuncu.muezzin.R
import com.mehmetakiftutuncu.muezzin.fragments.SelectionFragment
import java.util.*

abstract class SearchableAdapter<I: Any, VH: RecyclerView.ViewHolder>(protected open val items: List<I>,
                                                                      protected open val listener: SelectionFragment.OnSelectedListener<I>): RecyclerView.Adapter<VH>(), FloatingSearchView.OnQueryChangeListener {
    private var currentItems: List<I> = emptyList()

    abstract fun hold(ctx: Context, view: View, listener: SelectionFragment.OnSelectedListener<I>): VH

    abstract fun set(holder: VH, item: I)

    abstract fun search(query: String): List<I>

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        parent.context.let { ctx ->
            LayoutInflater.from(ctx).inflate(R.layout.item_county, parent, false).let {
                hold(ctx, it, listener)
            }
        }

    override fun onBindViewHolder(holder: VH, position: Int) =
        set(holder, currentItems[position])

    override fun getItemCount(): Int = currentItems.size

    override fun onSearchTextChanged(oldQuery: String?, newQuery: String?) {
        if (oldQuery ?: "" == newQuery ?: "") {
            return
        }

        val q = newQuery?.trim() ?: ""

        if (q.isEmpty()) {
            currentItems = items
            return
        }

        currentItems = search(q)

        notifyDataSetChanged()
    }

    companion object {
        val tr = Locale("tr", "TR")
    }
}