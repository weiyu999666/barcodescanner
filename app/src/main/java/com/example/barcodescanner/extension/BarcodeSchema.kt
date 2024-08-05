package com.example.barcodescanner.extension

import com.example.barcodescanner.R
import com.example.barcodescanner.model.schema.BarcodeSchema

fun BarcodeSchema.toImageId(): Int? {
    return when (this) {
        BarcodeSchema.BOOKMARK -> R.drawable.ic_bookmark
        BarcodeSchema.EMAIL -> R.drawable.ic_email
        BarcodeSchema.SMS -> R.drawable.ic_sms
        BarcodeSchema.URL -> R.drawable.ic_link
        else -> null
    }
}

fun BarcodeSchema.toStringId(): Int? {
    return when (this) {
        BarcodeSchema.BOOKMARK -> R.string.barcode_schema_bookmark
        BarcodeSchema.SMS -> R.string.barcode_schema_sms
        BarcodeSchema.URL -> R.string.barcode_schema_url
        BarcodeSchema.OTHER -> R.string.barcode_schema_other
        else -> null
    }
}