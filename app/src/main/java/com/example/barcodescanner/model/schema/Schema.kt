package com.example.barcodescanner.model.schema

enum class BarcodeSchema {
    BOOKMARK,
    EMAIL,
    SMS,
    URL,
    OTHER;
}

interface Schema {
    val schema: BarcodeSchema
    fun toFormattedText(): String
    fun toBarcodeText(): String
}