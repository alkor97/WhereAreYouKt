package info.alkor.whereareyou.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.ContactsContract
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import info.alkor.whereareyou.api.context.AppContext
import info.alkor.whereareyou.impl.settings.GOOGLE_API_KEY
import info.alkor.whereareyou.model.action.LocationAction
import info.alkor.whereareyou.model.action.Person
import info.alkor.whereareyou.model.action.PhoneNumber
import info.alkor.whereareyou.model.location.Location
import info.alkor.whereareyou.model.location.LocationFormatter
import info.alkor.whereareyou.ui.settings.SettingsActivity
import info.alkor.whereareyoukt.R
import kotlinx.android.synthetic.main.activity_simple.*


class SimpleActivity : AppCompatActivity(), ActionFragment.OnListFragmentInteractionListener {

    private val PICK_CONTACT_TO_LOCATE = 13579
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

        fab.setOnLongClickListener { locateMe() }
        fab.setOnClickListener { locatePerson() }

        permissionRequester.ensurePermissionsGranted(this)
    }

    fun locateMe(): Boolean {
        if (permissionRequester.canLocate()) {
            val ctx = applicationContext as AppContext
            ctx.requestMyLocation()
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

    fun locatePerson() {
        val intent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
        startActivityForResult(intent, PICK_CONTACT_TO_LOCATE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (requestCode == PICK_CONTACT_TO_LOCATE && resultCode == Activity.RESULT_OK && intent != null) {
            extractPerson(intent)?.let {
                confirmLocationRequest(it)
            }
        }
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
                    val phoneNumber = PhoneNumber(it.getString(phoneIdx))
                    return Person(phoneNumber, displayName)
                }
            }
        }
        return null
    }

    private fun confirmLocationRequest(person: Person) {
        with(AlertDialog.Builder(this)) {
            setTitle(R.string.location_request)
            setMessage(person.toQueryConfirmation(context.resources))
            setIcon(android.R.drawable.ic_dialog_alert)
            setPositiveButton(android.R.string.yes) { _, _ ->
                val ctx = applicationContext as AppContext
                ctx.requestLocationOf(person)
            }
            setNegativeButton(android.R.string.no, null)
            show()
        }
    }
}

fun Person.toQueryConfirmation(resources: Resources) = resources.getString(
        R.string.location_request_confirmation_query, toHumanReadable())

fun Person.toHumanReadable() = if (name != null)
    "$name (${phone.toHumanReadable()})"
else
    phone.toHumanReadable()
