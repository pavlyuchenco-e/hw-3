package com.example.hw3.data

import com.example.hw3.data.local.ShowDao
import com.example.hw3.data.local.toDomain
import com.example.hw3.data.local.toFavouriteEntity
import com.example.hw3.data.remote.ShowsApi
import com.example.hw3.data.remote.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.example.hw3.model.Show
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject


class ShowRepository @Inject constructor(
    private val api: ShowsApi,
    private val showDao: ShowDao
) {

    suspend fun getFavourites(): List<Show> = withContext(Dispatchers.IO){
        showDao.getFavourites().map { it.toDomain()}
    }

    suspend fun toggleFavourite(show: Show) = withContext(Dispatchers.IO) {
        if (show.isFavourite) {
            showDao.deleteById(show.id)
        } else {
            showDao.upsert(show.toFavouriteEntity())
        }
    }

    suspend fun isFavourite(id: Int): Boolean = withContext(Dispatchers.IO) {
        showDao.isFavourite(id)
    }

    suspend fun searchShows(query: String): List<Show> = withContext(Dispatchers.IO) {
        val favouriteIds = showDao.getFavouritesIds().toSet()

        val response = api.searchShows(query)
        response.mapNotNull { searchResponse ->
            val showDto = searchResponse.show
            val show = showDto.toDomain()
            show.copy(isFavourite = show.id in favouriteIds)
        }
    }

    suspend fun getShowById(id: Int): Show = withContext(Dispatchers.IO) {
        val dto = api.getShowById(id)
        val show = dto.toDomain()
        val isFav = showDao.isFavourite(id)
        show.copy(isFavourite = isFav)
    }
}

