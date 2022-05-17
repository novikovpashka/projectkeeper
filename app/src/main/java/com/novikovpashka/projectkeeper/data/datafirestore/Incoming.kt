package com.novikovpashka.projectkeeper.data.datafirestore

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class Incoming (
    var incomingDescription: String = "",
    var incomingValue: Double = 0.0,
    var incomingDate: Long = Date().time,
    var dateStamp: Long = Date().time) : Parcelable