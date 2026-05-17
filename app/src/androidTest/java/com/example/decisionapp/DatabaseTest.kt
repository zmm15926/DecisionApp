package com.example.decisionapp

import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.flow.first
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.decisionapp.data.local.AppDatabase
import com.example.decisionapp.data.local.dao.ChoiceDao
import com.example.decisionapp.data.local.dao.CriterionDao
import com.example.decisionapp.data.local.dao.DecisionDao
import com.example.decisionapp.data.local.dao.ScoreDao
import com.example.decisionapp.data.local.dao.UserDao
import com.example.decisionapp.data.local.entity.ChoiceEntity
import com.example.decisionapp.data.local.entity.CriterionEntity
import com.example.decisionapp.data.local.entity.DecisionEntity
import com.example.decisionapp.data.local.entity.ScoreEntity
import com.example.decisionapp.data.local.entity.UserEntity
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabaseTest {

    private lateinit var db: AppDatabase
    private lateinit var userDao: UserDao
    private lateinit var decisionDao: DecisionDao
    private lateinit var criterionDao: CriterionDao
    private lateinit var choiceDao: ChoiceDao
    private lateinit var scoreDao: ScoreDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        userDao = db.userDao()
        decisionDao = db.decisionDao()
        criterionDao = db.criterionDao()
        choiceDao = db.choiceDao()
        scoreDao = db.scoreDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insertAndGetUser() = runTest {
        val user = UserEntity(username = "testuser", passwordHash = "hash123")
        val id = userDao.insertUser(user)
        Assert.assertTrue(id > 0)

        val retrieved = userDao.getUserById(id)
        Assert.assertNotNull(retrieved)
        Assert.assertEquals("testuser", retrieved?.username)
    }

    @Test
    fun getUserByUsername() = runTest {
        userDao.insertUser(UserEntity(username = "alice", passwordHash = "hash"))

        val user = userDao.getUserByUsername("alice")
        Assert.assertNotNull(user)
        Assert.assertEquals("alice", user?.username)
    }

    @Test
    fun getUserByUsername_notFound() = runTest {
        val user = userDao.getUserByUsername("nonexistent")
        Assert.assertNull(user)
    }

    @Test
    fun insertDecisionAndGetAll() = runTest {
        val userId = userDao.insertUser(UserEntity(username = "user1", passwordHash = "h"))
        val decision = DecisionEntity(userId = userId, title = "Тест")
        decisionDao.insertDecision(decision)

        val decisions = decisionDao.getAllDecisions(userId).first()
        Assert.assertEquals(1, decisions.size)
        Assert.assertEquals("Тест", decisions.first().title)
    }

    @Test
    fun deleteDecision_cascadeDeletesCriteria() = runTest {
        val userId = userDao.insertUser(UserEntity(username = "user2", passwordHash = "h"))
        val decId = decisionDao.insertDecision(DecisionEntity(userId = userId, title = "Dec"))
        criterionDao.insertCriterion(CriterionEntity(decisionId = decId, name = "C1"))
        criterionDao.insertCriterion(CriterionEntity(decisionId = decId, name = "C2"))

        decisionDao.deleteDecision(decId)

        val criteria = criterionDao.getCriteriaByDecisionSync(decId)
        Assert.assertEquals(0, criteria.size)
    }

    @Test
    fun insertAndGetCriteria() = runTest {
        val userId = userDao.insertUser(UserEntity(username = "user3", passwordHash = "h"))
        val decId = decisionDao.insertDecision(DecisionEntity(userId = userId, title = "Dec"))
        criterionDao.insertCriterion(
            CriterionEntity(
                decisionId = decId,
                name = "Стоимость",
                weight = 3f
            )
        )
        criterionDao.insertCriterion(
            CriterionEntity(
                decisionId = decId,
                name = "Качество",
                weight = 5f
            )
        )

        val criteria = criterionDao.getCriteriaByDecisionSync(decId)
        Assert.assertEquals(2, criteria.size)
    }

    @Test
    fun upsertScore_insertsOrReplaces() = runTest {
        val userId = userDao.insertUser(UserEntity(username = "user4", passwordHash = "h"))
        val decId = decisionDao.insertDecision(DecisionEntity(userId = userId, title = "Dec"))
        val critId = criterionDao.insertCriterion(CriterionEntity(decisionId = decId, name = "C"))
        val choiceId = choiceDao.insertChoice(ChoiceEntity(decisionId = decId, name = "Choice"))

        scoreDao.upsertScore(ScoreEntity(choiceId = choiceId, criterionId = critId, value = 7f))
        var scores = scoreDao.getScoresByChoice(choiceId)
        Assert.assertEquals(1, scores.size)
        Assert.assertEquals(7f, scores.first().value)

        // Upsert again with new value
        scoreDao.upsertScore(ScoreEntity(choiceId = choiceId, criterionId = critId, value = 9f))
        scores = scoreDao.getScoresByChoice(choiceId)
        Assert.assertEquals(1, scores.size)
        Assert.assertEquals(9f, scores.first().value)
    }

    @Test
    fun getScoresByDecision() = runTest {
        val userId = userDao.insertUser(UserEntity(username = "user5", passwordHash = "h"))
        val decId = decisionDao.insertDecision(DecisionEntity(userId = userId, title = "Dec"))
        val c1 = criterionDao.insertCriterion(CriterionEntity(decisionId = decId, name = "C1"))
        val c2 = criterionDao.insertCriterion(CriterionEntity(decisionId = decId, name = "C2"))
        val ch1 = choiceDao.insertChoice(ChoiceEntity(decisionId = decId, name = "Ch1"))
        val ch2 = choiceDao.insertChoice(ChoiceEntity(decisionId = decId, name = "Ch2"))

        scoreDao.upsertScore(ScoreEntity(choiceId = ch1, criterionId = c1, value = 8f))
        scoreDao.upsertScore(ScoreEntity(choiceId = ch1, criterionId = c2, value = 6f))
        scoreDao.upsertScore(ScoreEntity(choiceId = ch2, criterionId = c1, value = 4f))

        val scores = scoreDao.getScoresByDecision(decId)
        Assert.assertEquals(3, scores.size)
    }
}