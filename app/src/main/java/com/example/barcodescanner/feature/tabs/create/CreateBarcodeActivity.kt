package com.example.barcodescanner.feature.tabs.create

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.barcodescanner.R
import com.example.barcodescanner.di.*
import com.example.barcodescanner.extension.applySystemWindowInsets
import com.example.barcodescanner.extension.showError
import com.example.barcodescanner.extension.toStringId
import com.example.barcodescanner.extension.unsafeLazy
import com.example.barcodescanner.feature.BaseActivity
import com.example.barcodescanner.feature.barcode.BarcodeActivity
import com.example.barcodescanner.feature.tabs.create.barcode.*
import com.example.barcodescanner.feature.tabs.create.qr.*
import com.example.barcodescanner.model.*
import com.example.barcodescanner.model.schema.BarcodeSchema
import com.example.barcodescanner.model.schema.Schema
import com.example.barcodescanner.usecase.save
import com.google.zxing.BarcodeFormat
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_create_barcode.*


class CreateBarcodeActivity : BaseActivity() {

    companion object {
        private const val BARCODE_FORMAT_KEY = "BARCODE_FORMAT_KEY"
        private const val BARCODE_SCHEMA_KEY = "BARCODE_SCHEMA_KEY"
        private const val DEFAULT_TEXT_KEY = "DEFAULT_TEXT_KEY"

        private const val CHOOSE_PHONE_REQUEST_CODE = 1
        private const val CHOOSE_CONTACT_REQUEST_CODE = 2

        private const val CONTACTS_PERMISSION_REQUEST_CODE = 101
        private val CONTACTS_PERMISSIONS = arrayOf(Manifest.permission.READ_CONTACTS)

        fun start(context: Context, barcodeFormat: BarcodeFormat, barcodeSchema: BarcodeSchema? = null, defaultText: String? = null) {
            val intent = Intent(context, CreateBarcodeActivity::class.java).apply {
                putExtra(BARCODE_FORMAT_KEY, barcodeFormat.ordinal)
                putExtra(BARCODE_SCHEMA_KEY, barcodeSchema?.ordinal ?: -1)
                putExtra(DEFAULT_TEXT_KEY, defaultText)
            }
            context.startActivity(intent)
        }
    }

    private val disposable = CompositeDisposable()

    private val barcodeFormat by unsafeLazy {
        BarcodeFormat.values().getOrNull(intent?.getIntExtra(BARCODE_FORMAT_KEY, -1) ?: -1)
            ?: BarcodeFormat.QR_CODE
    }

    private val barcodeSchema by unsafeLazy {
        BarcodeSchema.values().getOrNull(intent?.getIntExtra(BARCODE_SCHEMA_KEY, -1) ?: -1)
    }

    private val defaultText by unsafeLazy {
        intent?.getStringExtra(DEFAULT_TEXT_KEY).orEmpty()
    }

    var isCreateBarcodeButtonEnabled: Boolean
        get() = false
        set(enabled) {
            val iconId = if (enabled) {
                R.drawable.ic_confirm_enabled
            } else {
                R.drawable.ic_confirm_disabled
            }

            toolbar.menu?.findItem(R.id.item_create_barcode)?.apply {
                icon = ContextCompat.getDrawable(this@CreateBarcodeActivity, iconId)
                isEnabled = enabled
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (createBarcodeImmediatelyIfNeeded()) {
            return
        }

        setContentView(R.layout.activity_create_barcode)
        supportEdgeToEdge()
        handleToolbarBackClicked()
        handleToolbarMenuItemClicked()
        showToolbarTitle()
        showToolbarMenu()
        showFragment()
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        if (resultCode != Activity.RESULT_OK) {
//            return
//        }
//
//        when (requestCode) {
//            CHOOSE_PHONE_REQUEST_CODE -> showChosenPhone(data)
//            CHOOSE_CONTACT_REQUEST_CODE -> showChosenContact(data)
//        }
//    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == CONTACTS_PERMISSION_REQUEST_CODE && permissionsHelper.areAllPermissionsGranted(grantResults)) {
            chooseContact()
        }
    }



    override fun onDestroy() {
        super.onDestroy()
        disposable.clear()
    }

    private fun supportEdgeToEdge() {
        root_view.applySystemWindowInsets(applyTop = true, applyBottom = true)
    }

    private fun createBarcodeImmediatelyIfNeeded(): Boolean {
        if (intent?.action != Intent.ACTION_SEND) {
            return false
        }

        return when (intent?.type) {
            "text/plain" -> {
                createBarcodeForPlainText()
                true
            }
            else -> false
        }
    }

    private fun createBarcodeForPlainText() {
        val text = intent?.getStringExtra(Intent.EXTRA_TEXT).orEmpty()
        val schema = barcodeParser.parseSchema(barcodeFormat, text)
        createBarcode(schema, true)
    }




    private fun handleToolbarBackClicked() {
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun handleToolbarMenuItemClicked() {
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.item_create_barcode -> createBarcode()
            }
            return@setOnMenuItemClickListener true
        }
    }

