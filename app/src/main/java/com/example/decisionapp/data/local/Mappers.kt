package com.example.decisionapp.data.local

import com.example.decisionapp.data.local.entity.*
import com.example.decisionapp.domain.model.*
import com.example.decisionapp.domain.model.Decision
import com.example.decisionapp.domain.model.User

fun UserEntity.toDomain() = User(id = id, username = username, passwordHash = passwordHash)

fun User.toEntity() = UserEntity(id = id, username = username, passwordHash = passwordHash)

fun DecisionEntity.toDomain() = Decision(
    id = id, userId = userId, title = title,
    description = description, createdAt = createdAt, updatedAt = updatedAt
)
fun Decision.toEntity() = DecisionEntity(
    id = id, userId = userId, title = title,
    description = description, createdAt = createdAt, updatedAt = updatedAt
)

fun CriterionEntity.toDomain() = Criterion(
    id = id, decisionId = decisionId, name = name, weight = weight, orderIndex = orderIndex
)
fun Criterion.toEntity() = CriterionEntity(
    id = id, decisionId = decisionId, name = name, weight = weight, orderIndex = orderIndex
)

fun ChoiceEntity.toDomain() = Choice(id = id, decisionId = decisionId, name = name, orderIndex = orderIndex)
fun Choice.toEntity() = ChoiceEntity(id = id, decisionId = decisionId, name = name, orderIndex = orderIndex)

fun ScoreEntity.toDomain() = Score(id = id, choiceId = choiceId, criterionId = criterionId, value = value)
fun Score.toEntity() = ScoreEntity(id = id, choiceId = choiceId, criterionId = criterionId, value = value)