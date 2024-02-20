package com.ashuh.thoughtbank.wallet

import Thought
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ashuh.thoughtbank.repository.ThoughtsRepository
import kotlinx.coroutines.launch

@Composable
fun WalletScreen(
    modifier: Modifier = Modifier,
) {
    val walletScreenViewModel = remember { WalletScreenViewModel() }
    val savedThoughts = walletScreenViewModel.getThoughts()
    val scope = rememberCoroutineScope()

    LaunchedEffect(true) {
        scope.launch {
            walletScreenViewModel.refresh()
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (walletScreenViewModel.isLoading) {
            CircularProgressIndicator()
        } else if (savedThoughts.isEmpty()) {
            Text(
                "No thoughts yet! Like some thoughts to see them here.",
                textAlign = TextAlign.Center
            )
        } else {
            LazyColumn {
                items(savedThoughts.size) { i ->
                    ThoughtCard(thought = savedThoughts[i])
                }
            }
        }
    }
}

@Composable
private fun ThoughtCard(thought: Thought) {
    ElevatedCard(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = thought.content)
        }
    }
}

private class WalletScreenViewModel {

    var isLoading: Boolean by mutableStateOf(false)

    fun getThoughts(): List<Thought> {
        return ThoughtsRepository.userUpvotedThoughts
    }

    suspend fun refresh() {
        isLoading = true
        ThoughtsRepository.refreshUserUpvotedThoughts()
        isLoading = false
    }
}
