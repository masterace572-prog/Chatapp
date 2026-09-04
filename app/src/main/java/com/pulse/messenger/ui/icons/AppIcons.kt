package com.pulse.messenger.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/**
 * Pulse icon system.
 *
 * Single consistent vector icon set (PRD §4.4): stroke-based geometry at a 24dp
 * viewport with round caps/joins, in the visual language of Lucide.
 * Every icon is tinted at the usage site - never baked into the vector.
 * Each icon keeps one ImageVector with one path per source primitive, so no
 * geometry is dropped (multi-part icons: mail, users, smartphone, camera...).
 * Icon path data source: Lucide (ISC licence, https://lucide.dev).
 */
object AppIcons {

    val ArrowLeft: ImageVector by lazy {
        strokeIcon(
            "ArrowLeft",
            listOf(
                "m12 19-7-7 7-7",
                "M19 12H5",
            ),
        )
    }

    val ArrowRight: ImageVector by lazy {
        strokeIcon(
            "ArrowRight",
            listOf(
                "M5 12h14",
                "m12 5 7 7-7 7",
            ),
        )
    }

    val ArrowUp: ImageVector by lazy {
        strokeIcon(
            "ArrowUp",
            listOf(
                "m5 12 7-7 7 7",
                "M12 19V5",
            ),
        )
    }

    val ArrowDown: ImageVector by lazy {
        strokeIcon(
            "ArrowDown",
            listOf(
                "M12 5v14",
                "m19 12-7 7-7-7",
            ),
        )
    }

    val Search: ImageVector by lazy {
        strokeIcon(
            "Search",
            listOf(
                "M3.000,11.000 a8.000,8.000 0 1,0 16.000,0 a8.000,8.000 0 1,0 -16.000,0",
                "m21 21-4.3-4.3",
            ),
        )
    }

    val EllipsisVertical: ImageVector by lazy {
        strokeIcon(
            "EllipsisVertical",
            listOf(
                "M11.000,12.000 a1.000,1.000 0 1,0 2.000,0 a1.000,1.000 0 1,0 -2.000,0",
                "M11.000,5.000 a1.000,1.000 0 1,0 2.000,0 a1.000,1.000 0 1,0 -2.000,0",
                "M11.000,19.000 a1.000,1.000 0 1,0 2.000,0 a1.000,1.000 0 1,0 -2.000,0",
            ),
        )
    }

    val EllipsisHorizontal: ImageVector by lazy {
        strokeIcon(
            "EllipsisHorizontal",
            listOf(
                "M11.000,12.000 a1.000,1.000 0 1,0 2.000,0 a1.000,1.000 0 1,0 -2.000,0",
                "M18.000,12.000 a1.000,1.000 0 1,0 2.000,0 a1.000,1.000 0 1,0 -2.000,0",
                "M4.000,12.000 a1.000,1.000 0 1,0 2.000,0 a1.000,1.000 0 1,0 -2.000,0",
            ),
        )
    }

    val Plus: ImageVector by lazy {
        strokeIcon(
            "Plus",
            listOf(
                "M5 12h14",
                "M12 5v14",
            ),
        )
    }

    val Minus: ImageVector by lazy {
        strokeIcon("Minus", listOf("M5 12h14"))
    }

    val Close: ImageVector by lazy {
        strokeIcon(
            "Close",
            listOf(
                "M18 6 6 18",
                "m6 6 12 12",
            ),
        )
    }

    val CircleX: ImageVector by lazy {
        strokeIcon(
            "CircleX",
            listOf(
                "M2.000,12.000 a10.000,10.000 0 1,0 20.000,0 a10.000,10.000 0 1,0 -20.000,0",
                "m15 9-6 6",
                "m9 9 6 6",
            ),
        )
    }

    val Check: ImageVector by lazy {
        strokeIcon("Check", listOf("M20 6 9 17l-5-5"))
    }

    val CheckCheck: ImageVector by lazy {
        strokeIcon(
            "CheckCheck",
            listOf(
                "M18 6 7 17l-5-5",
                "m22 10-7.5 7.5L13 16",
            ),
        )
    }

    val ChevronDown: ImageVector by lazy {
        strokeIcon("ChevronDown", listOf("m6 9 6 6 6-6"))
    }

    val ChevronLeft: ImageVector by lazy {
        strokeIcon("ChevronLeft", listOf("m15 18-6-6 6-6"))
    }

    val ChevronRight: ImageVector by lazy {
        strokeIcon("ChevronRight", listOf("m9 18 6-6-6-6"))
    }

    val ChevronUp: ImageVector by lazy {
        strokeIcon("ChevronUp", listOf("m18 15-6-6-6 6"))
    }

    val Settings: ImageVector by lazy {
        strokeIcon(
            "Settings",
            listOf(
                "M12.22 2h-.44a2 2 0 0 0-2 2v.18a2 2 0 0 1-1 1.73l-.43.25a2 2 0 0 1-2 0l-.15-.08a2 2 0 0 0-2.73.73l-.22.38a2 2 0 0 0 .73 2.73l.15.1a2 2 0 0 1 1 1.72v.51a2 2 0 0 1-1 1.74l-.15.09a2 2 0 0 0-.73 2.73l.22.38a2 2 0 0 0 2.73.73l.15-.08a2 2 0 0 1 2 0l.43.25a2 2 0 0 1 1 1.73V20a2 2 0 0 0 2 2h.44a2 2 0 0 0 2-2v-.18a2 2 0 0 1 1-1.73l.43-.25a2 2 0 0 1 2 0l.15.08a2 2 0 0 0 2.73-.73l.22-.39a2 2 0 0 0-.73-2.73l-.15-.08a2 2 0 0 1-1-1.74v-.5a2 2 0 0 1 1-1.74l.15-.09a2 2 0 0 0 .73-2.73l-.22-.38a2 2 0 0 0-2.73-.73l-.15.08a2 2 0 0 1-2 0l-.43-.25a2 2 0 0 1-1-1.73V4a2 2 0 0 0-2-2z",
                "M9.000,12.000 a3.000,3.000 0 1,0 6.000,0 a3.000,3.000 0 1,0 -6.000,0",
            ),
        )
    }

