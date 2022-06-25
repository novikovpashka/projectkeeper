package com.novikovpashka.projectkeeper.data.datafirestore

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class Project (
    var name: String = "",
    var price: Double = 0.0,
    var description: String = "",
    var incomings: MutableList<Incoming> = mutableListOf(),
    var dateAdded: Long = Date().time,
    val dateStamp: Long = Date().time) : Parcelable {
        constructor(
            name: String = "",
            price: Double = 0.0,
            description: String = "",
            incomings: MutableList<Incoming> = mutableListOf(),
            dateAdded: Long = Date().time) : this(name, price, description, incomings, dateAdded, Date().time)
}

