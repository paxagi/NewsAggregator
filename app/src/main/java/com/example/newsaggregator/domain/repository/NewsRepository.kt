package com.example.newsaggregator.domain.repository

import com.example.newsaggregator.domain.model.NewsItem
import kotlinx.coroutines.flow.Flow

interface NewsRepository {
    suspend fun getNews(): Flow<List<NewsItem>>
} 