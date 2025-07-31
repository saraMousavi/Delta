package com.example.delta.screens

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.delta.R
import com.example.delta.data.entity.OnboardingPage
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState

import androidx.compose.material3.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import com.example.delta.init.FileManagement

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreenWithModalSheet(
    onManualEntry: () -> Unit,
    onImportExcel: () -> Unit
) {
    val context = LocalContext.current
    val pages = listOf(
        OnboardingPage(
            imageRes = R.drawable.banner_first,
            title = context.getString(R.string.welcome),
            description = context.getString(R.string.app_features)
        ),
        OnboardingPage(
            imageRes = R.drawable.banner_second,
            title = context.getString(R.string.easy_management),
            description = context.getString(R.string.easy_management)
        ),
        OnboardingPage(
            imageRes = R.drawable.banner_third,
            title = context.getString(R.string.exact_info),
            description = context.getString(R.string.exact_info),
        )
    )

    val pagerState = rememberPagerState(initialPage = 0)
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var showSheet by remember { mutableStateOf(false) }
    val thisActivity = LocalActivity.current ?: return

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = context.getString(R.string.select_data_input_method),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text(
                    text = context.getString(R.string.template_download),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable {
                        val fileManager = FileManagement()

                        fileManager.openTemplateExcel(
                            activity = thisActivity,
                            rawResourceId = R.raw.export_delta_template,
                            fileName = "export_delta_template.xlsx",
                            authority = "${thisActivity.packageName}.fileprovider"
                        )

                    },
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(Modifier.height(4.dp))
                Button(
                    onClick = {
                        showSheet = false
                        onImportExcel()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(text = context.getString(R.string.import_from_excel), style = MaterialTheme.typography.bodyLarge)
                }

                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        showSheet = false
                        onManualEntry()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(text = context.getString(R.string.enter_data_manually), style = MaterialTheme.typography.bodyLarge)
                }

            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        HorizontalPager(
            count = pages.size,
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            OnboardingPageContent(page = pages[page])
        }

        HorizontalPagerIndicator(
            pagerState = pagerState,
            modifier = Modifier.padding(16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (pagerState.currentPage == pages.size - 1) {
            Button(
                onClick = { showSheet = true },
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp)
            ) {
                Text(text = context.getString(R.string.start), style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}


@Composable
fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(page.imageRes),
            contentDescription = null,
            modifier = Modifier.size(250.dp),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = page.title,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}
