package com.example.newsaggregator.data.repository

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
            NewsItem(
                title = item.title,
                link = item.link,
                description = item.description,
                pubDate = item.pubDate,
                guid = item.guid,
                author = item.dcCreator,
                categories = item.categories.map { it.value }
            )
        }
        emit(news)
    }
} 