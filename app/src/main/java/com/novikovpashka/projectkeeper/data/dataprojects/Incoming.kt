package com.novikovpashka.projectkeeper.data.dataprojects

import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class Incoming (
    var incomingDescription: String = "",
    var incomingValue: Double = 0.0,
    var incomingDate: Long = Date().time,
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
            parcel.writeString(incomingDescription)
            parcel.writeDouble(incomingValue)
            parcel.writeLong(incomingDate)
            parcel.writeLong(dateStamp)
        }
    }
}