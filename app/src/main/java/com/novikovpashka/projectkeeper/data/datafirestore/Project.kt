package com.novikovpashka.projectkeeper.data.datafirestore

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class Project (
    var name: String = "",
    var price: Double = 0.0,
    var incomings: MutableList<Double> = mutableListOf()) : Parcelable {
    val dateAdded: Long = Date().time
}
