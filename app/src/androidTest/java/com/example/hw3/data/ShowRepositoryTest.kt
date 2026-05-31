package com.example.hw3.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.hw3.data.local.FavouriteShowEntity
import com.example.hw3.data.local.ShowDatabase
import com.example.hw3.data.remote.FakeShowsApi
import com.example.hw3.model.Show
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ShowRepositoryTest {

    private lateinit var database: ShowDatabase
    private lateinit var repository: ShowRepository
    private lateinit var fakeApi: FakeShowsApi

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, ShowDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        fakeApi = FakeShowsApi()
        repository = ShowRepository(fakeApi, database.showDao())
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun `toggleFavourite adds show then removes it`() = runBlocking {
        val show = Show(1, "Test", "En", emptyList(), null, null, null, isFavourite = false)

        repository.toggleFavourite(show)
        var favourites = repository.getFavourites()
        assertEquals(1, favourites.size)
        assertTrue(favourites[0].isFavourite)

        repository.toggleFavourite(show.copy(isFavourite = true))
        favourites = repository.getFavourites()
        assertTrue("После удаления список должен быть пуст", favourites.isEmpty())
    }

    @Test
    fun `adding same show twice via upsert does not create duplicate`() = runBlocking {
        val entity = FavouriteShowEntity(
            id = 1, name = "Test", language = "En",
            genres = emptyList(), rating = null, imageUrl = null, summary = null
        )

        database.showDao().upsert(entity)
        database.showDao().upsert(entity)

        val favourites = repository.getFavourites()
        assertEquals(
            "В базе должна быть ровно одна запись, даже после двух upsert",
            1,
            favourites.size
        )
    }
}