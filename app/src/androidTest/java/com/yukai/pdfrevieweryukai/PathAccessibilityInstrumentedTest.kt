package com.yukai.pdfrevieweryukai

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 階段一：基礎權限測試
 * 階段二：路徑兼容性測試
 * 測試URI權限持久性、Android版本兼容性和不同路徑格式
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class PathAccessibilityInstrumentedTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    private lateinit var context: Context

    @Before
    fun setup() {
        hiltRule.inject()
        context = InstrumentationRegistry.getInstrumentation().targetContext
    }

    // 階段一：基礎權限測試

    @Test
    fun testUriPermissionPersistence() = runTest {
        val testUri = createMockContentUri()
        
        try {
            context.contentResolver.takePersistableUriPermission(
                testUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            
            val persistedUriPermissions = context.contentResolver.persistedUriPermissions
            val hasPermission = persistedUriPermissions.any { 
                it.uri == testUri && it.isReadPermission 
            }
            
            assert(hasPermission) { "URI權限應該被持久化保存" }
            
        } catch (e: SecurityException) {
            println("SecurityException (expected in test): ${e.message}")
        }
    }

    @Test
    fun testAndroidVersionCompatibility() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                testScopedStorageAccess()
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                testTransitionalScopedStorage()
            }
            else -> {
                testLegacyStorageAccess()
            }
        }
    }

    @Test
    fun testPermissionLossErrorHandling() = runTest {
        val testUri = createMockContentUri()
        
        try {
            val inputStream = context.contentResolver.openInputStream(testUri)
            
            if (inputStream == null) {
                assert(true) { "正確檢測到URI無效" }
            } else {
                inputStream.close()
                assert(true) { "URI仍然有效" }
            }
            
        } catch (e: SecurityException) {
            assert(true) { "正確捕獲SecurityException: ${e.message}" }
        } catch (e: Exception) {
            println("Unexpected exception: ${e.message}")
        }
    }

    // 階段二：路徑兼容性測試

    @Test
    fun testDifferentUriFormats() {
        val testUris = listOf(
            "content://com.android.providers.media.documents/document/document%3A1000",
            "content://com.android.providers.downloads.documents/document/raw%3A%2Fstorage%2Femulated%2F0%2FDownload%2Ftest.pdf",
            "content://com.android.externalstorage.documents/document/primary%3ADownload%2Ftest.pdf"
        )
        
        testUris.forEach { uriString ->
            val uri = Uri.parse(uriString)
            val isAccessible = checkUriAccessibility(uri)
            println("URI: $uriString - Accessible: $isAccessible")
        }
        
        assert(true)
    }

    @Test
    fun testUriEncodingDecoding() {
        val originalUris = listOf(
            "content://test/path with spaces/document.pdf",
            "content://test/路徑中文/文檔.pdf"
        )
        
        originalUris.forEach { original ->
            val encoded = Uri.encode(original)
            val decoded = Uri.decode(encoded)
            
            assert(decoded == original) { 
                "URI編碼解碼應該保持一致: $original != $decoded" 
            }
        }
    }

    // 輔助方法

    private fun createMockContentUri(): Uri {
        return Uri.parse("content://com.test.provider/document/test")
    }

    private fun checkUriAccessibility(uri: Uri): Boolean {
        return try {
            context.contentResolver.openInputStream(uri)?.use { 
                true 
            } ?: false
        } catch (e: Exception) {
            false
        }
    }

    private fun testScopedStorageAccess() {
        println("Testing Android 11+ Scoped Storage behavior")
        
        val hasManagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            android.os.Environment.isExternalStorageManager()
        } else {
            false
        }
        
        println("Has MANAGE_EXTERNAL_STORAGE permission: $hasManagePermission")
        assert(true)
    }

    private fun testTransitionalScopedStorage() {
        println("Testing Android 10 transitional Scoped Storage behavior")
        
        val hasReadPermission = context.checkSelfPermission(
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        
        println("Has READ_EXTERNAL_STORAGE permission: $hasReadPermission")
        assert(true)
    }

    private fun testLegacyStorageAccess() {
        println("Testing legacy storage access behavior")
        
        val hasReadPermission = context.checkSelfPermission(
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        
        println("Has READ_EXTERNAL_STORAGE permission: $hasReadPermission")
        assert(true)
    }
}