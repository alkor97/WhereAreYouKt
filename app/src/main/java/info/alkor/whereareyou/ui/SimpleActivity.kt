package info.alkor.whereareyou.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.telephony.PhoneNumberUtils
import android.telephony.TelephonyManager
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.google.android.material.tabs.TabLayoutMediator
import info.alkor.whereareyou.BuildConfig
import info.alkor.whereareyou.R
import info.alkor.whereareyou.databinding.ActivitySimpleBinding
import info.alkor.whereareyou.impl.context.AppContext
import info.alkor.whereareyou.impl.settings.GOOGLE_API_KEY
import info.alkor.whereareyou.model.action.LocationAction
import info.alkor.whereareyou.model.action.Person
import info.alkor.whereareyou.model.action.PhoneNumber
import info.alkor.whereareyou.model.location.Location
import info.alkor.whereareyou.model.location.LocationFormatter
import info.alkor.whereareyou.ui.settings.SettingsActivity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.text.SimpleDateFormat
import java.util.*


class SimpleActivity : AppCompatActivity(), ActionFragment.OnListFragmentInteractionListener, PersonFragment.OnListFragmentInteractionListener {

    private val permissionRequester by lazy { PermissionRequester(this) }
    private val telephonyManager by lazy { getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager }
    private lateinit var binding: ActivitySimpleBinding
    private var locatePersonLauncher: ActivityResultLauncher<Intent>? = null

    @SuppressLint("MissingPermission")
    @ExperimentalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.binding = ActivitySimpleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        PreferenceManager.setDefaultValues(this, R.xml.settings, true)

        window.navigationBarColor = resources.getColor(R.color.colorPrimary, theme)
        binding.toolbar.setTitleTextColor(resources.getColor(R.color.colorOnPrimary, theme))

        val fragmentAdapter = prepareFragmentAdapter()
        binding.viewpagerMain.adapter = fragmentAdapter

        TabLayoutMediator(binding.tabsMain, binding.viewpagerMain) { tab, position ->
            tab.text = tabs[position].title
        }.attach()

        binding.fab.setOnLongClickListener { locateMe() }
        binding.fab.setOnClickListener { locatePerson() }

        permissionRequester.ensurePermissionsGranted(this)

        locatePersonLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                val intent = it.data
                if (it.resultCode == Activity.RESULT_OK && intent != null) {
                    extractPerson(intent)?.let {
                        addPersonToFavourites(it)
                        onPersonLocationRequested(it)
                    }
                }
            }
    }

    @ExperimentalCoroutinesApi
    fun locateMe(): Boolean {
        if (permissionRequester.canLocate()) {
            val ctx = applicationContext as AppContext
            ctx.handleOwnLocation()
        } else {
            permissionRequester.ensurePermissionsGranted(this)
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_settings, menu)
        return true
    }

    fun showMenu(item: MenuItem) = when (item.itemId) {
        R.id.action_settings -> showSettings()
        R.id.action_about -> showAbout()
        else -> throw UnsupportedOperationException("Unsupported menu item $item")
    }

    private fun showSettings() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    private fun showAbout() {
        val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.US)
        timeFormat.timeZone = TimeZone.getTimeZone("GMT")

        val version = resources.getString(R.string.app_version, BuildConfig.VERSION_NAME)
        val timestamp = resources.getString(R.string.app_timestamp, timeFormat.format(BuildConfig.TIMESTAMP))

        Date(BuildConfig.TIMESTAMP)
        with(AlertDialog.Builder(this)) {
            setTitle(R.string.app_name)
            setMessage("\n$version\n$timestamp")
            setIcon(R.mipmap.ic_launcher)
            show()
        }
    }

    private fun formatLink(location: Location, time: Date, person: Person?) = resources.getString(
        R.string.location_presenter_url,
        LocationFormatter.format(location, time),
        if (person?.phone != PhoneNumber.OWN)
            person?.phone?.toExternalForm() ?: ""
        else "",
        person?.name
            ?: resources.getString(R.string.your_location_is),
        GOOGLE_API_KEY
    )

    private fun prepareFragmentAdapter() = GenericPagerAdapter(supportFragmentManager, lifecycle, tabs)

    private val tabs by lazy { listOf(
        FragmentDescriptor(getString(R.string.tab_actions)) { ActionFragment.newInstance() },
        FragmentDescriptor(getString(R.string.tab_persons)) { PersonFragment.newInstance() }
    ) }

    override fun onShareLocation(action: LocationAction): Boolean {
        if (action.location != null) {
            val link = formatLink(action.location, action.time, action.person)
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
            val link = formatLink(action.location, action.time, action.person)
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
            startActivity(intent)
            return true
        }
        return false
    }


    private fun locatePerson() {
        val intent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
        locatePersonLauncher?.launch(intent)
    }

    private fun addPersonToFavourites(person: Person) {
        val viewModel = ViewModelProvider(this).get(PersonListViewModel::class.java)
        viewModel.addPerson(person)
    }

    private fun extractPerson(intent: Intent): Person? {
        val uri = intent.data
        if (uri != null) {
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                val displayNameIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val phoneIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA)
                if (it.moveToFirst()) {
                    val displayName = it.getString(displayNameIdx)
                    val phoneNumber = PhoneNumber(toPhoneNumber(it.getString(phoneIdx)))
                    return Person(phoneNumber, displayName)
                }
            }
        }
        return null
    }

    override fun onPersonLocationRequested(person: Person) {
        with(AlertDialog.Builder(this)) {
            setTitle(R.string.location_request)
            setMessage(person.toQueryConfirmation(context.resources))
            setIcon(android.R.drawable.ic_dialog_alert)
            setPositiveButton(android.R.string.ok) { _, _ ->
                val ctx = applicationContext as AppContext
                ctx.requestLocationOf(person)
            }
            setNegativeButton(android.R.string.cancel, null)
            show()
        }
    }

    private fun toPhoneNumber(text: String): String {
        val countryCode = telephonyManager.networkCountryIso.uppercase(Locale.US)
        return PhoneNumberUtils.formatNumberToE164(text, countryCode)
    }
}

fun Person.toQueryConfirmation(resources: Resources): String = resources.getString(
        R.string.location_request_confirmation_query, toHumanReadable())

fun Person.toHumanReadable() = if (name != null)
    "$name (${phone.toHumanReadable()})"
else
    phone.toHumanReadable()
