package com.example.barcodescanner.feature.tabs.create

import androidx.fragment.app.Fragment
import com.example.barcodescanner.extension.*
import com.example.barcodescanner.model.schema.*

abstract class BaseCreateBarcodeFragment : Fragment() {
    protected val parentActivity by unsafeLazy { requireActivity() as CreateBarcodeActivity }


    open fun getBarcodeSchema(): Schema = Other("")
    open fun showPhone(phone: String) {}
}