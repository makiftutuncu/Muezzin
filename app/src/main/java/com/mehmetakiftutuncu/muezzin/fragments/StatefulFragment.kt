package com.mehmetakiftutuncu.muezzin.fragments

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import com.kennyc.view.MultiStateView

abstract class StatefulFragment: Fragment() {
    protected lateinit var multiStateView: MultiStateView

    protected val ctx: Context by lazy { requireActivity() }

    protected val handler = Handler(Looper.getMainLooper())

    abstract fun changeStateTo(newState: Int, retryAction: Int)

    abstract fun retry(action: Int)

    protected inline fun runOnUI(crossinline f: () -> Unit) {
        requireActivity().runOnUiThread { f.invoke() }
    }

    companion object {
        const val retryActionDownload: Int = 1
    }
}