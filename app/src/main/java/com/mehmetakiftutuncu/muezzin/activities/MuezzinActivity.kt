package com.mehmetakiftutuncu.muezzin.activities

import androidx.appcompat.app.AppCompatActivity

abstract class MuezzinActivity : AppCompatActivity() {
    fun setTitle(title: String) =
        supportActionBar?.apply {
            this.title = title
        }

    override fun setTitle(titleResId: Int) {
        supportActionBar?.apply {
            setTitle(titleResId)
        }
    }

    fun setSubtitle(subtitle: String) =
        supportActionBar?.apply {
            this.subtitle = subtitle
        }

    fun setSubtitle(subtitleResId: Int) {
        supportActionBar?.apply {
            setSubtitle(subtitleResId)
        }
    }
}