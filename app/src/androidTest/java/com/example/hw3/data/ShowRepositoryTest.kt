package com.example.hw3.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.hw3.data.local.ShowDatabase
import com.example.hw3.data.remote.ShowsApi
import com.example.hw3.model.Show
import io.mockk.mockk
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
    private val api = mockk<ShowsApi>(relaxed = true)

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, ShowDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = ShowRepository(api, database.showDao())
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun `toggleFavourite adds and removes from database`() = runBlocking {
        val show = Show(1, "Test", "En", emptyList(), null, null, null, isFavourite = false)
        repository.toggleFavourite(show)
        var favourites = repository.getFavourites()
        assertEquals(1, favourites.size)
        assertTrue(favourites[0].isFavourite)

        repository.toggleFavourite(show.copy(isFavourite = true))
        favourites = repository.getFavourites()
        assertTrue(favourites.isEmpty())
    }
}