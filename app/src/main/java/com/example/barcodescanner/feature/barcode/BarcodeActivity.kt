package com.example.barcodescanner.feature.barcode

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.example.barcodescanner.R
import com.example.barcodescanner.di.*
import com.example.barcodescanner.extension.*
import com.example.barcodescanner.feature.BaseActivity
import com.example.barcodescanner.feature.barcode.save.SaveBarcodeAsImageActivity
import com.example.barcodescanner.feature.common.dialog.*
import com.example.barcodescanner.model.*
import com.example.barcodescanner.usecase.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_barcode.*
import java.text.SimpleDateFormat
import java.util.*


class BarcodeActivity : BaseActivity(), DeleteConfirmationDialogFragment.Listener,  EditBarcodeNameDialogFragment.Listener {

    companion object {
        private const val BARCODE_KEY = "BARCODE_KEY"
        private const val IS_CREATED = "IS_CREATED"

        fun start(context: Context, barcode: Barcode, isCreated: Boolean = false) {
            val intent = Intent(context, BarcodeActivity::class.java).apply {
                putExtra(BARCODE_KEY, barcode)
                putExtra(IS_CREATED, isCreated)
            }
            context.startActivity(intent)
        }
    }

    private val disposable = CompositeDisposable()
    private val dateFormatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.ENGLISH)

    private val originalBarcode by unsafeLazy {
        intent?.getSerializableExtra(BARCODE_KEY) as? Barcode ?: throw IllegalArgumentException("No barcode passed")
    }

    private val isCreated by unsafeLazy {
        intent?.getBooleanExtra(IS_CREATED, false).orFalse()
    }

    private val barcode by unsafeLazy {
        ParsedBarcode(originalBarcode)
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_barcode)

        supportEdgeToEdge()

        handleToolbarBackPressed()
        handleToolbarMenuClicked()
        handleButtonsClicked()

        showBarcode()
        showButtonText()
    }

    override fun onDeleteConfirmed() {
        deleteBarcode()
    }

    override fun onNameConfirmed(name: String) {
        updateBarcodeName(name)
    }


    override fun onDestroy() {
        super.onDestroy()
        disposable.clear()
    }


    private fun supportEdgeToEdge() {
        root_view.applySystemWindowInsets(applyTop = true, applyBottom = true)
    }


    private fun handleToolbarBackPressed() {
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun handleToolbarMenuClicked() {
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {

                R.id.item_add_to_favorites -> toggleIsFavorite()
                R.id.item_show_barcode_image -> navigateToBarcodeImageActivity()
                R.id.item_save -> saveBarcode()
                R.id.item_delete -> showDeleteBarcodeConfirmationDialog()
            }
            return@setOnMenuItemClickListener true
        }
    }

    private fun handleButtonsClicked() {
        button_edit_name.setOnClickListener { showEditBarcodeNameDialog() }


        button_save_as_image.setOnClickListener { navigateToSaveBarcodeAsImageActivity() }
    }


    private fun toggleIsFavorite() {
        val newBarcode = originalBarcode.copy(isFavorite = barcode.isFavorite.not())

        barcodeDatabase.save(newBarcode)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    barcode.isFavorite = newBarcode.isFavorite
                    showBarcodeIsFavorite(newBarcode.isFavorite)
                },
                {}
            )
            .addTo(disposable)
    }

    private fun updateBarcodeName(name: String) {
        if (name.isBlank()) {
            return
        }

        val newBarcode = originalBarcode.copy(
            id = barcode.id,
            name = name
        )

        barcodeDatabase.save(newBarcode)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    barcode.name = name
                    showBarcodeName(name)
                },
                ::showError
            )
            .addTo(disposable)
    }

    private fun saveBarcode() {
        toolbar?.menu?.findItem(R.id.item_save)?.isVisible = false
        //save data to database
        barcodeDatabase.save(originalBarcode, settings.doNotSaveDuplicates)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { id ->
                    barcode.id = id
                    button_edit_name.isVisible = true
                    toolbar?.menu?.findItem(R.id.item_delete)?.isVisible = true
                },
                { error ->
                    toolbar?.menu?.findItem(R.id.item_save)?.isVisible = true
                    showError(error)
                }
            )
            .addTo(disposable)
    }

    private fun deleteBarcode() {
        showLoading(true)

        barcodeDatabase.delete(barcode.id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { finish() },
                { error ->
                    showLoading(false)
                    showError(error)
                }
            )
            .addTo(disposable)
    }



    private fun navigateToBarcodeImageActivity() {
        BarcodeImageActivity.start(this, originalBarcode)
    }


    private fun navigateToSaveBarcodeAsImageActivity() {
        SaveBarcodeAsImageActivity.start(this, originalBarcode)
    }


    private fun showBarcode() {
        showBarcodeMenuIfNeeded()
        showBarcodeIsFavorite()
        showBarcodeImageIfNeeded()
        showBarcodeDate()
        showBarcodeFormat()
        showBarcodeName()
        showBarcodeText()
        showBarcodeCountry()
    }

    private fun showBarcodeMenuIfNeeded() {
        toolbar.inflateMenu(R.menu.menu_barcode)
        toolbar.menu.apply {
            findItem(R.id.item_add_to_favorites)?.isVisible = barcode.isInDb
            findItem(R.id.item_show_barcode_image)?.isVisible = isCreated.not()
            findItem(R.id.item_save)?.isVisible = barcode.isInDb.not()
            findItem(R.id.item_delete)?.isVisible = barcode.isInDb
        }
    }

    private fun showBarcodeIsFavorite() {
        showBarcodeIsFavorite(barcode.isFavorite)
    }

    private fun showBarcodeIsFavorite(isFavorite: Boolean) {
        val iconId = if (isFavorite) {
            R.drawable.ic_favorite_checked
        } else {
            R.drawable.ic_favorite_unchecked
        }
        toolbar.menu?.findItem(R.id.item_add_to_favorites)?.icon = ContextCompat.getDrawable(this, iconId)
    }

    private fun showBarcodeImageIfNeeded() {
        if (isCreated) {
            showBarcodeImage()
        }
    }

    private fun showBarcodeImage() {
        try {
            val bitmap = barcodeImageGenerator.generateBitmap(originalBarcode, 2000, 2000, 0, settings.barcodeContentColor, settings.barcodeBackgroundColor)
            layout_barcode_image_background.isVisible = true
            image_view_barcode.isVisible = true
            image_view_barcode.setImageBitmap(bitmap)
            image_view_barcode.setBackgroundColor(settings.barcodeBackgroundColor)
            layout_barcode_image_background.setBackgroundColor(settings.barcodeBackgroundColor)

            if (settings.isDarkTheme.not() || settings.areBarcodeColorsInversed) {
                layout_barcode_image_background.setPadding(0, 0, 0, 0)
            }
        } catch (ex: Exception) {
            Logger.log(ex)
            image_view_barcode.isVisible = false
        }
    }

    private fun showBarcodeDate() {
        text_view_date.text = dateFormatter.format(barcode.date)
    }

    private fun showBarcodeFormat() {
        val format = barcode.format.toStringId()
        toolbar.setTitle(format)
    }

    private fun showBarcodeName() {
        showBarcodeName(barcode.name)
    }

    private fun showBarcodeName(name: String?) {
        text_view_barcode_name.isVisible = name.isNullOrBlank().not()
        text_view_barcode_name.text = name.orEmpty()
    }

    private fun showBarcodeText() {
        text_view_barcode_text.text = if (isCreated) {
            barcode.text
        } else {
            barcode.formattedText
        }
    }

    private fun showBarcodeCountry() {
        val country = barcode.country ?: return
        when (country.contains('/')) {
            false -> showOneBarcodeCountry(country)
            true -> showTwoBarcodeCountries(country.split('/'))
        }
    }

    private fun showOneBarcodeCountry(country: String) {
        val fullCountryName = buildFullCountryName(country)
        showFullCountryName(fullCountryName)
    }

    private fun showTwoBarcodeCountries(countries: List<String>) {
        val firstFullCountryName = buildFullCountryName(countries[0])
        val secondFullCountryName = buildFullCountryName(countries[1])
        val fullCountryName = "$firstFullCountryName / $secondFullCountryName"
        showFullCountryName(fullCountryName)
    }

    private fun buildFullCountryName(country: String): String {
        val currentLocale = currentLocale ?: return ""
        val countryName = Locale("", country).getDisplayName(currentLocale)
        val countryEmoji = country.toCountryEmoji()
        return "$countryEmoji $countryName"
    }

    private fun showFullCountryName(fullCountryName: String) {
        text_view_country.apply {
            text = fullCountryName
            isVisible = fullCountryName.isBlank().not()
        }
    }


    private fun showButtonText() {
    }


    private fun showDeleteBarcodeConfirmationDialog() {
        val dialog = DeleteConfirmationDialogFragment.newInstance(R.string.dialog_delete_barcode_message)
        dialog.show(supportFragmentManager, "")
    }

    private fun showEditBarcodeNameDialog() {
        val dialog = EditBarcodeNameDialogFragment.newInstance(barcode.name)
        dialog.show(supportFragmentManager, "")
    }


    private fun showLoading(isLoading: Boolean) {
        progress_bar_loading.isVisible = isLoading
        scroll_view.isVisible = isLoading.not()
    }


    private fun startActivityIfExists(action: String, uri: String) {
        val intent = Intent(action, Uri.parse(uri))
        startActivityIfExists(intent)
    }

    private fun startActivityIfExists(intent: Intent) {
        intent.apply {
            flags = flags or Intent.FLAG_ACTIVITY_NEW_TASK
        }

        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            showToast(R.string.activity_barcode_no_app)
        }
    }



    private fun showToast(stringId: Int) {
        Toast.makeText(this, stringId, Toast.LENGTH_SHORT).show()
    }


}
