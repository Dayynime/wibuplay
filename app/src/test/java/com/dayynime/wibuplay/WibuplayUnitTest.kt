package com.dayynime.wibuplay

import com.dayynime.wibuplay.data.api.JsonHelper
import com.dayynime.wibuplay.data.model.AnimeItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class WibuplayUnitTest {

    @Test
    fun testCursorSanitization() {
        assertEquals("77", JsonHelper.sanitizeCursorValue(77.0))
        assertEquals("77", JsonHelper.sanitizeCursorValue(77.0f))
        assertEquals("100", JsonHelper.sanitizeCursorValue("100"))
        assertEquals("77.5", JsonHelper.sanitizeCursorValue(77.5))
    }

    @Test
    fun testParseAnimeItem() {
        val map = mapOf(
            "id" to "123",
            "title" to "Sousou no Frieren",
            "status" to "Completed",
            "image_poster" to "/images/frieren.jpg"
        )
        val anime = JsonHelper.parseAnimeItem(map)
        assertNotNull(anime)
        assertEquals("123", anime?.id)
        assertEquals("Sousou no Frieren", anime?.title)
        assertEquals("https://xyz-api.animein.net/images/frieren.jpg", anime?.getPosterUrl())
    }

    @Test
    fun testParseEmptyListDoesNotCrash() {
        val list = JsonHelper.parseAnimeList(emptyList<Any>())
        assertEquals(0, list.size)

        val nullList = JsonHelper.parseAnimeList(null)
        assertEquals(0, nullList.size)
    }
}
