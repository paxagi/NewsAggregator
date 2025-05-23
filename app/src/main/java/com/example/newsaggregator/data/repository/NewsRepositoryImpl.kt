package com.example.newsaggregator.data.repository

import android.util.Log
import com.example.newsaggregator.data.rss.RssFeed
import com.example.newsaggregator.domain.model.NewsItem
import com.example.newsaggregator.domain.repository.NewsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class NewsRepositoryImpl @Inject constructor(
    private val rssFeed: RssFeed
) : NewsRepository {

    override suspend fun getNews(): Flow<List<NewsItem>> = flow {
        val rss = rssFeed.getRss()
        val news = rss.channel.items.map { item ->
            val imageUrl = item.contents.firstOrNull()?.url
            Log.d("NewsRepository", "Item: ${item.title}")
            Log.d("NewsRepository", "Contents: ${item.contents}")
            Log.d("NewsRepository", "Image URL: $imageUrl")
            
            NewsItem(
                title = item.title,
                link = item.link,
                description = item.description,
                pubDate = item.pubDate,
                guid = item.guid,
                author = item.dcCreator,
                categories = item.categories.map { it.value },
                imageUrl = imageUrl
            )
        }
        emit(news)
    }
} 