    val MessageCircle: ImageVector by lazy {
        strokeIcon("MessageCircle", listOf("M7.9 20A9 9 0 1 0 4 16.1L2 22Z"))
    }

    val MessageSquare: ImageVector by lazy {
        strokeIcon("MessageSquare", listOf("M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"))
    }

    val Phone: ImageVector by lazy {
        strokeIcon("Phone", listOf("M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"))
    }

    val PhoneOff: ImageVector by lazy {
        strokeIcon(
            "PhoneOff",
            listOf(
                "M10.68 13.31a16 16 0 0 0 3.41 2.6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7 2 2 0 0 1 1.72 2v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.42 19.42 0 0 1-3.33-2.67m-2.67-3.34a19.79 19.79 0 0 1-3.07-8.63A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91",
                "M22,2 L2,22",
            ),
        )
    }

    val PhoneMissed: ImageVector by lazy {
        strokeIcon(
            "PhoneMissed",
            listOf(
                "M22,2 L16,8",
                "M16,2 L22,8",
                "M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z",
            ),
        )
    }

    val PhoneIncoming: ImageVector by lazy {
        strokeIcon(
            "PhoneIncoming",
            listOf(
                "M",
                "M22,2 L16,8",
                "M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z",
            ),
        )
    }

    val PhoneOutgoing: ImageVector by lazy {
        strokeIcon(
            "PhoneOutgoing",
            listOf(
                "M",
                "M16,8 L22,2",
                "M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z",
            ),
        )
    }

    val Video: ImageVector by lazy {
        strokeIcon(
            "Video",
            listOf(
                "m16 13 5.223 3.482a.5.5 0 0 0 .777-.416V7.87a.5.5 0 0 0-.752-.432L16 10.5",
                "M4.000,6.000 h10.000 a2.000,2.000 0 0 1 2.000,2.000 v8.000 a2.000,2.000 0 0 1 -2.000,2.000 h-10.000 a2.000,2.000 0 0 1 -2.000,-2.000 v-8.000 a2.000,2.000 0 0 1 2.000,-2.000 z",
            ),
        )
    }

    val VideoOff: ImageVector by lazy {
        strokeIcon(
            "VideoOff",
            listOf(
                "M10.66 6H14a2 2 0 0 1 2 2v2.5l5.248-3.062A.5.5 0 0 1 22 7.87v8.196",
                "M16 16a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h2",
                "m2 2 20 20",
            ),
        )
    }

    val Users: ImageVector by lazy {
        strokeIcon(
            "Users",
            listOf(
                "M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2",
                "M5.000,7.000 a4.000,4.000 0 1,0 8.000,0 a4.000,4.000 0 1,0 -8.000,0",
                "M22 21v-2a4 4 0 0 0-3-3.87",
                "M16 3.13a4 4 0 0 1 0 7.75",
            ),
        )
    }

    val User: ImageVector by lazy {
        strokeIcon(
            "User",
            listOf(
                "M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2",
                "M8.000,7.000 a4.000,4.000 0 1,0 8.000,0 a4.000,4.000 0 1,0 -8.000,0",
            ),
        )
    }

    val UserPlus: ImageVector by lazy {
        strokeIcon(
            "UserPlus",
            listOf(
                "M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2",
                "M5.000,7.000 a4.000,4.000 0 1,0 8.000,0 a4.000,4.000 0 1,0 -8.000,0",
                "M19,8 L19,14",
                "M22,11 L16,11",
            ),
        )
    }

    val UserMinus: ImageVector by lazy {
        strokeIcon(
            "UserMinus",
            listOf(
                "M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2",
                "M5.000,7.000 a4.000,4.000 0 1,0 8.000,0 a4.000,4.000 0 1,0 -8.000,0",
                "M22,11 L16,11",
            ),
        )
    }

    val UserX: ImageVector by lazy {
        strokeIcon(
            "UserX",
            listOf(
                "M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2",
                "M5.000,7.000 a4.000,4.000 0 1,0 8.000,0 a4.000,4.000 0 1,0 -8.000,0",
                "M17,8 L22,13",
                "M22,8 L17,13",
            ),
        )
    }

    val UserCheck: ImageVector by lazy {
        strokeIcon(
            "UserCheck",
            listOf(
                "M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2",
                "M5.000,7.000 a4.000,4.000 0 1,0 8.000,0 a4.000,4.000 0 1,0 -8.000,0",
                "M",
            ),
        )
    }

    val LogOut: ImageVector by lazy {
        strokeIcon(
            "LogOut",
            listOf(
                "M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4",
                "M",
                "M21,12 L9,12",
            ),
        )
    }

    val Share: ImageVector by lazy {
        strokeIcon(
            "Share",
            listOf(
                "M4 12v8a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2v-8",
                "M",
                "M12,2 L12,15",
            ),
        )
    }

    val Download: ImageVector by lazy {
        strokeIcon(
            "Download",
            listOf(
                "M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4",
                "M",
                "M12,15 L12,3",
            ),
        )
    }

    val Upload: ImageVector by lazy {
        strokeIcon(
            "Upload",
            listOf(
                "M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4",
                "M",
                "M12,3 L12,15",
            ),
        )
    }

    val Save: ImageVector by lazy {
        strokeIcon(
            "Save",
            listOf(
                "M15.2 3a2 2 0 0 1 1.4.6l3.8 3.8a2 2 0 0 1 .6 1.4V19a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2z",
                "M17 21v-7a1 1 0 0 0-1-1H8a1 1 0 0 0-1 1v7",
                "M7 3v4a1 1 0 0 0 1 1h7",
            ),
        )
    }

    val Trash: ImageVector by lazy {
        strokeIcon(
            "Trash",
            listOf(
                "M3 6h18",
                "M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6",
                "M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2",
                "M10,11 L10,17",
                "M14,11 L14,17",
            ),
        )
    }

    val Pencil: ImageVector by lazy {
        strokeIcon(
            "Pencil",
            listOf(
                "M21.174 6.812a1 1 0 0 0-3.986-3.987L3.842 16.174a2 2 0 0 0-.5.83l-1.321 4.352a.5.5 0 0 0 .623.622l4.353-1.32a2 2 0 0 0 .83-.497z",
                "m15 5 4 4",
            ),
        )
    }

