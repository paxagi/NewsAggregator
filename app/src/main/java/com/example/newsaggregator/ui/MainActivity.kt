package com.example.newsaggregator.ui

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.newsaggregator.data.rss.RssFeed
import com.example.newsaggregator.data.rss.dto.ChannelDto
import com.example.newsaggregator.data.rss.dto.ImageDto
import com.example.newsaggregator.data.rss.dto.RssDto
import com.example.newsaggregator.ui.theme.NewsAggregatorTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var rssFeed: RssFeed

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NewsAggregatorTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        text = "Press me!",
                        modifier = Modifier.padding(innerPadding),
                        feed = rssFeed,
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(
    text: String,
    modifier: Modifier = Modifier,
    feed: RssFeed,
) {
    val scope = rememberCoroutineScope()
    Button(
        onClick = {
            Log.d("happy", "done")
            scope.launch {
                val r = feed.getRss()
                r.channel.items.forEach {
                    Log.d("link", it.link)
                    Log.d("guid", it.guid)
                    Log.d("dcDate", it.dcDate)
                    Log.d("pubDate", it.pubDate)
                }
            }
        }
    ) {
        Text(
            text = text,
            modifier = modifier,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    NewsAggregatorTheme {
        val previewRssFeed = object : RssFeed {
            override suspend fun getRss(query: String): RssDto {
                return RssDto(
                    version = "2.0",
                    channel = ChannelDto(
                        title = "Preview",
                        link = "",
                        description = "",
                        language = "",
                        copyright = "",
                        pubDate = "",
                        dcDate = "",
                        dcLanguage = "",
                        dcRights = "",
                        image = ImageDto("", "", ""),
                        items = emptyList()
                    )
                )
            }
        }
        
        Greeting(
            text = "Press me!",
            feed = previewRssFeed
        )
    }
}