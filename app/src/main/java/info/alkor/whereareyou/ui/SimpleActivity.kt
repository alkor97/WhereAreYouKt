package info.alkor.whereareyou.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import info.alkor.whereareyou.api.context.AppContext
import info.alkor.whereareyou.common.Duration
import info.alkor.whereareyou.impl.settings.GOOGLE_API_KEY
import info.alkor.whereareyou.model.action.LocationAction
import info.alkor.whereareyou.model.action.Person
import info.alkor.whereareyou.model.action.PhoneNumber
import info.alkor.whereareyou.model.location.Location
import info.alkor.whereareyou.model.location.LocationFormatter
import info.alkor.whereareyou.ui.settings.SettingsActivity
import info.alkor.whereareyoukt.R
import kotlinx.android.synthetic.main.activity_simple.*
import java.util.concurrent.TimeUnit


class SimpleActivity : AppCompatActivity(), ActionFragment.OnListFragmentInteractionListener {

    private val permissionRequester by lazy { PermissionRequester(this) }

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simple)
        setSupportActionBar(toolbar)
        PreferenceManager.setDefaultValues(this, R.xml.settings, true)

        val fragmentAdapter = prepareFragmentAdapter()
        viewpager_main.adapter = fragmentAdapter
        tabs_main.setupWithViewPager(viewpager_main)

        permissionRequester.ensurePermissionsGranted(this)
    }

    @Suppress("UNUSED_PARAMETER")
    fun startLocating(view: View) {
        if (permissionRequester.canLocate()) {
            val ctx = applicationContext as AppContext
            ctx.requestMyLocation()
        } else {
            permissionRequester.ensurePermissionsGranted(this)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_settings, menu)
        return true
    }

    @Suppress("UNUSED_PARAMETER")
    fun showSettings(item: MenuItem) {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    private fun formatLink(location: Location, person: Person?) = resources.getString(R.string.location_presenter_url,
            LocationFormatter.format(location),
            if (person?.phone != PhoneNumber.OWN)
                person?.phone?.toExternalForm() ?: ""
            else "",
            person?.name
                    ?: resources.getString(R.string.your_location_is),
            GOOGLE_API_KEY)

    private fun prepareFragmentAdapter() = GenericPagerAdapter(supportFragmentManager,
            listOf(
                    //FragmentDescriptor(getString(R.string.tab_location)) { SingleRequestFragment.newInstance() },
                    FragmentDescriptor(getString(R.string.tab_actions)) { ActionFragment.newInstance() }
            )
    )

    override fun onShareLocation(action: LocationAction): Boolean {
        if (action.location != null) {
            val link = formatLink(action.location, action.person)
            val intent = Intent(Intent.ACTION_SEND)
            with(intent) {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.hera_i_am))
                putExtra(Intent.EXTRA_TEXT, link)
            }
            startActivity(Intent.createChooser(intent, getString(R.string.share_via)))
            return true
        }
        return false
    }

    override fun onShowLocation(action: LocationAction): Boolean {
        if (action.location != null) {
            val link = formatLink(action.location, action.person)
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
            startActivity(intent)
            return true
        }
        return false
    }
}

fun Duration.toString(resources: Resources) = byUnit().joinToString(" ") { (value, unit) ->
    val plural = when (unit) {
        TimeUnit.DAYS -> R.plurals.duration_days
        TimeUnit.HOURS -> R.plurals.duration_hours
        TimeUnit.MINUTES -> R.plurals.duration_minutes
        TimeUnit.SECONDS -> R.plurals.duration_seconds
        TimeUnit.MILLISECONDS -> R.plurals.duration_milliseconds
        TimeUnit.MICROSECONDS -> R.plurals.duration_microseconds
        TimeUnit.NANOSECONDS -> R.plurals.duration_nanoseconds
    }
    resources.getQuantityString(plural, value.toInt(), value)
}
