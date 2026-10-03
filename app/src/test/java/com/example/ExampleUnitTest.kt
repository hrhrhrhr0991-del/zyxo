package com.example

import com.example.data.api.Content
import com.example.data.api.GenerateContentRequest
import com.example.data.api.GeminiModelConstants
import com.example.data.api.Part
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testModelConstants() {
        assertEquals("gemini-3.5-flash", GeminiModelConstants.GEMINI_3_5_FLASH)
        assertEquals("gemini-3.1-pro-preview", GeminiModelConstants.GEMINI_3_1_PRO)
        assertEquals("gemini-3.1-flash-lite-preview", GeminiModelConstants.GEMINI_3_1_FLASH_LITE)
        assertEquals("gemini-3-pro-image-preview", GeminiModelConstants.GEMINI_3_PRO_IMAGE)
        assertEquals("gemini-3.1-flash-image-preview", GeminiModelConstants.GEMINI_3_1_FLASH_IMAGE)
        assertEquals("veo-3.1-fast-generate-preview", GeminiModelConstants.VEO_3_1_FAST)
    }

    @Test
    fun testRequestStructure() {
        val request = GenerateContentRequest(
            contents = listOf(
                Content(
                    role = "user",
                    parts = listOf(Part(text = "Hello Gemini"))
                )
            )
        )
        assertNotNull(request.contents)
        assertEquals(1, request.contents.size)
        assertEquals("Hello Gemini", request.contents[0].parts[0].text)
    }
}
