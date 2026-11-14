package com.invex.app.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.invex.app.ui.components.LineUnderlinedText
import com.invex.app.ui.theme.InvexTheme
import com.invex.app.ui.theme.InvexType
import com.invex.app.ui.theme.MobileBackground
import com.invex.app.ui.theme.TableHeaderText
import kotlinx.coroutines.launch

/**
 * The three onboarding screens as a swipeable pager. [onFinished] runs when the teacher taps Skip or
 * Start. Where it goes next (Login or Register) is up to the caller, since there is no navigation yet.
 */
@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
    pages: List<OnboardingPage> = onboardingPages,
) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    // System back goes to the previous onboarding screen instead of leaving the app.
    BackHandler(enabled = pagerState.currentPage > 0) {
        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
    }

    HorizontalPager(
        state = pagerState,
        modifier = modifier.fillMaxSize().background(MobileBackground),
    ) { index ->
        OnboardingPageContent(
            page = pages[index],
            index = index,
            count = pages.size,
            onSkip = onFinished,
            onNext = {
                if (index == pages.lastIndex) onFinished()
                else scope.launch { pagerState.animateScrollToPage(index + 1) }
            },
        )
    }
}

@Composable
private fun OnboardingPageContent(
    page: OnboardingPage,
    index: Int,
    count: Int,
    onSkip: () -> Unit,
    onNext: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            // Scrolls only on very short screens; otherwise the content is centered between the paddings.
            .verticalScroll(rememberScrollState())
            .padding(start = 32.dp, end = 32.dp, top = 88.dp, bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterVertically),
    ) {
        IllustrationCircle(page)

        // The indicator is part of each page, so it moves along with the screen.
        OnboardingProgress(count = count, current = index, accent = page.accent)

        Column(
            // Same height on every page (title 2 lines + body 3 lines) so nothing jumps between pages.
            modifier = Modifier.widthIn(max = 300.dp).fillMaxWidth().heightIn(min = 176.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            LineUnderlinedText(
                text = page.title,
                style = InvexType.OnboardingTitle,
                color = Color.Black,
                underlineColor = page.accent,
                modifier = Modifier.fillMaxWidth(),
            )
            // Left aligned (the Figma frame was justified).
            Text(text = page.body, style = InvexType.OnboardingBody, color = TableHeaderText)
        }

        OnboardingButtons(isLast = index == count - 1, onSkip = onSkip, onNext = onNext)
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun OnboardingScreenPreview() {
    InvexTheme { OnboardingScreen(onFinished = {}) }
}
