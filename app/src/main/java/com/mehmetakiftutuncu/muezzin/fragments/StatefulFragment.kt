package com.mehmetakiftutuncu.muezzin.fragments

import androidx.fragment.app.Fragment
import com.kennyc.view.MultiStateView

abstract class StatefulFragment: Fragment() {
    protected lateinit var multiStateView: MultiStateView

    abstract fun changeStateTo(newState: Int, retryAction: Int)

    abstract fun retry(action: Int)

    companion object {
        const val retryActionDownload: Int = 1
    }
}