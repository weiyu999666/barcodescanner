package com.example.barcodescanner.feature.tabs.create.qr

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.example.barcodescanner.R
import com.example.barcodescanner.extension.applySystemWindowInsets
import com.example.barcodescanner.feature.BaseActivity
import com.example.barcodescanner.feature.tabs.create.CreateBarcodeActivity
import com.example.barcodescanner.model.schema.BarcodeSchema
import com.google.zxing.BarcodeFormat
import kotlinx.android.synthetic.main.activity_create_qr_code_all.*

class CreateQrCodeAllActivity : BaseActivity() {

    companion object {

        fun start(context: Context) {
            val intent = Intent(context, CreateQrCodeAllActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_qr_code_all)
        supportEdgeToEdge()
        handleToolbarBackClicked()
        handleButtonsClicked()
    }

    private fun supportEdgeToEdge() {
        root_view.applySystemWindowInsets(applyTop = true, applyBottom = true)
    }

    private fun handleToolbarBackClicked() {
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun handleButtonsClicked() {
        button_text.setOnClickListener { CreateBarcodeActivity.start(this, BarcodeFormat.QR_CODE, BarcodeSchema.OTHER) }
        button_url.setOnClickListener { CreateBarcodeActivity.start(this, BarcodeFormat.QR_CODE, BarcodeSchema.URL) }

        button_sms.setOnClickListener { CreateBarcodeActivity.start(this, BarcodeFormat.QR_CODE, BarcodeSchema.SMS) }
        button_bookmark.setOnClickListener { CreateBarcodeActivity.start(this, BarcodeFormat.QR_CODE, BarcodeSchema.BOOKMARK) }
    }
}