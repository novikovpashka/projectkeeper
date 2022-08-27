package com.novikovpashka.projectkeeper.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.novikovpashka.projectkeeper.data.model.Incoming
import java.util.*

@Entity
data class IncomingEntity(
    @PrimaryKey var dateStamp: Long = Date().time,
    var description: String = "",
    var value: Double = 0.0,
    var date: Long = Date().time,
    var projectId: Long = 0
)