package info.alkor.whereareyou.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import info.alkor.whereareyou.api.context.AppContext
import info.alkor.whereareyou.common.duration
import info.alkor.whereareyou.model.location.Location
import info.alkor.whereareyou.ui.settings.SettingsActivity
import info.alkor.whereareyoukt.R
import kotlinx.android.synthetic.main.activity_simple.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch
import java.util.*


class SimpleActivity : AppCompatActivity() {

    var locationViewModel = SimpleViewModel()

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simple)
        setSupportActionBar(toolbar)
        PreferenceManager.setDefaultValues(this, R.xml.settings, false)

        val fragmentAdapter = SimplePagerAdapter(supportFragmentManager)
        viewpager_main.adapter = fragmentAdapter
        tabs_main.setupWithViewPager(viewpager_main)

        fab.setOnClickListener { view ->
            val ctx = applicationContext as AppContext
            val queryTimeout = ctx.settings.getLocationQueryTimeout()
            Log.i("location", "location query timeout is $queryTimeout")
            if (requestPermissions(Manifest.permission.ACCESS_FINE_LOCATION)) {
                val locationsChannel = Channel<Location>()
                launch {
                    locationsChannel.consumeEach {
                        Log.i("location", "intermediate: $it")
                        launch(UI) {
                            locationViewModel.update(it)
                            Snackbar.make(view, "Got intermediate location", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show()
                        }
                    }
                }
                launch {
                    val startTime = Date().time
                    val location = ctx.locationProvider.getLocation(queryTimeout, locationsChannel)
                    val duration = duration(millis = Date().time - startTime).toSeconds()

                    Log.i("location", "final: " + (location?.toString() ?: "none"))
                    launch(UI) {
                        locationViewModel.update(location)
                        Snackbar.make(view, "Completed in $duration seconds", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show()
                    }
                }
            }
            Snackbar.make(view, "Location getting started with timeout $queryTimeout", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
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
}