    val Star: ImageVector by lazy {
        strokeIcon("Star", listOf("M11.525 2.295a.53.53 0 0 1 .95 0l2.31 4.679a2.123 2.123 0 0 0 1.595 1.16l5.166.756a.53.53 0 0 1 .294.904l-3.736 3.638a2.123 2.123 0 0 0-.611 1.878l.882 5.14a.53.53 0 0 1-.771.56l-4.618-2.428a2.122 2.122 0 0 0-1.973 0L6.396 21.01a.53.53 0 0 1-.77-.56l.881-5.139a2.122 2.122 0 0 0-.611-1.879L2.16 9.795a.53.53 0 0 1 .294-.906l5.165-.755a2.122 2.122 0 0 0 1.597-1.16z"))
    }

    val Pin: ImageVector by lazy {
        strokeIcon(
            "Pin",
            listOf(
                "M12 17v5",
                "M9 10.76a2 2 0 0 1-1.11 1.79l-1.78.9A2 2 0 0 0 5 15.24V16a1 1 0 0 0 1 1h12a1 1 0 0 0 1-1v-.76a2 2 0 0 0-1.11-1.79l-1.78-.9A2 2 0 0 1 15 10.76V7a1 1 0 0 1 1-1 2 2 0 0 0 0-4H8a2 2 0 0 0 0 4 1 1 0 0 1 1 1z",
            ),
        )
    }

    val PinOff: ImageVector by lazy {
        strokeIcon(
            "PinOff",
            listOf(
                "M12 17v5",
                "M15 9.34V7a1 1 0 0 1 1-1 2 2 0 0 0 0-4H7.89",
                "m2 2 20 20",
                "M9 9v1.76a2 2 0 0 1-1.11 1.79l-1.78.9A2 2 0 0 0 5 15.24V16a1 1 0 0 0 1 1h11",
            ),
        )
    }

    val Archive: ImageVector by lazy {
        strokeIcon(
            "Archive",
            listOf(
                "M3.000,3.000 h18.000 a1.000,1.000 0 0 1 1.000,1.000 v3.000 a1.000,1.000 0 0 1 -1.000,1.000 h-18.000 a1.000,1.000 0 0 1 -1.000,-1.000 v-3.000 a1.000,1.000 0 0 1 1.000,-1.000 z",
                "M4 8v11a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8",
                "M10 12h4",
            ),
        )
    }

    val ArchiveRestore: ImageVector by lazy {
        strokeIcon(
            "ArchiveRestore",
            listOf(
                "M3.000,3.000 h18.000 a1.000,1.000 0 0 1 1.000,1.000 v3.000 a1.000,1.000 0 0 1 -1.000,1.000 h-18.000 a1.000,1.000 0 0 1 -1.000,-1.000 v-3.000 a1.000,1.000 0 0 1 1.000,-1.000 z",
                "M4 8v11a2 2 0 0 0 2 2h2",
                "M20 8v11a2 2 0 0 1-2 2h-2",
                "m9 15 3-3 3 3",
                "M12 12v9",
            ),
        )
    }

    val Mic: ImageVector by lazy {
        strokeIcon(
            "Mic",
            listOf(
                "M12 2a3 3 0 0 0-3 3v7a3 3 0 0 0 6 0V5a3 3 0 0 0-3-3Z",
                "M19 10v2a7 7 0 0 1-14 0v-2",
                "M12,19 L12,22",
            ),
        )
    }

    val MicOff: ImageVector by lazy {
        strokeIcon(
            "MicOff",
            listOf(
                "M2,2 L22,22",
                "M18.89 13.23A7.12 7.12 0 0 0 19 12v-2",
                "M5 10v2a7 7 0 0 0 12 5",
                "M15 9.34V5a3 3 0 0 0-5.68-1.33",
                "M9 9v3a3 3 0 0 0 5.12 2.12",
                "M12,19 L12,22",
            ),
        )
    }

    val Send: ImageVector by lazy {
        strokeIcon(
            "Send",
            listOf(
                "M14.536 21.686a.5.5 0 0 0 .937-.024l6.5-19a.496.496 0 0 0-.635-.635l-19 6.5a.5.5 0 0 0-.024.937l7.93 3.18a2 2 0 0 1 1.112 1.11z",
                "m21.854 2.147-10.94 10.939",
            ),
        )
    }

    val Camera: ImageVector by lazy {
        strokeIcon(
            "Camera",
            listOf(
                "M14.5 4h-5L7 7H4a2 2 0 0 0-2 2v9a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2V9a2 2 0 0 0-2-2h-3l-2.5-3z",
                "M9.000,13.000 a3.000,3.000 0 1,0 6.000,0 a3.000,3.000 0 1,0 -6.000,0",
            ),
        )
    }

    val CameraOff: ImageVector by lazy {
        strokeIcon(
            "CameraOff",
            listOf(
                "M2,2 L22,22",
                "M7 7H4a2 2 0 0 0-2 2v9a2 2 0 0 0 2 2h16",
                "M9.5 4h5L17 7h3a2 2 0 0 1 2 2v7.5",
                "M14.121 15.121A3 3 0 1 1 9.88 10.88",
            ),
        )
    }

    val Paperclip: ImageVector by lazy {
        strokeIcon("Paperclip", listOf("m21.44 11.05-9.19 9.19a6 6 0 0 1-8.49-8.49l8.57-8.57A4 4 0 1 1 18 8.84l-8.59 8.57a2 2 0 0 1-2.83-2.83l8.49-8.48"))
    }

    val Image: ImageVector by lazy {
        strokeIcon(
            "Image",
            listOf(
                "M5.000,3.000 h14.000 a2.000,2.000 0 0 1 2.000,2.000 v14.000 a2.000,2.000 0 0 1 -2.000,2.000 h-14.000 a2.000,2.000 0 0 1 -2.000,-2.000 v-14.000 a2.000,2.000 0 0 1 2.000,-2.000 z",
                "M7.000,9.000 a2.000,2.000 0 1,0 4.000,0 a2.000,2.000 0 1,0 -4.000,0",
                "m21 15-3.086-3.086a2 2 0 0 0-2.828 0L6 21",
            ),
        )
    }

