package com.novikovpashka.projectkeeper.extensions

import com.novikovpashka.projectkeeper.data.model.*

fun Project.toProjectWithIncomingsEntity(): ProjectWithIncomings {
    val projectEntity = ProjectEntity(
        dateStamp = dateStamp,
        name = name,
        price = price,
        description = description,
        incomingsSum = incomingsSum
    )
    val incomingsEntity = mutableListOf<IncomingEntity>()
    this.incomings.forEach {
        incomingsEntity.add(it.toEntity(this.dateStamp))
    }
    return ProjectWithIncomings(
        projectEntity,
        incomingsEntity
    )
}

fun ProjectWithIncomings.toProject(): Project {
    val incomings = mutableListOf<Incoming>()
    this.incomingsEntity.forEach {
        incomings.add(it.toIncoming())
    }
    val projectEntity = this.projectEntity
    val project = Project(
        name = projectEntity.name,
        price = projectEntity.price,
        description = projectEntity.description,
        incomings = incomings
    )
    project.dateStamp = projectEntity.dateStamp
    return project
}

fun Incoming.toEntity(projectId: Long): IncomingEntity {
    return IncomingEntity(
        dateStamp = dateStamp,
        description = description,
        value = value,
        date = date,
        projectId = projectId
    )
}

fun IncomingEntity.toIncoming(): Incoming {
    val incoming = Incoming(
        description = description,
        value = value,
        date = date
    )
    incoming.dateStamp = this.dateStamp
    return incoming
}