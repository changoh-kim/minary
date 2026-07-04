package kr.co.presentation.feature.setting.screen.profileedit.formatter

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ProfilePhotoUriFormatterTest {

    @Test
    fun `toDisplayUrl keeps blank url blank`() {
        assertEquals("", ProfilePhotoUriFormatter.toDisplayUrl(""))
    }

    @Test
    fun `toDisplayUrl appends timestamp query after storage url`() {
        val displayUrl = ProfilePhotoUriFormatter.toDisplayUrl("profile-photo-url-test")

        assertTrue(displayUrl.startsWith("profile-photo-url-test?t="))
    }

    @Test
    fun `toStorageUrl removes timestamp query`() {
        assertEquals(
            "profile-photo-url-test",
            ProfilePhotoUriFormatter.toStorageUrl("profile-photo-url-test?t=1234")
        )
    }
}
