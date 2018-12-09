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
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import info.alkor.whereareyou.api.context.AppContext
import info.alkor.whereareyou.api.persistence.ExecutionCompleted
import info.alkor.whereareyou.api.persistence.IntermediateLocation
import info.alkor.whereareyou.common.Duration
import info.alkor.whereareyou.ui.settings.SettingsActivity
import info.alkor.whereareyoukt.R
import kotlinx.android.synthetic.main.activity_simple.*
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch
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

        fab.setOnClickListener { view ->
            if (requestPermissions(Manifest.permission.ACCESS_FINE_LOCATION)) {
                hideFloatingActionButton(fab)
                val ctx = applicationContext as AppContext

                val listener = Job()
                launch(parent = listener) {
                    ctx.locationRequestPersistence.events.consumeEach { event ->
                        when (event) {
                            is ExecutionCompleted -> launch(UI) {
                                launch(UI) {
                                    val duration = event.elapsed.toString(resources)
                                    Snackbar.make(view, getString(R.string.completed_within_duration, duration), Snackbar.LENGTH_LONG)
                                            .setAction("Action", null).show()
                                    showFloatingActionButton(fab)
                                }
                                listener.cancel()
                            }
                            is IntermediateLocation -> launch(UI) {
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
        for (permission in missingPermissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                showExplanation(permission)
            } else {
                doRequestPermissions(permission)
            }
            // one permission at a time
            return false
        }
        return true
    }

    private fun doRequestPermissions(vararg permissions: String) {
        ActivityCompat.requestPermissions(this, permissions, 1)
    }

    private fun showExplanation(vararg permissions: String) {
        permissions.map { permission ->
            when (permission) {
                Manifest.permission.ACCESS_FINE_LOCATION -> Pair(permission, R.string.location_permission_needed)
                else -> throw IllegalArgumentException(permission)
            }
        }.forEach { (permission, messageId) ->
            with(AlertDialog.Builder(this)) {
                setTitle(R.string.permission_needed_title)
                setMessage(messageId)
                setPositiveButton(android.R.string.ok) { _, _ ->
                    doRequestPermissions(permission)
                }
                create()
                show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_simple, menu)
        return true
    }

    @Suppress("UNUSED_PARAMETER")
    fun showSettings(item: MenuItem) {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    @Suppress("UNUSED_PARAMETER")
    fun showLocation(item: MenuItem) {
        withLocationLink { link ->
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
            startActivity(intent)
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun shareLocation(item: MenuItem) {
        withLocationLink { link ->
            val intent = Intent(Intent.ACTION_SEND)
            with(intent) {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.hera_i_am))
                putExtra(Intent.EXTRA_TEXT, link)
            }
            startActivity(Intent.createChooser(intent, getString(R.string.share_via)))
        }
    }

    private fun withLocationLink(handle: (link: String) -> Unit) {
        val locationViewModel = ViewModelProviders.of(this).get(SingleLocationViewModel::class.java)
        locationViewModel.link?.let {
            handle(it)
        }
    }

    fun showPopup(view: View) {
        val popup = PopupMenu(this, view)
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.menu_location_popup, popup.menu)
        popup.show()
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
