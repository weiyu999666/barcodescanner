package com.example.barcodescanner.extension

import com.example.barcodescanner.R
import com.google.zxing.BarcodeFormat

fun BarcodeFormat.toStringId(): Int {
    return when (this) {

        BarcodeFormat.EAN_8 -> R.string.barcode_format_ean_8
        BarcodeFormat.EAN_13 -> R.string.barcode_format_ean_13
        BarcodeFormat.PDF_417 -> R.string.barcode_format_pdf_417
        BarcodeFormat.QR_CODE -> R.string.barcode_format_qr_code
        else -> R.string.barcode_format_qr_code
    }
}

fun BarcodeFormat.toImageId(): Int {
    return when (this) {
        BarcodeFormat.QR_CODE -> R.drawable.ic_qr_code
        BarcodeFormat.DATA_MATRIX -> R.drawable.ic_data_matrix
        BarcodeFormat.PDF_417 -> R.drawable.ic_pdf417
        else -> R.drawable.ic_barcode
    }
}

fun BarcodeFormat.toColorId(): Int {
    return when (this) {
        BarcodeFormat.QR_CODE -> R.color.blue3
        BarcodeFormat.DATA_MATRIX, BarcodeFormat.AZTEC, BarcodeFormat.PDF_417, BarcodeFormat.MAXICODE -> R.color.orange
        else -> R.color.green
    }
}