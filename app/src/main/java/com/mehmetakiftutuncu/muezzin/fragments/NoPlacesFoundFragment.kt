package com.mehmetakiftutuncu.muezzin.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mehmetakiftutuncu.muezzin.R
import com.mehmetakiftutuncu.muezzin.activities.PlaceSelectionActivity

class NoPlacesFoundFragment: Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_noplacesfound, container, false).also {
            (it.findViewById<View>(R.id.fab_addPlace) as FloatingActionButton).setOnClickListener {
                startActivity(Intent(activity, PlaceSelectionActivity::class.java))
            }
        }
}