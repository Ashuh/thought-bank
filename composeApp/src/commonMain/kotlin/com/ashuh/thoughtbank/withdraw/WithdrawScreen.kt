package com.ashuh.thoughtbank.withdraw

import Thought
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ashuh.thoughtbank.repository.ThoughtsRepository
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sign

@Composable
fun WithdrawScreen(modifier: Modifier) {
    val withdrawScreenViewModel = remember { WithdrawScreenViewModel() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(withdrawScreenViewModel.currentThought == null) {
        if (withdrawScreenViewModel.currentThought == null) {
            scope.launch {
                withdrawScreenViewModel.getNewThoughts()
            }
        }
    }

    if (withdrawScreenViewModel.currentThought != null) {
        Cards(modifier, withdrawScreenViewModel)
    } else {
        if (withdrawScreenViewModel.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Empty()
        }
    }
}

@Composable
private fun Cards(modifier: Modifier, withdrawScreenViewModel: WithdrawScreenViewModel) {
    BoxWithConstraints(
        modifier = modifier
    ) {
        val screenWidth = with(LocalDensity.current) { maxWidth.toPx() }
        val threshold = screenWidth / 3
        val scope = rememberCoroutineScope()
        val animationSpec: AnimationSpec<Float> = remember { tween(500) }

        val offsetX = remember { Animatable(0f) }
        val offsetY = remember { Animatable(0f) }
        val rotation = remember { Animatable(0f) }
        val scale = remember { Animatable(0.8f) }
        var isAnimating by remember { mutableStateOf(false) }

        suspend fun swipeLeft() {
            scope.apply {
                isAnimating = true

                val a = launch {
                    offsetX.animateTo(-screenWidth, animationSpec)
                }

                val b = launch {
                    scale.animateTo(1f, animationSpec)
                }

                joinAll(a, b)

                launch {
                    offsetX.snapTo(0f)
                    offsetY.snapTo(0f)
                    rotation.snapTo(0f)
                    scale.snapTo(0.8f)
                    withdrawScreenViewModel.swipeLeft()
                    isAnimating = false
                }
            }
        }

        suspend fun swipeRight() {
            scope.apply {
                isAnimating = true

                val a = launch {
                    offsetX.animateTo(screenWidth, animationSpec)
                }

                val b = launch {
                    scale.animateTo(1f, animationSpec)
                }

                joinAll(a, b)

                launch {
                    offsetX.snapTo(0f)
                    offsetY.snapTo(0f)
                    rotation.snapTo(0f)
                    scale.snapTo(0.8f)
                    withdrawScreenViewModel.swipeRight()
                    isAnimating = false
                }
            }
        }

        suspend fun returnCenter() {
            scope.apply {
                isAnimating = true

                val a = launch {
                    offsetX.animateTo(0f, animationSpec)
                }

                val b = launch {
                    offsetY.animateTo(0f, animationSpec)
                }

                val c = launch {
                    rotation.animateTo(0f, animationSpec)
                }

                val d = launch {
                    scale.animateTo(0.8f, animationSpec)
                }

                joinAll(a, b, c, d)
                isAnimating = false
            }
        }

        Box(modifier = Modifier
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        if (offsetX.value < -threshold) {
                            scope.launch {
                                swipeLeft()
                            }
                        } else if (offsetX.value > threshold) {
                            scope.launch {
                                swipeRight()
                            }
                        } else {
                            scope.launch {
                                returnCenter()
                            }
                        }
                    },
                    onDrag = { change, dragAmount ->
                        scope.apply {
                            launch {
                                offsetX.snapTo(offsetX.value + dragAmount.x)
                                offsetY.snapTo(offsetY.value + dragAmount.y)

                                val targetRotation = normalize(
                                    0f,
                                    screenWidth,
                                    abs(offsetX.value),
                                    0f,
                                    10f
                                )

                                rotation.snapTo(targetRotation * -offsetX.value.sign)
                                scale.snapTo(
                                    normalize(
                                        0f,
                                        screenWidth / 3,
                                        abs(offsetX.value),
                                        0.8f
                                    )
                                )
                            }
                        }
                        change.consume()
                    }
                )
            }
        ) {
            withdrawScreenViewModel.nextThought?.let {
                ThoughtCard(
                    modifier = Modifier
                        .graphicsLayer(
                            scaleX = scale.value,
                            scaleY = scale.value
                        )
                        .fillMaxHeight(),
                    thought = it,
                    isAnimating = isAnimating,
                    onLike = {
                        scope.launch {
                            swipeLeft()
                        }
                    },
                    onDislike = {
                        scope.launch {
                            swipeRight()
                        }
                    }
                )
            }

            withdrawScreenViewModel.currentThought?.let {
                ThoughtCard(
                    modifier = Modifier
                        .graphicsLayer(
                            translationX = offsetX.value,
                            translationY = offsetY.value,
                            rotationZ = rotation.value,
                        )
                        .fillMaxHeight(),
                    thought = it,
                    isAnimating = isAnimating,
                    onLike = {
                        scope.launch {
                            swipeRight()
                        }
                    },
                    onDislike = {
                        scope.launch {
                            swipeLeft()
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun Empty() {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No more thoughts",
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ThoughtCard(
    modifier: Modifier = Modifier,
    thought: Thought,
    isAnimating: Boolean,
    onLike: () -> Unit,
    onDislike: () -> Unit
) {
    ElevatedCard(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = thought.content,
                modifier = Modifier.padding(16.dp),
                textAlign = TextAlign.Center,
                fontSize = 24.sp
            )

            Spacer(modifier = Modifier.weight(1f))

            Row(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = "-" + thought.author,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "score: " + thought.score.toString(),
                    fontSize = 16.sp
                )
            }

            Row {
                IconButton(
                    modifier = Modifier.padding(50.dp, 0.dp, 0.dp, 0.dp),
                    enabled = !isAnimating,
                    onClick = onDislike
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "",
                        tint = Color.Black,
                        modifier = Modifier
                            .height(50.dp)
                            .width(50.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                IconButton(
                    modifier = Modifier.padding(0.dp, 0.dp, 50.dp, 0.dp),
                    enabled = !isAnimating,
                    onClick = onLike
                ) {
                    Icon(
                        Icons.Default.FavoriteBorder,
                        contentDescription = "",
                        tint = Color.Black,
                        modifier = Modifier
                            .height(50.dp)
                            .width(50.dp)
                    )
                }
            }
        }
    }
}

private fun normalize(
    min: Float,
    max: Float,
    v: Float,
    startRange: Float = 0f,
    endRange: Float = 1f
): Float {
    require(startRange < endRange) {
        "Start range is greater than end range"
    }

    val value = v.coerceIn(min, max)

    return (value - min) / (max - min) * (endRange - startRange) + startRange
}

class WithdrawScreenViewModel {

    var currentThought: Thought? by mutableStateOf(null)
    var nextThought: Thought? by mutableStateOf(null)
    var isLoading: Boolean by mutableStateOf(false)

    suspend fun swipeLeft() {
        val thought = currentThought
        update()
        ThoughtsRepository.downvote(thought!!)
    }

    suspend fun swipeRight() {
        val thought = currentThought
        update()
        ThoughtsRepository.upvote(thought!!)
    }

    private fun update() {
        ThoughtsRepository.removeFirstNewThought()
        currentThought = ThoughtsRepository.newThoughts.firstOrNull()
        nextThought = ThoughtsRepository.newThoughts.getOrNull(1)
    }

    suspend fun getNewThoughts() {
        isLoading = true
        ThoughtsRepository.getNewThoughts()
        if (currentThought == null && nextThought == null) {
            currentThought = ThoughtsRepository.newThoughts.firstOrNull()
            nextThought = ThoughtsRepository.newThoughts.getOrNull(1)
        } else if (nextThought == null) {
            nextThought = ThoughtsRepository.newThoughts.getOrNull(1)
        }
        isLoading = false
    }
}