    val Images: ImageVector by lazy {
        strokeIcon(
            "Images",
            listOf(
                "M18 22H4a2 2 0 0 1-2-2V6",
                "m22 13-1.296-1.296a2.41 2.41 0 0 0-3.408 0L11 18",
                "M10.000,8.000 a2.000,2.000 0 1,0 4.000,0 a2.000,2.000 0 1,0 -4.000,0",
                "M8.000,2.000 h12.000 a2.000,2.000 0 0 1 2.000,2.000 v12.000 a2.000,2.000 0 0 1 -2.000,2.000 h-12.000 a2.000,2.000 0 0 1 -2.000,-2.000 v-12.000 a2.000,2.000 0 0 1 2.000,-2.000 z",
            ),
        )
    }

    val Smile: ImageVector by lazy {
        strokeIcon(
            "Smile",
            listOf(
                "M2.000,12.000 a10.000,10.000 0 1,0 20.000,0 a10.000,10.000 0 1,0 -20.000,0",
                "M8 14s1.5 2 4 2 4-2 4-2",
                "M9,9 L9.01,9",
                "M15,9 L15.01,9",
            ),
        )
    }

    val Lock: ImageVector by lazy {
        strokeIcon(
            "Lock",
            listOf(
                "M5.000,11.000 h14.000 a2.000,2.000 0 0 1 2.000,2.000 v7.000 a2.000,2.000 0 0 1 -2.000,2.000 h-14.000 a2.000,2.000 0 0 1 -2.000,-2.000 v-7.000 a2.000,2.000 0 0 1 2.000,-2.000 z",
                "M7 11V7a5 5 0 0 1 10 0v4",
            ),
        )
    }

    val LockOpen: ImageVector by lazy {
        strokeIcon(
            "LockOpen",
            listOf(
                "M5.000,11.000 h14.000 a2.000,2.000 0 0 1 2.000,2.000 v7.000 a2.000,2.000 0 0 1 -2.000,2.000 h-14.000 a2.000,2.000 0 0 1 -2.000,-2.000 v-7.000 a2.000,2.000 0 0 1 2.000,-2.000 z",
                "M7 11V7a5 5 0 0 1 9.9-1",
            ),
        )
    }

    val Shield: ImageVector by lazy {
        strokeIcon("Shield", listOf("M20 13c0 5-3.5 7.5-7.66 8.95a1 1 0 0 1-.67-.01C7.5 20.5 4 18 4 13V6a1 1 0 0 1 1-1c2 0 4.5-1.2 6.24-2.72a1.17 1.17 0 0 1 1.52 0C14.51 3.81 17 5 19 5a1 1 0 0 1 1 1z"))
    }

    val ShieldCheck: ImageVector by lazy {
        strokeIcon(
            "ShieldCheck",
            listOf(
                "M20 13c0 5-3.5 7.5-7.66 8.95a1 1 0 0 1-.67-.01C7.5 20.5 4 18 4 13V6a1 1 0 0 1 1-1c2 0 4.5-1.2 6.24-2.72a1.17 1.17 0 0 1 1.52 0C14.51 3.81 17 5 19 5a1 1 0 0 1 1 1z",
                "m9 12 2 2 4-4",
            ),
        )
    }

    val Bell: ImageVector by lazy {
        strokeIcon(
            "Bell",
            listOf(
                "M6 8a6 6 0 0 1 12 0c0 7 3 9 3 9H3s3-2 3-9",
                "M10.3 21a1.94 1.94 0 0 0 3.4 0",
            ),
        )
    }

    val BellOff: ImageVector by lazy {
        strokeIcon(
            "BellOff",
            listOf(
                "M8.7 3A6 6 0 0 1 18 8a21.3 21.3 0 0 0 .6 5",
                "M17 17H3s3-2 3-9a4.67 4.67 0 0 1 .3-1.7",
                "M10.3 21a1.94 1.94 0 0 0 3.4 0",
                "m2 2 20 20",
            ),
        )
    }

    val BellRing: ImageVector by lazy {
        strokeIcon(
            "BellRing",
            listOf(
                "M6 8a6 6 0 0 1 12 0c0 7 3 9 3 9H3s3-2 3-9",
                "M10.3 21a1.94 1.94 0 0 0 3.4 0",
                "M4 2C2.8 3.7 2 5.7 2 8",
                "M22 8c0-2.3-.8-4.3-2-6",
            ),
        )
    }

    val Moon: ImageVector by lazy {
        strokeIcon("Moon", listOf("M12 3a6 6 0 0 0 9 9 9 9 0 1 1-9-9Z"))
    }

    val Sun: ImageVector by lazy {
        strokeIcon(
            "Sun",
            listOf(
                "M8.000,12.000 a4.000,4.000 0 1,0 8.000,0 a4.000,4.000 0 1,0 -8.000,0",
                "M12 2v2",
                "M12 20v2",
                "m4.93 4.93 1.41 1.41",
                "m17.66 17.66 1.41 1.41",
                "M2 12h2",
                "M20 12h2",
                "m6.34 17.66-1.41 1.41",
                "m19.07 4.93-1.41 1.41",
            ),
        )
    }

    val Globe: ImageVector by lazy {
        strokeIcon(
            "Globe",
            listOf(
                "M2.000,12.000 a10.000,10.000 0 1,0 20.000,0 a10.000,10.000 0 1,0 -20.000,0",
                "M12 2a14.5 14.5 0 0 0 0 20 14.5 14.5 0 0 0 0-20",
                "M2 12h20",
            ),
        )
    }

    val HelpCircle: ImageVector by lazy {
        strokeIcon(
            "HelpCircle",
            listOf(
                "M2.000,12.000 a10.000,10.000 0 1,0 20.000,0 a10.000,10.000 0 1,0 -20.000,0",
                "M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3",
                "M12 17h.01",
            ),
        )
    }