    private fun showToolbarTitle() {
        val titleId = barcodeSchema?.toStringId() ?: barcodeFormat.toStringId()
        toolbar.setTitle(titleId)
    }

    private fun showToolbarMenu() {
        val menuId = when (barcodeSchema) {
            else -> R.menu.menu_create_barcode
        }
        toolbar.inflateMenu(menuId)
    }

    private fun showFragment() {
        val fragment = when {
            barcodeFormat == BarcodeFormat.QR_CODE && barcodeSchema == BarcodeSchema.OTHER -> CreateQrCodeTextFragment.newInstance(defaultText)
            barcodeFormat == BarcodeFormat.QR_CODE && barcodeSchema == BarcodeSchema.URL -> CreateQrCodeUrlFragment()
            barcodeFormat == BarcodeFormat.QR_CODE && barcodeSchema == BarcodeSchema.BOOKMARK -> CreateQrCodeBookmarkFragment()
            barcodeFormat == BarcodeFormat.QR_CODE && barcodeSchema == BarcodeSchema.SMS -> CreateQrCodeSmsFragment()

            barcodeFormat == BarcodeFormat.PDF_417 -> CreatePdf417Fragment()
            barcodeFormat == BarcodeFormat.EAN_8 -> CreateEan8Fragment()
            barcodeFormat == BarcodeFormat.EAN_13 -> CreateEan13Fragment()

            else -> return
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .commit()
    }


    private fun requestContactsPermissions() {
        permissionsHelper.requestPermissions(this, CONTACTS_PERMISSIONS, CONTACTS_PERMISSION_REQUEST_CODE)
    }

    private fun chooseContact() {
        val intent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
        startActivityForResultIfExists(intent, CHOOSE_CONTACT_REQUEST_CODE)
    }

    private fun startActivityForResultIfExists(intent: Intent, requestCode: Int) {
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, requestCode)
        } else {
            Toast.makeText(this, R.string.activity_barcode_no_app, Toast.LENGTH_SHORT).show()
        }
    }

    private fun createBarcode() {
        val schema = getCurrentFragment().getBarcodeSchema()
        createBarcode(schema)
    }

    private fun createBarcode(schema: Schema, finish: Boolean = false) {
        val barcode = Barcode(
            text = schema.toBarcodeText(),
            formattedText = schema.toFormattedText(),
            format = barcodeFormat,
            schema = schema.schema,
            date = System.currentTimeMillis(),
            isGenerated = true
        )

        if (settings.saveCreatedBarcodesToHistory.not()) {
            navigateToBarcodeScreen(barcode, finish)
            return
        }

        barcodeDatabase.save(barcode, settings.doNotSaveDuplicates)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { id ->
                    navigateToBarcodeScreen(barcode.copy(id = id), finish)
                },
                ::showError
            )
            .addTo(disposable)
    }

    private fun getCurrentFragment(): BaseCreateBarcodeFragment {
        return supportFragmentManager.findFragmentById(R.id.container) as BaseCreateBarcodeFragment
    }

    private fun navigateToBarcodeScreen(barcode: Barcode, finish: Boolean) {
        BarcodeActivity.start(this, barcode, true)

        if (finish) {
            finish()
        }
    }
}