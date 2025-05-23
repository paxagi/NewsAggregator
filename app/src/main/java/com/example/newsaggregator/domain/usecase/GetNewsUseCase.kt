package com.example.newsaggregator.domain.usecase

import com.example.newsaggregator.domain.model.NewsItem
import com.example.newsaggregator.domain.repository.NewsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNewsUseCase @Inject constructor(
    private val repository: NewsRepository
) {
    suspend operator fun invoke(): Flow<List<NewsItem>> {
        return repository.getNews()
    }
} 