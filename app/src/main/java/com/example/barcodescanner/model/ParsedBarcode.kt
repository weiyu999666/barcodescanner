package com.example.barcodescanner.model

import com.example.barcodescanner.model.schema.*
import com.google.zxing.BarcodeFormat

class ParsedBarcode(barcode: Barcode) {
    var id = barcode.id
    var name = barcode.name
    val text = barcode.text
    val formattedText = barcode.formattedText
    val format = barcode.format
    val schema = barcode.schema
    val date = barcode.date
    var isFavorite = barcode.isFavorite
    val country = barcode.country

    var firstName: String? = null
    var lastName: String? = null
    var organization: String? = null
    var jobTitle: String? = null
    var address: String? = null

    var email: String? = null
    var emailSubject: String? = null
    var emailBody: String? = null

    var emailType: String? = null
    var secondaryEmail: String? = null
    var secondaryEmailType: String? = null
    var tertiaryEmail: String? = null
    var tertiaryEmailType: String? = null
    var note: String? = null

    var phone: String? = null
    var phoneType: String? = null
    var secondaryPhone: String? = null
    var secondaryPhoneType: String? = null
    var tertiaryPhone: String? = null
    var tertiaryPhoneType: String? = null

    var smsBody: String? = null

    var networkAuthType: String? = null
    var networkName: String? = null
    var networkPassword: String? = null
    var isHidden: Boolean? = null
    var anonymousIdentity: String? = null
    var identity: String? = null
    var eapMethod: String? = null
    var phase2Method: String? = null

    var bookmarkTitle: String? = null
    var url: String? = null
    var youtubeUrl: String? = null
    var bitcoinUri: String? = null
    var otpUrl: String? = null
    var geoUri: String? = null

    var eventUid: String? = null
    var eventStamp: String? = null
    var eventOrganizer: String? = null
    var eventDescription: String? = null
    var eventLocation: String? = null
    var eventSummary: String? = null
    var eventStartDate: Long? = null
    var eventEndDate: Long? = null

    var appMarketUrl: String? = null
    var appPackage: String? = null

    val isInDb: Boolean
        get() = id != 0L

    val isProductBarcode: Boolean
        get() = when (format) {
            BarcodeFormat.EAN_8, BarcodeFormat.EAN_13, BarcodeFormat.UPC_A, BarcodeFormat.UPC_E -> true
            else -> false
        }

    init {
        when (schema) {
            BarcodeSchema.BOOKMARK -> parseBookmark()
            BarcodeSchema.SMS -> parseSms()
            BarcodeSchema.URL -> parseUrl()
            else -> {}
        }
    }

    private fun parseBookmark() {
        val bookmark = Bookmark.parse(text) ?: return
        bookmarkTitle = bookmark.title
        url = bookmark.url
    }




    private fun parseSms() {
        val sms = Sms.parse(text) ?: return
        phone = sms.phone
        smsBody = sms.message
    }



    private fun parseUrl() {
        url = text
    }


}