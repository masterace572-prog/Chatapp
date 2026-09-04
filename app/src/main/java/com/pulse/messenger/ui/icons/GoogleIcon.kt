package com.pulse.messenger.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * Official-style Google "G" brand glyph (S02 "Continue with Google" button).
 * Four flat brand colours - no gradients. Paths from Google's public brand
 * assets; rendered untinted via AppButton.leadingContent.
 */
object GoogleIcon {
    val G: ImageVector by lazy {
        // 20x20 official glyph geometry (one path per colour).
        val pBlue = "M17.64 10.2c0-.637-.057-1.25-.164-1.84H10v3.48h4.29c-.185.998-.747 1.843-1.592 2.41v2h2.579c1.508-1.39 2.363-3.434 2.363-6.05z"
        val pGreen = "M10 18c2.16 0 3.972-.716 5.296-1.94l-2.58-2c-.716.48-1.632.763-2.716.763-2.09 0-3.858-1.41-4.49-3.305H2.83v2.064A8 8 0 0 0 10 18z"
        val pYellow = "M5.51 11.518A4.82 4.82 0 0 1 5.26 10c0-.527.09-1.04.25-1.518V6.418H2.83A8.02 8.02 0 0 0 2 10c0 1.29.31 2.51.83 3.582l2.68-2.064z"
        val pRed = "M10 5.02c1.174 0 2.228.403 3.057 1.194l2.293-2.293C13.972 2.617 12.16 2 10 2 7.24 2 4.81 3.437 3.42 5.77L5.51 8.08C6.14 6.187 7.91 5.02 10 5.02z"

        fun add(builder: ImageVector.Builder, colorHex: String, d: String) {
            builder.addPath(
                pathData = PathParser().parsePathString(d).toNodes(),
                fill = SolidColor(Color(android.graphics.Color.parseColor(colorHex))),
            )
        }

        val builder = ImageVector.Builder(
            name = "google_g",
            defaultWidth = 20.dp,
            defaultHeight = 20.dp,
            viewportWidth = 20f,
            viewportHeight = 20f,
        )
        add(builder, "#4285F4", pBlue)
        add(builder, "#34A853", pGreen)
        add(builder, "#FBBC05", pYellow)
        add(builder, "#EA4335", pRed)
        builder.build()
    }
}