    val Info: ImageVector by lazy {
        strokeIcon(
            "Info",
            listOf(
                "M2.000,12.000 a10.000,10.000 0 1,0 20.000,0 a10.000,10.000 0 1,0 -20.000,0",
                "M12 16v-4",
                "M12 8h.01",
            ),
        )
    }

    val AlertCircle: ImageVector by lazy {
        strokeIcon(
            "AlertCircle",
            listOf(
                "M2.000,12.000 a10.000,10.000 0 1,0 20.000,0 a10.000,10.000 0 1,0 -20.000,0",
                "M12,8 L12,12",
                "M12,16 L12.01,16",
            ),
        )
    }

    val AlertTriangle: ImageVector by lazy {
        strokeIcon(
            "AlertTriangle",
            listOf(
                "m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3",
                "M12 9v4",
                "M12 17h.01",
            ),
        )
    }

    val Mail: ImageVector by lazy {
        strokeIcon(
            "Mail",
            listOf(
                "M4.000,4.000 h16.000 a2.000,2.000 0 0 1 2.000,2.000 v12.000 a2.000,2.000 0 0 1 -2.000,2.000 h-16.000 a2.000,2.000 0 0 1 -2.000,-2.000 v-12.000 a2.000,2.000 0 0 1 2.000,-2.000 z",
                "m22 7-8.97 5.7a1.94 1.94 0 0 1-2.06 0L2 7",
            ),
        )
    }

    val Eye: ImageVector by lazy {
        strokeIcon(
            "Eye",
            listOf(
                "M2.062 12.348a1 1 0 0 1 0-.696 10.75 10.75 0 0 1 19.876 0 1 1 0 0 1 0 .696 10.75 10.75 0 0 1-19.876 0",
                "M9.000,12.000 a3.000,3.000 0 1,0 6.000,0 a3.000,3.000 0 1,0 -6.000,0",
            ),
        )
    }

    val EyeOff: ImageVector by lazy {
        strokeIcon(
            "EyeOff",
            listOf(
                "M10.733 5.076a10.744 10.744 0 0 1 11.205 6.575 1 1 0 0 1 0 .696 10.747 10.747 0 0 1-1.444 2.49",
                "M14.084 14.158a3 3 0 0 1-4.242-4.242",
                "M17.479 17.499a10.75 10.75 0 0 1-15.417-5.151 1 1 0 0 1 0-.696 10.75 10.75 0 0 1 4.446-5.143",
                "m2 2 20 20",
            ),
        )
    }

    val Calendar: ImageVector by lazy {
        strokeIcon(
            "Calendar",
            listOf(
                "M8 2v4",
                "M16 2v4",
                "M5.000,4.000 h14.000 a2.000,2.000 0 0 1 2.000,2.000 v14.000 a2.000,2.000 0 0 1 -2.000,2.000 h-14.000 a2.000,2.000 0 0 1 -2.000,-2.000 v-14.000 a2.000,2.000 0 0 1 2.000,-2.000 z",
                "M3 10h18",
            ),
        )
    }

    val CalendarDays: ImageVector by lazy {
        strokeIcon(
            "CalendarDays",
            listOf(
                "M8 2v4",
                "M16 2v4",
                "M5.000,4.000 h14.000 a2.000,2.000 0 0 1 2.000,2.000 v14.000 a2.000,2.000 0 0 1 -2.000,2.000 h-14.000 a2.000,2.000 0 0 1 -2.000,-2.000 v-14.000 a2.000,2.000 0 0 1 2.000,-2.000 z",
                "M3 10h18",
                "M8 14h.01",
                "M12 14h.01",
                "M16 14h.01",
                "M8 18h.01",
                "M12 18h.01",
                "M16 18h.01",
            ),
        )
    }

    val Copy: ImageVector by lazy {
        strokeIcon(
            "Copy",
            listOf(
                "M10.000,8.000 h10.000 a2.000,2.000 0 0 1 2.000,2.000 v10.000 a2.000,2.000 0 0 1 -2.000,2.000 h-10.000 a2.000,2.000 0 0 1 -2.000,-2.000 v-10.000 a2.000,2.000 0 0 1 2.000,-2.000 z",
                "M4 16c-1.1 0-2-.9-2-2V4c0-1.1.9-2 2-2h10c1.1 0 2 .9 2 2",
            ),
        )
    }

    val RotateCw: ImageVector by lazy {
        strokeIcon(
            "RotateCw",
            listOf(
                "M21 12a9 9 0 1 1-9-9c2.52 0 4.93 1 6.74 2.74L21 8",
                "M21 3v5h-5",
            ),
        )
    }

    val MapPin: ImageVector by lazy {
        strokeIcon(
            "MapPin",
            listOf(
                "M20 10c0 4.993-5.539 10.193-7.399 11.799a1 1 0 0 1-1.202 0C9.539 20.193 4 14.993 4 10a8 8 0 0 1 16 0",
                "M9.000,10.000 a3.000,3.000 0 1,0 6.000,0 a3.000,3.000 0 1,0 -6.000,0",
            ),
        )
    }

    val File: ImageVector by lazy {
        strokeIcon(
            "File",
            listOf(
                "M15 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V7Z",
                "M14 2v4a2 2 0 0 0 2 2h4",
            ),
        )
    }

    val FileText: ImageVector by lazy {
        strokeIcon(
            "FileText",
            listOf(
                "M15 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V7Z",
                "M14 2v4a2 2 0 0 0 2 2h4",
                "M10 9H8",
                "M16 13H8",
                "M16 17H8",
            ),
        )
    }

    val FileAudio: ImageVector by lazy {
        strokeIcon(
            "FileAudio",
            listOf(
                "M17.5 22h.5a2 2 0 0 0 2-2V7l-5-5H6a2 2 0 0 0-2 2v3",
                "M14 2v4a2 2 0 0 0 2 2h4",
                "M2 19a2 2 0 1 1 4 0v1a2 2 0 1 1-4 0v-4a6 6 0 0 1 12 0v4a2 2 0 1 1-4 0v-1a2 2 0 1 1 4 0",
            ),
        )
    }

