package com.example.newsaggregator.domain.model

data class NewsItem(
    val title: String,
    val link: String,
    val description: String,
    val pubDate: String,
    val guid: String,
    val author: String,
    val categories: List<String>,
    val imageUrl: String?
) 