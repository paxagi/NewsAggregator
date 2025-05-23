package com.example.newsaggregator.ui

import android.os.Bundle
import android.text.Html
import android.text.Spanned
import android.util.Log
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.newsaggregator.domain.model.NewsItem
import com.example.newsaggregator.navigation.NavGraph
import com.example.newsaggregator.presentation.NewsViewModel
import com.example.newsaggregator.ui.theme.NewsAggregatorTheme
import dagger.hilt.android.AndroidEntryPoint
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NewsAggregatorTheme {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }
}

@Composable
fun NewsListScreen(
    onNewsClick: (String) -> Unit,
    viewModel: NewsViewModel = hiltViewModel()
) {
    val news by viewModel.news.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        NewsLoadingButton { viewModel.loadNews() }
        
        when {
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.padding(16.dp)
                )
            }
            error != null -> {
                Text(
                    text = error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }
            else -> {
                LazyColumn {
                    items(news) { newsItem ->
                        NewsItemCard(
                            newsItem = newsItem,
                            onClick = { onNewsClick(newsItem.guid) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NewsDetailScreen(
    url: String,
    onBackClick: () -> Unit
) {
    val decodedUrl = URLDecoder.decode(url, StandardCharsets.UTF_8.toString())
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                webViewClient = WebViewClient()
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                }
            }
        },
        modifier = Modifier.fillMaxSize(),
        update = { webView ->
            webView.loadUrl(decodedUrl)
                }
    )
}

@Composable
@Stable
fun NewsLoadingButton(loadNews: () -> Unit) {
    Button(
        onClick = { loadNews() },
        modifier = Modifier.padding(16.dp)
    ) {
        Text("Load News")
    }
}

@Composable
fun NewsItemCard(
    newsItem: NewsItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Изображение
            if (newsItem.imageUrl != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(newsItem.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "News image",
                    modifier = Modifier
                        .size(100.dp),
                    contentScale = ContentScale.Crop,
                    onError = { state ->
                        Log.e("NewsItemCard", "Error loading image: ${state.result.throwable}")
                        Log.e("NewsItemCard", "Image URL: ${newsItem.imageUrl}")
                    },
                    onSuccess = { state ->
                        Log.d("NewsItemCard", "Successfully loaded image: ${newsItem.imageUrl}")
                    }
                )
            } else {
                Surface(
                    modifier = Modifier.size(100.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No image",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Контент
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // Заголовок
                Text(
                    text = buildAnnotatedString {
                        val spanned = HtmlCompat.fromHtml(
                            newsItem.title,
                            HtmlCompat.FROM_HTML_MODE_COMPACT
                        )
                        append(spanned.toString())
                        spanned.getSpans(0, spanned.length, Any::class.java).forEach { span ->
                            when (span) {
                                is android.text.style.StyleSpan -> {
                                    when (span.style) {
                                        android.graphics.Typeface.BOLD -> {
                                            addStyle(
                                                SpanStyle(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                                                spanned.getSpanStart(span),
                                                spanned.getSpanEnd(span)
                                            )
                                        }
                                        android.graphics.Typeface.ITALIC -> {
                                            addStyle(
                                                SpanStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                                                spanned.getSpanStart(span),
                                                spanned.getSpanEnd(span)
                                            )
                                        }
                                    }
                                }
                                is android.text.style.UnderlineSpan -> {
                                    addStyle(
                                        SpanStyle(textDecoration = TextDecoration.Underline),
                                        spanned.getSpanStart(span),
                                        spanned.getSpanEnd(span)
                                    )
                                }
                            }
                        }
                    },
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Описание
                Text(
                    text = buildAnnotatedString {
                        val spanned = HtmlCompat.fromHtml(
                            newsItem.description,
                            HtmlCompat.FROM_HTML_MODE_COMPACT
                        )
                        append(spanned.toString())
                        spanned.getSpans(0, spanned.length, Any::class.java).forEach { span ->
                            when (span) {
                                is android.text.style.StyleSpan -> {
                                    when (span.style) {
                                        android.graphics.Typeface.BOLD -> {
                                            addStyle(
                                                SpanStyle(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                                                spanned.getSpanStart(span),
                                                spanned.getSpanEnd(span)
                                            )
                                        }
                                        android.graphics.Typeface.ITALIC -> {
                                            addStyle(
                                                SpanStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                                                spanned.getSpanStart(span),
                                                spanned.getSpanEnd(span)
                                            )
                                        }
                                    }
                                }
                                is android.text.style.UnderlineSpan -> {
                                    addStyle(
                                        SpanStyle(textDecoration = TextDecoration.Underline),
                                        spanned.getSpanStart(span),
                                        spanned.getSpanEnd(span)
                                    )
                                }
                            }
                        }
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Метаданные
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Дата публикации
                    Text(
                        text = newsItem.pubDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Автор
                    if (newsItem.author.isNotBlank()) {
                        Text(
                            text = newsItem.author,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Теги
                if (newsItem.categories.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        newsItem.categories.take(3).forEach { category ->
                            Surface(
                                modifier = Modifier.padding(vertical = 2.dp),
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = category,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}