    val Music: ImageVector by lazy {
        strokeIcon(
            "Music",
            listOf(
                "M9 18V5l12-2v13",
                "M3.000,18.000 a3.000,3.000 0 1,0 6.000,0 a3.000,3.000 0 1,0 -6.000,0",
                "M15.000,16.000 a3.000,3.000 0 1,0 6.000,0 a3.000,3.000 0 1,0 -6.000,0",
            ),
        )
    }

    val Play: ImageVector by lazy {
        strokeIcon("Play", listOf("M Z"))
    }

    val Pause: ImageVector by lazy {
        strokeIcon(
            "Pause",
            listOf(
                "M15.000,4.000 h2.000 a1.000,1.000 0 0 1 1.000,1.000 v14.000 a1.000,1.000 0 0 1 -1.000,1.000 h-2.000 a1.000,1.000 0 0 1 -1.000,-1.000 v-14.000 a1.000,1.000 0 0 1 1.000,-1.000 z",
                "M7.000,4.000 h2.000 a1.000,1.000 0 0 1 1.000,1.000 v14.000 a1.000,1.000 0 0 1 -1.000,1.000 h-2.000 a1.000,1.000 0 0 1 -1.000,-1.000 v-14.000 a1.000,1.000 0 0 1 1.000,-1.000 z",
            ),
        )
    }

    val AudioLines: ImageVector by lazy {
        strokeIcon(
            "AudioLines",
            listOf(
                "M2 10v3",
                "M6 6v11",
                "M10 3v18",
                "M14 8v7",
                "M18 5v13",
                "M22 10v3",
            ),
        )
    }

    val AtSign: ImageVector by lazy {
        strokeIcon(
            "AtSign",
            listOf(
                "M8.000,12.000 a4.000,4.000 0 1,0 8.000,0 a4.000,4.000 0 1,0 -8.000,0",
                "M16 8v5a3 3 0 0 0 6 0v-1a10 10 0 1 0-4 8",
            ),
        )
    }

    val Filter: ImageVector by lazy {
        strokeIcon("Filter", listOf("M22 3H2l8 9.46V19l4 2v-8.54L22 3z"))
    }

    val WifiOff: ImageVector by lazy {
        strokeIcon(
            "WifiOff",
            listOf(
                "M12 20h.01",
                "M8.5 16.429a5 5 0 0 1 7 0",
                "M5 12.859a10 10 0 0 1 5.17-2.69",
                "M19 12.859a10 10 0 0 0-2.007-1.523",
                "M2 8.82a15 15 0 0 1 4.177-2.643",
                "M22 8.82a15 15 0 0 0-11.288-3.764",
                "m2 2 20 20",
            ),
        )
    }

    val Clock: ImageVector by lazy {
        strokeIcon(
            "Clock",
            listOf(
                "M2.000,12.000 a10.000,10.000 0 1,0 20.000,0 a10.000,10.000 0 1,0 -20.000,0",
                "M",
            ),
        )
    }

    val Folder: ImageVector by lazy {
        strokeIcon("Folder", listOf("M20 20a2 2 0 0 0 2-2V8a2 2 0 0 0-2-2h-7.9a2 2 0 0 1-1.69-.9L9.6 3.9A2 2 0 0 0 7.93 3H4a2 2 0 0 0-2 2v13a2 2 0 0 0 2 2Z"))
    }

    val FolderPlus: ImageVector by lazy {
        strokeIcon(
            "FolderPlus",
            listOf(
                "M12 10v6",
                "M9 13h6",
                "M20 20a2 2 0 0 0 2-2V8a2 2 0 0 0-2-2h-7.9a2 2 0 0 1-1.69-.9L9.6 3.9A2 2 0 0 0 7.93 3H4a2 2 0 0 0-2 2v13a2 2 0 0 0 2 2Z",
            ),
        )
    }

    val QrCode: ImageVector by lazy {
        strokeIcon(
            "QrCode",
            listOf(
                "M4.000,3.000 h3.000 a1.000,1.000 0 0 1 1.000,1.000 v3.000 a1.000,1.000 0 0 1 -1.000,1.000 h-3.000 a1.000,1.000 0 0 1 -1.000,-1.000 v-3.000 a1.000,1.000 0 0 1 1.000,-1.000 z",
                "M17.000,3.000 h3.000 a1.000,1.000 0 0 1 1.000,1.000 v3.000 a1.000,1.000 0 0 1 -1.000,1.000 h-3.000 a1.000,1.000 0 0 1 -1.000,-1.000 v-3.000 a1.000,1.000 0 0 1 1.000,-1.000 z",
                "M4.000,16.000 h3.000 a1.000,1.000 0 0 1 1.000,1.000 v3.000 a1.000,1.000 0 0 1 -1.000,1.000 h-3.000 a1.000,1.000 0 0 1 -1.000,-1.000 v-3.000 a1.000,1.000 0 0 1 1.000,-1.000 z",
                "M21 16h-3a2 2 0 0 0-2 2v3",
                "M21 21v.01",
                "M12 7v3a2 2 0 0 1-2 2H7",
                "M3 12h.01",
                "M12 3h.01",
                "M12 16v.01",
                "M16 12h1",
                "M21 12v.01",
                "M12 21v-1",
            ),
        )
    }

    val Smartphone: ImageVector by lazy {
        strokeIcon(
            "Smartphone",
            listOf(
                "M7.000,2.000 h10.000 a2.000,2.000 0 0 1 2.000,2.000 v16.000 a2.000,2.000 0 0 1 -2.000,2.000 h-10.000 a2.000,2.000 0 0 1 -2.000,-2.000 v-16.000 a2.000,2.000 0 0 1 2.000,-2.000 z",
                "M12 18h.01",
            ),
        )
    }

    val Fingerprint: ImageVector by lazy {
        strokeIcon(
            "Fingerprint",
            listOf(
                "M12 10a2 2 0 0 0-2 2c0 1.02-.1 2.51-.26 4",
                "M14 13.12c0 2.38 0 6.38-1 8.88",
                "M17.29 21.02c.12-.6.43-2.3.5-3.02",
                "M2 12a10 10 0 0 1 18-6",
                "M2 16h.01",
                "M21.8 16c.2-2 .131-5.354 0-6",
                "M5 19.5C5.5 18 6 15 6 12a6 6 0 0 1 .34-2",
                "M8.65 22c.21-.66.45-1.32.57-2",
                "M9 6.8a6 6 0 0 1 9 5.2v2",
            ),
        )
    }

