package com.novikovpashka.projectkeeper.data.model

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
    var incomingsSum = 0.0

    init {
        for (incoming in incomings) {
            this.incomingsSum += incoming.value
        }
    }

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
            project.incomingsSum = parcel.readDouble()
            return project
        }

        override fun Project.write(parcel: Parcel, flags: Int) {
            parcel.writeString(name)
            parcel.writeDouble(price)
            parcel.writeString(description)
            parcel.writeList(incomings)
            parcel.writeLong(dateStamp)
            parcel.writeDouble(incomingsSum)
        }
    }
}

