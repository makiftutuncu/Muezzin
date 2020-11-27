package com.mehmetakiftutuncu.muezzin.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mehmetakiftutuncu.muezzin.R
import net.yslibrary.licenseadapter.LicenseAdapter
import net.yslibrary.licenseadapter.Licenses
import java.util.*

class LicencesActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_licences)

        val licences = listOf(
            // Libraries that are not hosted on GitHub
            Licenses.noContent("Android SDK", "Google Inc.", "https://developer.android.com/sdk/terms.html"),
            Licenses.noContent("Android Support Libraries", "Google Inc.", "https://developer.android.com/sdk/terms.html"),
            Licenses.noContent("Mosque Vector Icon", "Freepik", "https://www.flaticon.com"),

            // Libraries that are hosted on GitHub, but do not provide license text
            Licenses.fromGitHub("Kennyc1012/MultiStateView", Licenses.LICENSE_APACHE_V2),

            // Libraries that are hosted on GitHub, and "LICENSE.md" is provided
            Licenses.fromGitHub("makiftutuncu/Toolbelt", Licenses.FILE_MD),

            // Libraries that are hosted on GitHub, and "LICENSE.txt" is provided
            Licenses.fromGitHub("arimorty/floatingsearchview"),
            Licenses.fromGitHub("stephentuso/welcome-android"),
            Licenses.fromGitHub("square/okhttp"),
            Licenses.fromGitHub("JodaOrg/joda-time"),

            // Libraries that are hosted on GitHub, and license file is provided as "LICENSE"
            Licenses.fromGitHub("Maddoc42/Android-Material-Icon-Generator", Licenses.FILE_NO_EXTENSION),
            Licenses.fromGitHub("yshrsmz/LicenseAdapter", Licenses.FILE_NO_EXTENSION)
        )

        findViewById<RecyclerView>(R.id.recyclerView_licences).run {
            layoutManager = LinearLayoutManager(this@LicencesActivity)
            adapter = LicenseAdapter(licences)

            Licenses.load(licences)
        }
    }
}