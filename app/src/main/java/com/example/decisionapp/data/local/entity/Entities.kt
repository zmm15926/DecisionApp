package com.example.decisionapp.data.local.entity

import androidx.room.*

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "username") val username: String,
    @ColumnInfo(name = "password_hash") val passwordHash: String
)

@Entity(
    tableName = "decisions",
    foreignKeys = [ForeignKey(
        entity = UserEntity::class,
        parentColumns = ["id"],
        childColumns = ["user_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("user_id")]
)
data class DecisionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "user_id") val userId: Long,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "description") val description: String = "",
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "criteria",
    foreignKeys = [ForeignKey(
        entity = DecisionEntity::class,
        parentColumns = ["id"],
        childColumns = ["decision_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("decision_id")]
)
data class CriterionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "decision_id") val decisionId: Long,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "weight") val weight: Float = 1f,
    @ColumnInfo(name = "order_index") val orderIndex: Int = 0
)

@Entity(
    tableName = "choices",
    foreignKeys = [ForeignKey(
        entity = DecisionEntity::class,
        parentColumns = ["id"],
        childColumns = ["decision_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("decision_id")]
)
data class ChoiceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "decision_id") val decisionId: Long,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "order_index") val orderIndex: Int = 0
)

@Entity(
    tableName = "scores",
    foreignKeys = [
        ForeignKey(
            entity = ChoiceEntity::class,
            parentColumns = ["id"],
            childColumns = ["choice_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CriterionEntity::class,
            parentColumns = ["id"],
            childColumns = ["criterion_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("choice_id"), Index("criterion_id")],
    primaryKeys = ["choice_id", "criterion_id"]
)
data class ScoreEntity(
    @ColumnInfo(name = "id") val id: Long = 0,
    @ColumnInfo(name = "choice_id") val choiceId: Long,
    @ColumnInfo(name = "criterion_id") val criterionId: Long,
    @ColumnInfo(name = "value") val value: Float
)