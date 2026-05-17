package com.example.decisionapp.domain.model

data class User(
    val id: Long = 0,
    val username: String,
    val passwordHash: String
)

data class Decision(
    val id: Long = 0,
    val userId: Long,
    val title: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class Criterion(
    val id: Long = 0,
    val decisionId: Long,
    val name: String,
    val weight: Float = 1f,
    val orderIndex: Int = 0
)

data class Choice(
    val id: Long = 0,
    val decisionId: Long,
    val name: String,
    val orderIndex: Int = 0
)

data class Score(
    val id: Long = 0,
    val choiceId: Long,
    val criterionId: Long,
    val value: Float // 1..10
)

data class DecisionWithDetails(
    val decision: Decision,
    val criteria: List<Criterion>,
    val choices: List<Choice>,
    val scores: List<Score>
)

data class ChoiceResult(
    val choice: Choice,
    val totalScore: Float,
    val percentage: Float,
    val scoresByCriterion: Map<Long, Float>
)

data class DecisionResult(
    val decision: Decision,
    val rankedChoices: List<ChoiceResult>,
    val criteria: List<Criterion>
)