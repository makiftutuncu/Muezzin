package com.mehmetakiftutuncu.muezzin.models

import com.mehmetakiftutuncu.muezzin.R

enum class WidgetType(val layoutId: Int) {
    Horizontal(R.layout.widget_prayertimes_horizontal),
    Vertical(R.layout.widget_prayertimes_vertical),
    Big(R.layout.widget_prayertimes_big)
}