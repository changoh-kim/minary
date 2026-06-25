package kr.co.presentation.feature.setting.screen.profileedit.formatter

object ProfilePhotoUriFormatter {
    private const val TIMESTAMP_PARAM = "?t="

    fun toDisplayUrl(storageUrl: String): String {
        if (storageUrl.isBlank()) return storageUrl
        return "${toStorageUrl(storageUrl)}$TIMESTAMP_PARAM${System.currentTimeMillis()}"
    }

    fun toStorageUrl(displayUrl: String): String {
        return displayUrl.substringBefore(TIMESTAMP_PARAM)
    }
}