    val Palette: ImageVector by lazy {
        strokeIcon(
            "Palette",
            listOf(
                "M13.000,6.500 a0.500,0.500 0 1,0 1.000,0 a0.500,0.500 0 1,0 -1.000,0",
                "M17.000,10.500 a0.500,0.500 0 1,0 1.000,0 a0.500,0.500 0 1,0 -1.000,0",
                "M8.000,7.500 a0.500,0.500 0 1,0 1.000,0 a0.500,0.500 0 1,0 -1.000,0",
                "M6.000,12.500 a0.500,0.500 0 1,0 1.000,0 a0.500,0.500 0 1,0 -1.000,0",
                "M12 2C6.5 2 2 6.5 2 12s4.5 10 10 10c.926 0 1.648-.746 1.648-1.688 0-.437-.18-.835-.437-1.125-.29-.289-.438-.652-.438-1.125a1.64 1.64 0 0 1 1.668-1.668h1.996c3.051 0 5.555-2.503 5.555-5.554C21.965 6.012 17.461 2 12 2z",
            ),
        )
    }

    val SlidersHorizontal: ImageVector by lazy {
        strokeIcon(
            "SlidersHorizontal",
            listOf(
                "M21,4 L14,4",
                "M10,4 L3,4",
                "M21,12 L12,12",
                "M8,12 L3,12",
                "M21,20 L16,20",
                "M12,20 L3,20",
                "M14,2 L14,6",
                "M8,10 L8,14",
                "M16,18 L16,22",
            ),
        )
    }

    val ExternalLink: ImageVector by lazy {
        strokeIcon(
            "ExternalLink",
            listOf(
                "M15 3h6v6",
                "M10 14 21 3",
                "M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6",
            ),
        )
    }

    val Link: ImageVector by lazy {
        strokeIcon(
            "Link",
            listOf(
                "M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71",
                "M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71",
            ),
        )
    }

    val BadgeCheck: ImageVector by lazy {
        strokeIcon(
            "BadgeCheck",
            listOf(
                "M3.85 8.62a4 4 0 0 1 4.78-4.77 4 4 0 0 1 6.74 0 4 4 0 0 1 4.78 4.78 4 4 0 0 1 0 6.74 4 4 0 0 1-4.77 4.78 4 4 0 0 1-6.75 0 4 4 0 0 1-4.78-4.77 4 4 0 0 1 0-6.76Z",
                "m9 12 2 2 4-4",
            ),
        )
    }

    val Ban: ImageVector by lazy {
        strokeIcon(
            "Ban",
            listOf(
                "M2.000,12.000 a10.000,10.000 0 1,0 20.000,0 a10.000,10.000 0 1,0 -20.000,0",
                "m4.9 4.9 14.2 14.2",
            ),
        )
    }

    val Flag: ImageVector by lazy {
        strokeIcon(
            "Flag",
            listOf(
                "M4 15s1-1 4-1 5 2 8 2 4-1 4-1V3s-1 1-4 1-5-2-8-2-4 1-4 1z",
                "M4,22 L4,15",
            ),
        )
    }

    val Volume2: ImageVector by lazy {
        strokeIcon(
            "Volume2",
            listOf(
                "M11 4.702a.705.705 0 0 0-1.203-.498L6.413 7.587A1.4 1.4 0 0 1 5.416 8H3a1 1 0 0 0-1 1v6a1 1 0 0 0 1 1h2.416a1.4 1.4 0 0 1 .997.413l3.383 3.384A.705.705 0 0 0 11 19.298z",
                "M16 9a5 5 0 0 1 0 6",
                "M19.364 18.364a9 9 0 0 0 0-12.728",
            ),
        )
    }

    val VolumeX: ImageVector by lazy {
        strokeIcon(
            "VolumeX",
            listOf(
                "M11 4.702a.705.705 0 0 0-1.203-.498L6.413 7.587A1.4 1.4 0 0 1 5.416 8H3a1 1 0 0 0-1 1v6a1 1 0 0 0 1 1h2.416a1.4 1.4 0 0 1 .997.413l3.383 3.384A.705.705 0 0 0 11 19.298z",
                "M22,9 L16,15",
                "M16,9 L22,15",
            ),
        )
    }

    val Headphones: ImageVector by lazy {
        strokeIcon("Headphones", listOf("M3 14h3a2 2 0 0 1 2 2v3a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-7a9 9 0 0 1 18 0v7a2 2 0 0 1-2 2h-1a2 2 0 0 1-2-2v-3a2 2 0 0 1 2-2h3"))
    }

    val GripVertical: ImageVector by lazy {
        strokeIcon(
            "GripVertical",
            listOf(
                "M8.000,12.000 a1.000,1.000 0 1,0 2.000,0 a1.000,1.000 0 1,0 -2.000,0",
                "M8.000,5.000 a1.000,1.000 0 1,0 2.000,0 a1.000,1.000 0 1,0 -2.000,0",
                "M8.000,19.000 a1.000,1.000 0 1,0 2.000,0 a1.000,1.000 0 1,0 -2.000,0",
                "M14.000,12.000 a1.000,1.000 0 1,0 2.000,0 a1.000,1.000 0 1,0 -2.000,0",
                "M14.000,5.000 a1.000,1.000 0 1,0 2.000,0 a1.000,1.000 0 1,0 -2.000,0",
                "M14.000,19.000 a1.000,1.000 0 1,0 2.000,0 a1.000,1.000 0 1,0 -2.000,0",
            ),
        )
    }

