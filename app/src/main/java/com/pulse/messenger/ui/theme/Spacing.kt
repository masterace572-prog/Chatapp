package com.pulse.messenger.ui.theme

import androidx.compose.ui.unit.dp

/*
 * Pulse spacing & sizing tokens (PRD §4.3) on a 4/8dp grid.
 * Screen horizontal padding: 20dp. Min touch target: 48dp. Button height: 52dp.
 */
object PulseSpacing {
    /** Hairline gaps inside chat bubbles runs / between meta rows (2dp). */
    val tight = 2.dp
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
    val xxl = 24.dp
    val xxxl = 32.dp
    val huge = 40.dp
    val colossal = 48.dp
    /** Standard screen horizontal padding (PRD §4.3). */
    val screen = 20.dp
}

/** Icon sizes (PRD §4.4): 14 tiny (glyph micro-marks), 16 small (chip
 *  leading icons), 20 inline, 24 default, 28 feature. */
object PulseIconSizes {
    val tiny = 14.dp
    val small = 16.dp
    val inline = 20.dp
    val default = 24.dp
    val feature = 28.dp
}

/** Interaction & layout constants (PRD §4.3). */
object PulseSizes {
    val buttonHeight = 52.dp
    val minTouchTarget = 48.dp
    val avatarChat = 52.dp
    val topBarHeight = 56.dp
    val dividerHairline = 1.dp
    val inputHeight = 56.dp

    /** List divider indent aligning under the 52dp avatar block (S19/S21). */
    val chatRowDividerInset = 84.dp

    /** Bottom list padding so the floating action bar never covers the last row. */
    val fabClearance = 96.dp

    /** Conversation header avatar (S23). */
    val avatarHeader = 40.dp

    /** Inline avatar on the last bubble of a group run (S23). */
    val avatarRun = 24.dp

    /** Small sender prefix avatar inside group bubble rows (S23). */
    val bubbleMetaIcon = 14.dp

    /* Chat-list & conversation glyph metrics (S19/S23). */
    val unreadOverlayBadge = 14.dp
    val selectionCheck = 22.dp
    val selectionCheckMark = 13.dp
    val selectionStroke = 2.dp
    val sendCircle = 40.dp

    /** Conversation media-attachment tiles (M4c S24). */
    val attachmentTile = 56.dp

    /** Thumbnail strip tile in the media review sheet (M4c S25). */
    val attachmentStrip = 52.dp

    /** Simulated camera shutter diameter (M4c S26). */
    val cameraShutter = 78.dp

    /** Fixed 16:9 preview height in the media review sheet (M4c S25). */
    val videoPreviewHeight = 220.dp

    /** Center play/pause control in the media viewer (M4c S33). */
    val playerButton = 72.dp
}
