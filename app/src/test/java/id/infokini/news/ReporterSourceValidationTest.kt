package id.infokini.news

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReporterSourceValidationTest {
    @Test fun acceptsHttpAndHttpsUrls() {
        assertTrue(isValidHttpUrl("https://example.com/news"))
        assertTrue(isValidHttpUrl("http://example.org/story"))
    }

    @Test fun rejectsInvalidOrUnsafeSchemes() {
        assertFalse(isValidHttpUrl(""))
        assertFalse(isValidHttpUrl("javascript:alert(1)"))
        assertFalse(isValidHttpUrl("file:///etc/passwd"))
        assertFalse(isValidHttpUrl("https:///missing-host"))
        assertFalse(isValidHttpUrl("not a url"))
    }

    @Test fun sanitizesExportFileNames() {
        assertTrue(safeExportFileName("Liputan Ciadeg 2026").matches(Regex("[A-Za-z0-9._-]+")))
        assertTrue(safeExportFileName("   ").isNotBlank())
        assertFalse(safeExportFileName("Liputan /:*?\"<>|").contains("/"))
    }

    @Test fun requiresDifferentNormalizedHosts() {
        assertTrue(isValidIndependentSourcePair("https://news.example.com/a", "https://other.org/b"))
        assertFalse(isValidIndependentSourcePair("https://example.com/a", "https://www.example.com/b"))
        assertFalse(isValidIndependentSourcePair("https://example.com/a", "https://example.com/b"))
        assertFalse(isValidIndependentSourcePair("invalid", "https://other.org/b"))
    }
}