    val KeyRound: ImageVector by lazy {
        strokeIcon(
            "KeyRound",
            listOf(
                "M2.586 17.414A2 2 0 0 0 2 18.828V21a1 1 0 0 0 1 1h3a1 1 0 0 0 1-1v-1a1 1 0 0 1 1-1h1a1 1 0 0 0 1-1v-1a1 1 0 0 1 1-1h.172a2 2 0 0 0 1.414-.586l.814-.814a6.5 6.5 0 1 0-4-4z",
                "M16.000,7.500 a0.500,0.500 0 1,0 1.000,0 a0.500,0.500 0 1,0 -1.000,0",
            ),
        )
    }

    val Languages: ImageVector by lazy {
        strokeIcon(
            "Languages",
            listOf(
                "m5 8 6 6",
                "m4 14 6-6 2-3",
                "M2 5h12",
                "M7 2h1",
                "m22 22-5-10-5 10",
                "M14 18h6",
            ),
        )
    }

    val HardDrive: ImageVector by lazy {
        strokeIcon(
            "HardDrive",
            listOf(
                "M22,12 L2,12",
                "M5.45 5.11 2 12v6a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2v-6l-3.45-6.89A2 2 0 0 0 16.76 4H7.24a2 2 0 0 0-1.79 1.11z",
                "M6,16 L6.01,16",
                "M10,16 L10.01,16",
            ),
        )
    }

    val Cloud: ImageVector by lazy {
        strokeIcon("Cloud", listOf("M17.5 19H9a7 7 0 1 1 6.71-9h1.79a4.5 4.5 0 1 1 0 9Z"))
    }

    val Bug: ImageVector by lazy {
        strokeIcon(
            "Bug",
            listOf(
                "m8 2 1.88 1.88",
                "M14.12 3.88 16 2",
                "M9 7.13v-1a3.003 3.003 0 1 1 6 0v1",
                "M12 20c-3.3 0-6-2.7-6-6v-3a4 4 0 0 1 4-4h4a4 4 0 0 1 4 4v3c0 3.3-2.7 6-6 6",
                "M12 20v-9",
                "M6.53 9C4.6 8.8 3 7.1 3 5",
                "M6 13H2",
                "M3 21c0-2.1 1.7-3.9 3.8-4",
                "M20.97 5c0 2.1-1.6 3.8-3.5 4",
                "M22 13h-4",
                "M17.2 17c2.1.1 3.8 1.9 3.8 4",
            ),
        )
    }

    val List: ImageVector by lazy {
        strokeIcon(
            "List",
            listOf(
                "M3 12h.01",
                "M3 18h.01",
                "M3 6h.01",
                "M8 12h13",
                "M8 18h13",
                "M8 6h13",
            ),
        )
    }

    val BookOpen: ImageVector by lazy {
        strokeIcon(
            "BookOpen",
            listOf(
                "M12 7v14",
                "M3 18a1 1 0 0 1-1-1V4a1 1 0 0 1 1-1h5a4 4 0 0 1 4 4 4 4 0 0 1 4-4h5a1 1 0 0 1 1 1v13a1 1 0 0 1-1 1h-6a3 3 0 0 0-3 3 3 3 0 0 0-3-3z",
            ),
        )
    }

    val LifeBuoy: ImageVector by lazy {
        strokeIcon(
            "LifeBuoy",
            listOf(
                "M2.000,12.000 a10.000,10.000 0 1,0 20.000,0 a10.000,10.000 0 1,0 -20.000,0",
                "m4.93 4.93 4.24 4.24",
                "m14.83 9.17 4.24-4.24",
                "m14.83 14.83 4.24 4.24",
                "m9.17 14.83-4.24 4.24",
                "M8.000,12.000 a4.000,4.000 0 1,0 8.000,0 a4.000,4.000 0 1,0 -8.000,0",
            ),
        )
    }

    val Tag: ImageVector by lazy {
        strokeIcon(
            "Tag",
            listOf(
                "M12.586 2.586A2 2 0 0 0 11.172 2H4a2 2 0 0 0-2 2v7.172a2 2 0 0 0 .586 1.414l8.704 8.704a2.426 2.426 0 0 0 3.42 0l6.58-6.58a2.426 2.426 0 0 0 0-3.42z",
                "M7.000,7.500 a0.500,0.500 0 1,0 1.000,0 a0.500,0.500 0 1,0 -1.000,0",
            ),
        )
    }

    val Zap: ImageVector by lazy {
        strokeIcon("Zap", listOf("M4 14a1 1 0 0 1-.78-1.63l9.9-10.2a.5.5 0 0 1 .86.46l-1.92 6.02A1 1 0 0 0 13 10h7a1 1 0 0 1 .78 1.63l-9.9 10.2a.5.5 0 0 1-.86-.46l1.92-6.02A1 1 0 0 0 11 14z"))
    }

    val CornerUpLeft: ImageVector by lazy {
        strokeIcon(
            "CornerUpLeft",
            listOf(
                "M",
                "M20 20v-7a4 4 0 0 0-4-4H4",
            ),
        )
    }

    val Wifi: ImageVector by lazy {
        strokeIcon(
            "Wifi",
            listOf(
                "M12 20h.01",
                "M2 8.82a15 15 0 0 1 20 0",
                "M5 12.859a10 10 0 0 1 14 0",
                "M8.5 16.429a5 5 0 0 1 7 0",
            ),
        )
    }

    val Database: ImageVector by lazy {
        strokeIcon(
            "Database",
            listOf(
                "M3.000,5.000 a9.000,3.000 0 1,0 18.000,0 a9.000,3.000 0 1,0 -18.000,0",
                "M3 5V19A9 3 0 0 0 21 19V5",
                "M3 12A9 3 0 0 0 21 12",
            ),
        )
    }

    val MoonStar: ImageVector by lazy {
        strokeIcon(
            "MoonStar",
            listOf(
                "M12 3a6 6 0 0 0 9 9 9 9 0 1 1-9-9",
                "M20 3v4",
                "M22 5h-4",
            ),
        )
    }
}

private fun strokeIcon(name: String, pathData: List<String>): ImageVector {
    val builder = ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    )
    pathData.forEachIndexed { index, d ->
        builder.addPath(
            pathData = PathParser().parsePathString(d).toNodes(),
            name = "$name-$index",
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeAlpha = 1f,
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        )
    }
    return builder.build()
}