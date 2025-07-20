package com.yukai.pdfrevieweryukai

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.yukai.pdfrevieweryukai.data.repository.PdfRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

/**
 * Example instrumented test demonstrating Hilt dependency injection.
 * 
 * This test shows how to use the HiltTestRunner to inject dependencies
 * in instrumented tests.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ExampleHiltTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var pdfRepository: PdfRepository

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun testRepositoryInjection() {
        // Test that the repository is properly injected
        assert(::pdfRepository.isInitialized)
    }
}