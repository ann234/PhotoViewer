package com.jhoonpark.app.photoviewer.presentation.view.adapters

import androidx.databinding.BindingAdapter
import com.google.android.material.button.MaterialButton

@BindingAdapter("app:isEnabled")
fun MaterialButton.setButtonEnabled(enabled: Boolean) {
    this.isEnabled = enabled
}