package com.novikovpashka.projectkeeper.data.dataprojects

import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class Project (
    var name: String = "",
    var price: Double = 0.0,
    var description: String = "",
    var incomings: MutableList<Incoming> = mutableListOf(),
) : Parcelable {
    var dateStamp: Long = Date().time
    private companion object : Parceler<Project> {
        override fun create(parcel: Parcel): Project {
            val project = Project(
                parcel.readString()!!,
                parcel.readDouble(),
                parcel.readString()!!,
                mutableListOf<Incoming>().apply {
                    parcel.readList(this, Incoming::class.java.classLoader)
                })
            project.dateStamp = parcel.readLong()
            return project
        }

        override fun Project.write(parcel: Parcel, flags: Int) {
            parcel.writeString(name)
            parcel.writeDouble(price)
            parcel.writeString(description)
            parcel.writeList(incomings)
            parcel.writeLong(dateStamp)
        }
    }
}


//@Parcelize
//data class Project (
//    var name: String = "",
//    var price: Double = 0.0,
//    var description: String = "",
//    var incomings: MutableList<Incoming> = mutableListOf(),
//    var dateAdded: Long = Date().time,
//    val dateStamp: Long = Date().time) : Parcelable {
//    constructor(
//        name: String = "",
//        price: Double = 0.0,
//        description: String = "",
//        incomings: MutableList<Incoming> = mutableListOf(),
//        dateAdded: Long = Date().time) : this(name, price, description, incomings, dateAdded, Date().time)
//}

