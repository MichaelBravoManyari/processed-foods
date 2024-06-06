package com.mbm.alimentosprocesados.customviews

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.text.InputType
import android.text.TextUtils
import android.util.AttributeSet
import com.google.android.material.textfield.MaterialAutoCompleteTextView

class ExposedDropdownMenu : MaterialAutoCompleteTextView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun getFreezesText(): Boolean {
        return false
    }

    init {
        inputType = InputType.TYPE_NULL
    }

    override fun onSaveInstanceState(): Parcelable? {
        val parcelable = super.onSaveInstanceState()
        if (TextUtils.isEmpty(text)) {
            return parcelable
        }
        val customSavedState = CustomSavedState(parcelable)
        customSavedState.text = text.toString()
        return customSavedState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is CustomSavedState) {
            super.onRestoreInstanceState(state)
            return
        }
        setText(state.text, false)
        super.onRestoreInstanceState(state.superState)
    }

    private class CustomSavedState(superState: Parcelable?) : BaseSavedState(superState) {
        var text: String? = null

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeString(text)
        }
    }
}