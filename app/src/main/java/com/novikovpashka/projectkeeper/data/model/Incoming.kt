package com.novikovpashka.projectkeeper.data.model

import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class Incoming (
    var description: String = "",
    var value: Double = 0.0,
    var date: Long = Date().time,
) : Parcelable {
    var dateStamp: Long = Date().time

    private companion object : Parceler<Incoming> {
        override fun create(parcel: Parcel): Incoming {
            val incoming = Incoming(
                parcel.readString()!!,
                parcel.readDouble(),
                parcel.readLong()
            )
            incoming.dateStamp = parcel.readLong()
            return incoming
        }

        override fun Incoming.write(parcel: Parcel, flags: Int) {
            parcel.writeString(description)
            parcel.writeDouble(value)
            parcel.writeLong(date)
            parcel.writeLong(dateStamp)
        }
    }
}