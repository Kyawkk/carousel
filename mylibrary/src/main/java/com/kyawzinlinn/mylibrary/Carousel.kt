@file:OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)

package com.kyawzinlinn.mylibrary

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

val defaultDotSize = 10.dp
val defaultDotShape = CircleShape
val defaultPageSpacing = 16.dp
val defaultItemShape = RoundedCornerShape(16.dp)
val defaultDuration = 3000L

@Composable
fun DotIndicators(
    modifier: Modifier = Modifier,
    pagerState: PagerState,
    dotShape: Shape,
    dotColor: Color,
    dotSize: Dp,
    dotCount: Int
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val unselectedDotSize = dotSize.value * 0.6

        repeat(dotCount) {
            val animatedSize by animateDpAsState(
                targetValue = if (pagerState.currentPage == it) dotSize else unselectedDotSize.dp,
                animationSpec = tween(400)
            )
            Box(
                modifier = Modifier
                    .size(animatedSize)
                    .clip(dotShape)
                    .background(dotColor)
            )
        }
    }
}

@Composable
fun CarouselSlider(
    itemCount: Int,
    itemCardShape: Shape = defaultItemShape,
    duration: Long = defaultDuration,
    dotShape: Shape = defaultDotShape,
    dotColor: Color = MaterialTheme.colorScheme.onBackground,
    dotSize: Dp = defaultDotSize,
    pageSpacing: Dp = defaultPageSpacing,
    onItemClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    carouselItem: @Composable (Int) -> Unit
) {
    val pagerState = rememberPagerState(0) { itemCount }
    val isDragged by pagerState.interactionSource.collectIsDraggedAsState()

    if (isDragged.not()) {
        with(pagerState) {
            var currentPage by remember { mutableStateOf(0) }

            LaunchedEffect(currentPage) {
                launch {
                    delay(duration)
                    val nextPage = (currentPage + 1).mod(pageCount)
                    animateScrollToPage(nextPage)
                    currentPage = nextPage
                }
            }
        }
    }

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 32.dp),
            modifier = Modifier,
            pageSpacing = pageSpacing
        ) { page ->
            CarouselItem(
                page = page,
                itemShape = itemCardShape,
                pagerState = pagerState,
                onItemClick = { onItemClick(page) },
                content = carouselItem
            )
        }

        Spacer(Modifier.height(16.dp))
        /*DotIndicators(
            dotSize = dotSize,
            dotColor = dotColor,
            dotShape = dotShape,
            pagerState = pagerState,
            dotCount = itemCount,
            modifier = Modifier
        )*/
    }
}

@Composable
private fun CarouselItem(
    page: Int,
    itemShape: Shape,
    pagerState: PagerState,
    onItemClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (Int) -> Unit
) {
    Card(
        shape = itemShape,
        onClick = onItemClick,
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                val pageOffset =
                    ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue
                val transformation = lerp(
                    start = 0.7f, stop = 1f, fraction = 1f - pageOffset.coerceIn(0f, 1f)
                )
                alpha = transformation
                scaleY = transformation
            },
    ) {
        content(page)
    }
}

