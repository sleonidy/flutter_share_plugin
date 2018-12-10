package com.sleonidy.share.fluttershareplugin

data class FlutterSharePluginParams(
    val isMultiple: Boolean,
    val type: FlutterSharePlugin.ShareType,
    val title: String,
    val text: String,
    val path: String
) {
    companion object {
        const val TITLE = "title"
        const val TEXT = "text"
        const val PATH = "path"
        const val TYPE = "type"
        const val IS_MULTIPLE = "is_multiple"
    }
}