package com.novikovpashka.projectkeeper.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class ProjectWithIncomings(
    @Embedded val projectEntity: ProjectEntity,
    @Relation(
        parentColumn = "dateStamp",
        entityColumn = "projectId"
    )
    var incomingsEntity: List<IncomingEntity>
)