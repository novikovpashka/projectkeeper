package com.novikovpashka.projectkeeper.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.novikovpashka.projectkeeper.data.model.Incoming
import java.util.*

@Entity
data class ProjectEntity(
    @PrimaryKey var dateStamp: Long = Date().time,
    var name: String = "",
    var price: Double = 0.0,
    var description: String = "",
    var incomingsSum: Double = 0.0
)