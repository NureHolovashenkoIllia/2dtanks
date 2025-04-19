package ua.nure.holovashenko.vmptf_lb3_2dtanks.ui.screens.multiplayer.game

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import kotlin.random.Random

class GameRepository(private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    suspend fun fetchRoom(roomId: String): DocumentSnapshot? {
        return firestore.collection("gameRooms").document(roomId).get().await()
    }

    suspend fun updatePlayerPosition(roomId: String, playerId: String, x: Int, y: Int) {
        firestore.collection("gameRooms")
            .document(roomId)
            .update("positions.$playerId", mapOf("x" to x, "y" to y))
            .await()
    }

    suspend fun updateMap(roomId: String, gridSize: Int = 10, obstacleChance: Double = 0.2) {
        val roomRef = firestore.collection("gameRooms").document(roomId)
        val snapshot = roomRef.get().await()
        if (snapshot.contains("map")) return

        val newMap = mutableMapOf<String, Boolean>()
        for (y in 0 until gridSize) {
            for (x in 0 until gridSize) {
                val key = "$x,$y"
                newMap[key] = Random.nextDouble() < obstacleChance
            }
        }
        roomRef.update("map", newMap).await()
    }

    suspend fun assignInitialPosition(roomId: String, playerId: String, gridSize: Int = 10) {
        val roomRef = firestore.collection("gameRooms").document(roomId)
        val snapshot = roomRef.get().await()
        val positionsMap = snapshot.get("positions") as? Map<*, *> ?: emptyMap<String, Any>()
        if (!positionsMap.containsKey(playerId)) {
            val x = Random.nextInt(0, gridSize)
            val y = Random.nextInt(0, gridSize)
            roomRef.update("positions.$playerId", mapOf("x" to x, "y" to y)).await()
        }
    }

    suspend fun updateBullets(roomId: String, bullets: List<Map<String, Any?>>) {
        firestore.collection("gameRooms")
            .document(roomId)
            .update("bullets", bullets)
            .await()
    }

    suspend fun updateAliveStatus(roomId: String, updated: Map<String, Boolean>) {
        firestore.collection("gameRooms")
            .document(roomId)
            .update("aliveStatus", updated)
            .await()
    }

    suspend fun resetGameState(roomId: String) {
        val roomRef = firestore.collection("gameRooms").document(roomId)

        // Очищаємо лише дані, пов’язані з грою, а не саму кімнату
        val updates = mapOf(
            "positions" to emptyMap<String, Any>(),
            "aliveStatus" to emptyMap<String, Any>(),
            "bullets" to emptyList<Any>(),
            "map" to FieldValue.delete(),
            "gameStarted" to false,
            "gameDuration" to 180
        )

        roomRef.update(updates).await()
    }

    suspend fun saveGameHistory(
        gameId: String,
        data: Map<String, Any>
    ) {
        firestore.collection("gameHistory")
            .document(gameId)
            .set(data)
            .await()
    }

    suspend fun generateNewGameId(): Long? {
        val metadataRef = FirebaseFirestore.getInstance()
            .collection("gameMetadata")
            .document("counters")

        return try {
            FirebaseFirestore.getInstance().runTransaction { transaction ->
                val snapshot = transaction.get(metadataRef)
                val lastId = snapshot.getLong("lastGameId") ?: 0
                val newId = lastId + 1
                transaction.update(metadataRef, "lastGameId", newId)
                newId
            }.await()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun updateMatchStats(playerId: String, won: Boolean, kills: Int) {
        val statsRef = firestore.collection("users").document(playerId)
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(statsRef)
            val prevWins = snapshot.getLong("wins") ?: 0
            val prevKills = snapshot.getLong("kills") ?: 0
            val prevMatches = snapshot.getLong("matches") ?: 0

            transaction.update(statsRef, mapOf(
                "wins" to (prevWins + if (won) 1 else 0),
                "kills" to (prevKills + kills),
                "matches" to (prevMatches + 1)
            ))
        }.await()
    }
}
