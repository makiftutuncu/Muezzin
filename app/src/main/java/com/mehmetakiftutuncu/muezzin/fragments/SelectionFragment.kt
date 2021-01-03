package com.mehmetakiftutuncu.muezzin.fragments

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arlib.floatingsearchview.FloatingSearchView
import com.github.mehmetakiftutuncu.toolbelt.Log
import com.kennyc.view.MultiStateView
import com.mehmetakiftutuncu.muezzin.R
import com.mehmetakiftutuncu.muezzin.adapters.SearchableAdapter

abstract class SelectionFragment<I: Parcelable,
                                 L: SelectionFragment.OnSelectedListener<I>,
                                 VH: RecyclerView.ViewHolder,
                                 A: SearchableAdapter<I, VH>>(protected open val listener: L): StatefulFragment() {
    interface OnSelectedListener<I: Any> {
        fun onSelected(item: I)
    }

    private lateinit var floatingSearchView: FloatingSearchView
    private lateinit var recyclerView: RecyclerView

    private lateinit var items: List<I>
    private lateinit var adapter: A

    private val ctx: Context by lazy {
        requireActivity()
    }

    protected abstract val fragmentId: Int
    protected abstract val multiStateViewId: Int
    protected abstract val recyclerViewId: Int
    protected abstract val floatingSearchViewId: Int
    protected abstract val titleId: Int

    protected abstract fun loadFromDB(ctx: Context): List<I>

    protected abstract fun download(ctx: Context, onFail: (Throwable) -> Unit, onSuccess: (List<I>) -> Unit)

    protected abstract fun saveToDB(ctx: Context, items: List<I>): Boolean

    protected abstract fun adapt(items: List<I>): A

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        savedInstanceState?.also {
            items = it.getParcelableArrayList("items") ?: emptyList()
        }

        retainInstance = true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelableArrayList("items", ArrayList(items))

        recyclerView.layoutManager?.onSaveInstanceState()?.also {
            outState.putParcelable("recyclerView", it)
        }

        super.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)

        savedInstanceState?.also { state ->
            recyclerView.layoutManager?.apply {
                onRestoreInstanceState(state.getParcelable("recyclerView"))
            }
        }
    }

    override fun onStart() {
        super.onStart()

        if (!itemsReady()) {
            load()
        } else {
            updateUI()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(fragmentId, container, false)?.also { layout ->
            multiStateView = layout.findViewById(multiStateViewId)
            recyclerView = layout.findViewById(recyclerViewId)
            floatingSearchView = layout.findViewById(floatingSearchViewId)

            floatingSearchView.setOnQueryChangeListener { o, n -> adapter.onSearchTextChanged(o, n) }

            recyclerView.layoutManager = LinearLayoutManager(ctx)
        }

    override fun changeStateTo(newState: Int, retryAction: Int) {
        multiStateView.viewState = newState

        when (newState) {
            MultiStateView.VIEW_STATE_LOADING, MultiStateView.VIEW_STATE_ERROR, MultiStateView.VIEW_STATE_EMPTY -> {
                activity?.setTitle(R.string.applicationName)

                multiStateView.getView(newState)?.findViewById<View>(R.id.fab_retry)?.setOnClickListener {
                    retry(retryAction)
                }
            }
        }
    }

    override fun retry(action: Int) =
        when (action) {
            retryActionDownload -> {
                changeStateTo(MultiStateView.VIEW_STATE_LOADING, 0)
                downloadAndSave()
            }

            else -> {}
        }

    private fun load() {
        changeStateTo(MultiStateView.VIEW_STATE_LOADING, 0)

        items = loadFromDB(ctx)

        if (items.isEmpty()) {
            Log.debug(javaClass, "No items were found on database!")
            downloadAndSave()
        } else {
            Log.debug(javaClass, "Loaded items from database!")
            updateUI()
        }
    }

    private fun downloadAndSave() =
        download(ctx, { error ->
            Log.error(SelectionFragment::class.java, error, "Cannot download!")
            runOnUI { changeStateTo(MultiStateView.VIEW_STATE_ERROR, retryActionDownload) }
        }) { items ->
            this.items = items
            saveToDB(ctx, items).takeUnless { it }?.also {
                Log.error(SelectionFragment::class.java, "Cannot save!")
                runOnUI { changeStateTo(MultiStateView.VIEW_STATE_ERROR, retryActionDownload) }
            }

            runOnUI { updateUI() }
        }

    private fun updateUI() {
        if (!haveItems()) {
            changeStateTo(MultiStateView.VIEW_STATE_EMPTY, retryActionDownload)
            return
        }

        changeStateTo(MultiStateView.VIEW_STATE_CONTENT, 0)
        adapter = adapt(items)
        recyclerView.adapter = adapter

        activity?.setTitle(titleId)
    }

    private fun haveItems() = itemsReady() && items.isNotEmpty()

    private fun itemsReady() = this::items.isInitialized
}
