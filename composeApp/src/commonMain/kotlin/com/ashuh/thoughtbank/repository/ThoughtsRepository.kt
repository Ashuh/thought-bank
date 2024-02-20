package com.ashuh.thoughtbank.repository
import Thought
import androidx.compose.runtime.mutableStateListOf
import com.ashuh.thoughtbank.api.ThoughtBankApi

object ThoughtsRepository {

    private val _newThoughts = mutableStateListOf<Thought>()
    val newThoughts: List<Thought> = _newThoughts

    private val _userUpvotedThoughts = mutableStateListOf<Thought>()
    val userUpvotedThoughts: List<Thought> = _userUpvotedThoughts

    suspend fun getNewThoughts() {
        val newThoughts = ThoughtBankApi.getThoughts(AuthenticationRepository.userId!!, 10)
        _newThoughts.addAll(newThoughts)
    }

    suspend fun refreshUserUpvotedThoughts() {
        val userUpvotedThoughts =
            ThoughtBankApi.getUserUpvotedThoughts(AuthenticationRepository.userId!!)
        _userUpvotedThoughts.clear()
        _userUpvotedThoughts.addAll(userUpvotedThoughts)
    }

    suspend fun upvote(thought: Thought) {
        ThoughtBankApi.upvote(AuthenticationRepository.userId!!, thought.id)
    }

    suspend fun downvote(thought: Thought) {
        ThoughtBankApi.downvote(AuthenticationRepository.userId!!, thought.id)
    }

    fun removeFirstNewThought() {
        _newThoughts.removeFirstOrNull()
    }

    suspend fun depositThought(thoughtContent: String) {
        ThoughtBankApi.depositThought(AuthenticationRepository.userId!!, thoughtContent)
    }
}
