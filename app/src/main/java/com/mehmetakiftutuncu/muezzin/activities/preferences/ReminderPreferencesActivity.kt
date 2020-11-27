package com.mehmetakiftutuncu.muezzin.activities.preferences

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mehmetakiftutuncu.muezzin.R
import com.mehmetakiftutuncu.muezzin.fragments.preferences.ReminderPreferencesFragment

class ReminderPreferencesActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_reminderpreferences)

        supportFragmentManager
                .beginTransaction()
                .replace(R.id.frameLayout_reminderPreferencesContainer, ReminderPreferencesFragment())
                .commit()
    }
}