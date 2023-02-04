package ir.kaaveh.data.repository

import ir.kaaveh.domain.model.News
import ir.kaaveh.domain.repository.NewsRepository
import ir.kaaveh.localdatasource.database.NewsDao
import ir.kaaveh.localdatasource.mapper.toNews
import ir.kaaveh.localdatasource.mapper.toLocalNewsDto
import ir.kaaveh.remotedatasource.api.NewsApi
import ir.kaaveh.remotedatasource.mapper.toNews
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NewsRepositoryImpl @Inject constructor(
    private val api: NewsApi,
    private val dao: NewsDao
) : NewsRepository {

    override fun getNews(): Flow<List<News>> =
        dao.getAllNews().map { list -> list.map { it.toNews() } }

    override suspend fun updateNews(): Boolean = try {
        api.getNews()
            .news
            .map { newsDto ->
                val news = newsDto.toNews()
                news.toLocalNewsDto().copy(
                    isFavorite = isFavoriteNews(news)
                )
            }
            .onEach {
                dao.insertNews(it)
            }
        true
    } catch (e: Exception) {
        false
    }

    override suspend fun toggleFavoriteNews(news: News) = dao.toggleFavoriteNews(news.toLocalNewsDto())

    // TODO: Make isFavoriteNews private
    override suspend fun isFavoriteNews(news: News): Boolean =
        dao.isFavoriteNews(
            title = news.title,
            source = news.source,
            isFavorite = true
        )

}