package com.example.barcodescanner.feature.tabs.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.barcodescanner.R
import com.example.barcodescanner.extension.applySystemWindowInsets
import com.example.barcodescanner.extension.clipboardManager
import com.example.barcodescanner.extension.orZero
import com.example.barcodescanner.model.schema.BarcodeSchema
import com.google.zxing.BarcodeFormat
import kotlinx.android.synthetic.main.fragment_create_barcode.*

class CreateBarcodeFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_create_barcode, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        supportEdgeToEdge()
        handleButtonsClicked()
    }

    private fun supportEdgeToEdge() {
        app_bar_layout.applySystemWindowInsets(applyTop = true)
    }

    private fun handleButtonsClicked() {
        // QR code
        button_text.setOnClickListener { CreateBarcodeActivity.start(requireActivity(), BarcodeFormat.QR_CODE, BarcodeSchema.OTHER) }
        button_url.setOnClickListener { CreateBarcodeActivity.start(requireActivity(), BarcodeFormat.QR_CODE, BarcodeSchema.URL) }
        button_sms.setOnClickListener {CreateBarcodeActivity.start(requireActivity(), BarcodeFormat.QR_CODE, BarcodeSchema.SMS)}
        button_bookmark.setOnClickListener {CreateBarcodeActivity.start(requireActivity(), BarcodeFormat.QR_CODE, BarcodeSchema.BOOKMARK)}


        // Barcode
        button_pdf_417.setOnClickListener {CreateBarcodeActivity.start(requireActivity(), BarcodeFormat.PDF_417)}
        button_ean_8.setOnClickListener {CreateBarcodeActivity.start(requireActivity(), BarcodeFormat.EAN_8)}
        button_ean_13.setOnClickListener {CreateBarcodeActivity.start(requireActivity(), BarcodeFormat.EAN_13)}

    }


}