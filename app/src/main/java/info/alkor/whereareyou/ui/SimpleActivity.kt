package info.alkor.whereareyou.ui

import android.Manifest
import android.annotation.SuppressLint
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import info.alkor.whereareyou.api.context.AppContext
import info.alkor.whereareyou.common.Duration
import info.alkor.whereareyou.common.duration
import info.alkor.whereareyou.ui.settings.SettingsActivity
import info.alkor.whereareyoukt.R
import kotlinx.android.synthetic.main.activity_simple.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch
import java.util.*
import java.util.concurrent.TimeUnit


class SimpleActivity : AppCompatActivity() {

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simple)
        setSupportActionBar(toolbar)
        PreferenceManager.setDefaultValues(this, R.xml.settings, false)

        val fragmentAdapter = prepareFragmentAdapter()
        viewpager_main.adapter = fragmentAdapter
        tabs_main.setupWithViewPager(viewpager_main)

        var locating = false
        fab.setOnClickListener { view ->
            if (locating)
                return@setOnClickListener
            if (requestPermissions(Manifest.permission.ACCESS_FINE_LOCATION)) {
                locating = true
                hideFloatingActionButton(fab)
                val ctx = applicationContext as AppContext
                val startTime = Date().time

                launch {
                    ctx.locationChannel.consumeEach {
                        launch(UI) {
                            if (it.final) {
                                val d = duration(millis = Date().time - startTime).convertTo(TimeUnit.SECONDS)
                                Snackbar.make(view, getString(R.string.completed_within_duration, d.toString(resources)), Snackbar.LENGTH_LONG)
                                        .setAction("Action", null).show()
                                showFloatingActionButton(fab)
                                locating = false
                            } else {
                                Snackbar.make(view, getString(R.string.got_intermediate_result), Snackbar.LENGTH_LONG)
                                        .setAction("Action", null).show()
                            }
                        }
                    }
                }
                ctx.requestLocation()

                val queryTimeout = ctx.settings.getLocationQueryTimeout()
                Snackbar.make(view, getString(R.string.query_started_with_timeout, queryTimeout.toString(resources)), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show()
            }
        }
    }

    private fun requestPermissions(vararg permissions: String): Boolean {
        val missingPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (!missingPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(this, missingPermissions.toTypedArray(), 1)
            return false
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_simple, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item != null) {
            if (item.itemId == R.id.action_settings) {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    @Suppress("UNUSED_PARAMETER")
    fun showLocation(view: View) {
        val locationViewModel = ViewModelProviders.of(this).get(SingleLocationViewModel::class.java)
        if (locationViewModel.link != null) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(locationViewModel.link))
            startActivity(intent)
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun shareLocation(view: View) {
        val locationViewModel = ViewModelProviders.of(this).get(SingleLocationViewModel::class.java)
        if (locationViewModel.link != null) {
            val intent = Intent(Intent.ACTION_SEND)
            with(intent) {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.hera_i_am))
                putExtra(Intent.EXTRA_TEXT, locationViewModel.link)
            }
            startActivity(Intent.createChooser(intent, getString(R.string.share_via)))
        }
    }

    private fun prepareFragmentAdapter() = GenericPagerAdapter(supportFragmentManager,
            listOf(
                    FragmentDescriptor(getString(R.string.tab_location)) { SimpleActivityFragment() }
            )
    )

    private fun hideFloatingActionButton(fab: FloatingActionButton) {
        val params = fab.layoutParams as CoordinatorLayout.LayoutParams
        val behavior = params.behavior as FloatingActionButton.Behavior?

        if (behavior != null) {
            behavior.isAutoHideEnabled = false
        }

        fab.hide()
    }

    private fun showFloatingActionButton(fab: FloatingActionButton) {
        fab.show()
        val params = fab.layoutParams as CoordinatorLayout.LayoutParams
        val behavior = params.behavior as FloatingActionButton.Behavior?

        if (behavior != null) {
            behavior.isAutoHideEnabled = true
        }
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
