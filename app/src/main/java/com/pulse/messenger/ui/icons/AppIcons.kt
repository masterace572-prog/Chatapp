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
 * Icon path data source: Lucide (ISC licence, https://lucide.dev).
 */
object AppIcons {
    val ArrowLeft: ImageVector by lazy {
        strokeIcon("ArrowLeft", "m12 19-7-7 7-7")
    }
    val ArrowRight: ImageVector by lazy {
        strokeIcon("ArrowRight", "M5 12h14")
    }
    val ArrowUp: ImageVector by lazy {
        strokeIcon("ArrowUp", "m5 12 7-7 7 7")
    }
    val ArrowDown: ImageVector by lazy {
        strokeIcon("ArrowDown", "M12 5v14")
    }
    val Search: ImageVector by lazy {
        strokeIcon("Search", "m21 21-4.34-4.34")
    }
    val Plus: ImageVector by lazy {
        strokeIcon("Plus", "M5 12h14")
    }
    val Minus: ImageVector by lazy {
        strokeIcon("Minus", "M5 12h14")
    }
    val Close: ImageVector by lazy {
        strokeIcon("Close", "M18 6 6 18")
    }
    val CircleX: ImageVector by lazy {
        strokeIcon("CircleX", "m15 9-6 6")
    }
    val Check: ImageVector by lazy {
        strokeIcon("Check", "M20 6 9 17l-5-5")
    }
    val CheckCheck: ImageVector by lazy {
        strokeIcon("CheckCheck", "M18 6 7 17l-5-5")
    }
    val ChevronDown: ImageVector by lazy {
        strokeIcon("ChevronDown", "m6 9 6 6 6-6")
    }
    val ChevronLeft: ImageVector by lazy {
        strokeIcon("ChevronLeft", "m15 18-6-6 6-6")
    }
    val ChevronRight: ImageVector by lazy {
        strokeIcon("ChevronRight", "m9 18 6-6-6-6")
    }
    val ChevronUp: ImageVector by lazy {
        strokeIcon("ChevronUp", "m18 15-6-6-6 6")
    }
    val Settings: ImageVector by lazy {
        strokeIcon("Settings", "M9.671 4.136a2.34 2.34 0 0 1 4.659 0 2.34 2.34 0 0 0 3.319 1.915 2.34 2.34 0 0 1 2.33 4.033 2.34 2.34 0 0 0 0 3.831 2.34 2.34 0 0 1-2.33 4.033 2.34 2.34 0 0 0-3.319 1.915 2.34 2.34 0 0 1-4.659 0 2.34 2.34 0 0 0-3.32-1.915 2.34 2.34 0 0 1-2.33-4.033 2.34 2.34 0 0 0 0-3.831A2.34 2.34 0 0 1 6.35 6.051a2.34 2.34 0 0 0 3.319-1.915")
    }
    val MessageCircle: ImageVector by lazy {
        strokeIcon("MessageCircle", "M2.992 16.342a2 2 0 0 1 .094 1.167l-1.065 3.29a1 1 0 0 0 1.236 1.168l3.413-.998a2 2 0 0 1 1.099.092 10 10 0 1 0-4.777-4.719")
    }
    val MessageSquare: ImageVector by lazy {
        strokeIcon("MessageSquare", "M22 17a2 2 0 0 1-2 2H6.828a2 2 0 0 0-1.414.586l-2.202 2.202A.71.71 0 0 1 2 21.286V5a2 2 0 0 1 2-2h16a2 2 0 0 1 2 2z")
    }
    val Phone: ImageVector by lazy {
        strokeIcon("Phone", "M13.832 16.568a1 1 0 0 0 1.213-.303l.355-.465A2 2 0 0 1 17 15h3a2 2 0 0 1 2 2v3a2 2 0 0 1-2 2A18 18 0 0 1 2 4a2 2 0 0 1 2-2h3a2 2 0 0 1 2 2v3a2 2 0 0 1-.8 1.6l-.468.351a1 1 0 0 0-.292 1.233 14 14 0 0 0 6.392 6.384")
    }
    val PhoneOff: ImageVector by lazy {
        strokeIcon("PhoneOff", "M10.1 13.9a14 14 0 0 0 3.732 2.668 1 1 0 0 0 1.213-.303l.355-.465A2 2 0 0 1 17 15h3a2 2 0 0 1 2 2v3a2 2 0 0 1-2 2 18 18 0 0 1-12.728-5.272")
    }
    val PhoneMissed: ImageVector by lazy {
        strokeIcon("PhoneMissed", "m16 2 6 6")
    }
    val PhoneIncoming: ImageVector by lazy {
        strokeIcon("PhoneIncoming", "M16 2v6h6")
    }
    val PhoneOutgoing: ImageVector by lazy {
        strokeIcon("PhoneOutgoing", "m16 8 6-6")
    }
    val Video: ImageVector by lazy {
        strokeIcon("Video", "m16 13 5.223 3.482a.5.5 0 0 0 .777-.416V7.87a.5.5 0 0 0-.752-.432L16 10.5")
    }
    val VideoOff: ImageVector by lazy {
        strokeIcon("VideoOff", "M10.66 6H14a2 2 0 0 1 2 2v2.5l5.248-3.062A.5.5 0 0 1 22 7.87v8.196")
    }
    val Users: ImageVector by lazy {
        strokeIcon("Users", "M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2")
    }
    val User: ImageVector by lazy {
        strokeIcon("User", "M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2")
    }
    val UserPlus: ImageVector by lazy {
        strokeIcon("UserPlus", "M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2")
    }
    val UserMinus: ImageVector by lazy {
        strokeIcon("UserMinus", "M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2")
    }
    val UserX: ImageVector by lazy {
        strokeIcon("UserX", "M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2")
    }
    val UserCheck: ImageVector by lazy {
        strokeIcon("UserCheck", "m16 11 2 2 4-4")
    }
    val LogOut: ImageVector by lazy {
        strokeIcon("LogOut", "m16 17 5-5-5-5")
    }
    val Share: ImageVector by lazy {
        strokeIcon("Share", "M12 2v13")
    }
    val Download: ImageVector by lazy {
        strokeIcon("Download", "M12 15V3")
    }
    val Upload: ImageVector by lazy {
        strokeIcon("Upload", "M12 3v12")
    }
    val Save: ImageVector by lazy {
        strokeIcon("Save", "M15.2 3a2 2 0 0 1 1.4.6l3.8 3.8a2 2 0 0 1 .6 1.4V19a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2z")
    }
    val Trash: ImageVector by lazy {
        strokeIcon("Trash", "M10 11v6")
    }
    val Pencil: ImageVector by lazy {
        strokeIcon("Pencil", "M21.174 6.812a1 1 0 0 0-3.986-3.987L3.842 16.174a2 2 0 0 0-.5.83l-1.321 4.352a.5.5 0 0 0 .623.622l4.353-1.32a2 2 0 0 0 .83-.497z")
    }
    val Star: ImageVector by lazy {
        strokeIcon("Star", "M11.525 2.295a.53.53 0 0 1 .95 0l2.31 4.679a2.123 2.123 0 0 0 1.595 1.16l5.166.756a.53.53 0 0 1 .294.904l-3.736 3.638a2.123 2.123 0 0 0-.611 1.878l.882 5.14a.53.53 0 0 1-.771.56l-4.618-2.428a2.122 2.122 0 0 0-1.973 0L6.396 21.01a.53.53 0 0 1-.77-.56l.881-5.139a2.122 2.122 0 0 0-.611-1.879L2.16 9.795a.53.53 0 0 1 .294-.906l5.165-.755a2.122 2.122 0 0 0 1.597-1.16z")
    }
    val Pin: ImageVector by lazy {
        strokeIcon("Pin", "M12 17v5")
    }
    val PinOff: ImageVector by lazy {
        strokeIcon("PinOff", "M12 17v5")
    }
    val Archive: ImageVector by lazy {
        strokeIcon("Archive", "M4 8v11a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8")
    }
    val ArchiveRestore: ImageVector by lazy {
        strokeIcon("ArchiveRestore", "M4 8v11a2 2 0 0 0 2 2h2")
    }
    val Mic: ImageVector by lazy {
        strokeIcon("Mic", "M12 19v3")
    }
    val MicOff: ImageVector by lazy {
        strokeIcon("MicOff", "M12 19v3")
    }
    val Send: ImageVector by lazy {
        strokeIcon("Send", "M14.536 21.686a.5.5 0 0 0 .937-.024l6.5-19a.496.496 0 0 0-.635-.635l-19 6.5a.5.5 0 0 0-.024.937l7.93 3.18a2 2 0 0 1 1.112 1.11z")
    }
    val Camera: ImageVector by lazy {
        strokeIcon("Camera", "M13.997 4a2 2 0 0 1 1.76 1.05l.486.9A2 2 0 0 0 18.003 7H20a2 2 0 0 1 2 2v9a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V9a2 2 0 0 1 2-2h1.997a2 2 0 0 0 1.759-1.048l.489-.904A2 2 0 0 1 10.004 4z")
    }
    val CameraOff: ImageVector by lazy {
        strokeIcon("CameraOff", "M14.564 14.558a3 3 0 1 1-4.122-4.121")
    }
    val Paperclip: ImageVector by lazy {
        strokeIcon("Paperclip", "m16 6-8.414 8.586a2 2 0 0 0 2.829 2.829l8.414-8.586a4 4 0 1 0-5.657-5.657l-8.379 8.551a6 6 0 1 0 8.485 8.485l8.379-8.551")
    }
    val Image: ImageVector by lazy {
        strokeIcon("Image", "m21 15-3.086-3.086a2 2 0 0 0-2.828 0L6 21")
    }
    val Images: ImageVector by lazy {
        strokeIcon("Images", "m22 11-1.296-1.296a2.4 2.4 0 0 0-3.408 0L11 16")
    }
    val Lock: ImageVector by lazy {
        strokeIcon("Lock", "M7 11V7a5 5 0 0 1 10 0v4")
    }
    val LockOpen: ImageVector by lazy {
        strokeIcon("LockOpen", "M7 11V7a5 5 0 0 1 9.9-1")
    }
    val Shield: ImageVector by lazy {
        strokeIcon("Shield", "M20 13c0 5-3.5 7.5-7.66 8.95a1 1 0 0 1-.67-.01C7.5 20.5 4 18 4 13V6a1 1 0 0 1 1-1c2 0 4.5-1.2 6.24-2.72a1.17 1.17 0 0 1 1.52 0C14.51 3.81 17 5 19 5a1 1 0 0 1 1 1z")
    }
    val ShieldCheck: ImageVector by lazy {
        strokeIcon("ShieldCheck", "M20 13c0 5-3.5 7.5-7.66 8.95a1 1 0 0 1-.67-.01C7.5 20.5 4 18 4 13V6a1 1 0 0 1 1-1c2 0 4.5-1.2 6.24-2.72a1.17 1.17 0 0 1 1.52 0C14.51 3.81 17 5 19 5a1 1 0 0 1 1 1z")
    }
    val Bell: ImageVector by lazy {
        strokeIcon("Bell", "M10.268 21a2 2 0 0 0 3.464 0")
    }
    val BellOff: ImageVector by lazy {
        strokeIcon("BellOff", "M10.268 21a2 2 0 0 0 3.464 0")
    }
    val BellRing: ImageVector by lazy {
        strokeIcon("BellRing", "M10.268 21a2 2 0 0 0 3.464 0")
    }
    val Moon: ImageVector by lazy {
        strokeIcon("Moon", "M20.985 12.486a9 9 0 1 1-9.473-9.472c.405-.022.617.46.402.803a6 6 0 0 0 8.268 8.268c.344-.215.825-.004.803.401")
    }
    val Sun: ImageVector by lazy {
        strokeIcon("Sun", "M12 2v2")
    }
    val Globe: ImageVector by lazy {
        strokeIcon("Globe", "M12 2a14.5 14.5 0 0 0 0 20 14.5 14.5 0 0 0 0-20")
    }
    val Info: ImageVector by lazy {
        strokeIcon("Info", "M12 16v-4")
    }
    val AlertTriangle: ImageVector by lazy {
        strokeIcon("AlertTriangle", "m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3")
    }
    val Mail: ImageVector by lazy {
        strokeIcon("Mail", "m22 7-8.991 5.727a2 2 0 0 1-2.009 0L2 7")
    }
    val Eye: ImageVector by lazy {
        strokeIcon("Eye", "M2.062 12.348a1 1 0 0 1 0-.696 10.75 10.75 0 0 1 19.876 0 1 1 0 0 1 0 .696 10.75 10.75 0 0 1-19.876 0")
    }
    val EyeOff: ImageVector by lazy {
        strokeIcon("EyeOff", "M10.733 5.076a10.744 10.744 0 0 1 11.205 6.575 1 1 0 0 1 0 .696 10.747 10.747 0 0 1-1.444 2.49")
    }
    val Calendar: ImageVector by lazy {
        strokeIcon("Calendar", "M8 2v3")
    }
    val CalendarDays: ImageVector by lazy {
        strokeIcon("CalendarDays", "M8 2v3")
    }
    val Copy: ImageVector by lazy {
        strokeIcon("Copy", "M4 16c-1.1 0-2-.9-2-2V4c0-1.1.9-2 2-2h10c1.1 0 2 .9 2 2")
    }
    val RotateCw: ImageVector by lazy {
        strokeIcon("RotateCw", "M21 12a9 9 0 1 1-9-9c2.52 0 4.93 1 6.74 2.74L21 8")
    }
    val MapPin: ImageVector by lazy {
        strokeIcon("MapPin", "M20 10c0 4.993-5.539 10.193-7.399 11.799a1 1 0 0 1-1.202 0C9.539 20.193 4 14.993 4 10a8 8 0 0 1 16 0")
    }
    val File: ImageVector by lazy {
        strokeIcon("File", "M6 22a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h8a2.4 2.4 0 0 1 1.704.706l3.588 3.588A2.4 2.4 0 0 1 20 8v12a2 2 0 0 1-2 2z")
    }
    val FileText: ImageVector by lazy {
        strokeIcon("FileText", "M6 22a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h8a2.4 2.4 0 0 1 1.704.706l3.588 3.588A2.4 2.4 0 0 1 20 8v12a2 2 0 0 1-2 2z")
    }
    val Music: ImageVector by lazy {
        strokeIcon("Music", "M9 18V5l12-2v13")
    }
    val Play: ImageVector by lazy {
        strokeIcon("Play", "M5 5a2 2 0 0 1 3.008-1.728l11.997 6.998a2 2 0 0 1 .003 3.458l-12 7A2 2 0 0 1 5 19z")
    }
    val AudioLines: ImageVector by lazy {
        strokeIcon("AudioLines", "M2 10v3")
    }
    val AtSign: ImageVector by lazy {
        strokeIcon("AtSign", "M16 8v5a3 3 0 0 0 6 0v-1a10 10 0 1 0-4 8")
    }
    val WifiOff: ImageVector by lazy {
        strokeIcon("WifiOff", "M12 20h.01")
    }
    val Clock: ImageVector by lazy {
        strokeIcon("Clock", "M12 6v6l4 2")
    }
    val Folder: ImageVector by lazy {
        strokeIcon("Folder", "M20 20a2 2 0 0 0 2-2V8a2 2 0 0 0-2-2h-7.9a2 2 0 0 1-1.69-.9L9.6 3.9A2 2 0 0 0 7.93 3H4a2 2 0 0 0-2 2v13a2 2 0 0 0 2 2Z")
    }
    val FolderPlus: ImageVector by lazy {
        strokeIcon("FolderPlus", "M12 10v6")
    }
    val QrCode: ImageVector by lazy {
        strokeIcon("QrCode", "M21 16h-3a2 2 0 0 0-2 2v3")
    }
    val Smartphone: ImageVector by lazy {
        strokeIcon("Smartphone", "M12 18h.01")
    }
    val Palette: ImageVector by lazy {
        strokeIcon("Palette", "M12 22a1 1 0 0 1 0-20 10 9 0 0 1 10 9 5 5 0 0 1-5 5h-2.25a1.75 1.75 0 0 0-1.4 2.8l.3.4a1.75 1.75 0 0 1-1.4 2.8z")
    }
    val SlidersHorizontal: ImageVector by lazy {
        strokeIcon("SlidersHorizontal", "M10 5H3")
    }
    val ExternalLink: ImageVector by lazy {
        strokeIcon("ExternalLink", "M15 3h6v6")
    }
    val Link: ImageVector by lazy {
        strokeIcon("Link", "M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71")
    }
    val BadgeCheck: ImageVector by lazy {
        strokeIcon("BadgeCheck", "M3.85 8.62a4 4 0 0 1 4.78-4.77 4 4 0 0 1 6.74 0 4 4 0 0 1 4.78 4.78 4 4 0 0 1 0 6.74 4 4 0 0 1-4.77 4.78 4 4 0 0 1-6.75 0 4 4 0 0 1-4.78-4.77 4 4 0 0 1 0-6.76Z")
    }
    val Ban: ImageVector by lazy {
        strokeIcon("Ban", "M4.929 4.929 19.07 19.071")
    }
    val Flag: ImageVector by lazy {
        strokeIcon("Flag", "M4 22V4a1 1 0 0 1 .4-.8A6 6 0 0 1 8 2c3 0 5 2 7.333 2q2 0 3.067-.8A1 1 0 0 1 20 4v10a1 1 0 0 1-.4.8A6 6 0 0 1 16 16c-3 0-5-2-8-2a6 6 0 0 0-4 1.528")
    }
    val Volume2: ImageVector by lazy {
        strokeIcon("Volume2", "M11 4.702a.705.705 0 0 0-1.203-.498L6.413 7.587A1.4 1.4 0 0 1 5.416 8H3a1 1 0 0 0-1 1v6a1 1 0 0 0 1 1h2.416a1.4 1.4 0 0 1 .997.413l3.383 3.384A.705.705 0 0 0 11 19.298z")
    }
    val VolumeX: ImageVector by lazy {
        strokeIcon("VolumeX", "M11 4.702a.7.7 0 0 0-1.203-.498L6.413 7.587A1.4 1.4 0 0 1 5.416 8H3a1 1 0 0 0-1 1v6a1 1 0 0 0 1 1h2.416a1.4 1.4 0 0 1 .997.413l3.383 3.384A.7.7 0 0 0 11 19.298z")
    }
    val Headphones: ImageVector by lazy {
        strokeIcon("Headphones", "M3 14h3a2 2 0 0 1 2 2v3a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-7a9 9 0 0 1 18 0v7a2 2 0 0 1-2 2h-1a2 2 0 0 1-2-2v-3a2 2 0 0 1 2-2h3")
    }
    val KeyRound: ImageVector by lazy {
        strokeIcon("KeyRound", "M2.586 17.414A2 2 0 0 0 2 18.828V21a1 1 0 0 0 1 1h3a1 1 0 0 0 1-1v-1a1 1 0 0 1 1-1h1a1 1 0 0 0 1-1v-1a1 1 0 0 1 1-1h.172a2 2 0 0 0 1.414-.586l.814-.814a6.5 6.5 0 1 0-4-4z")
    }
    val Languages: ImageVector by lazy {
        strokeIcon("Languages", "m5 8 6 6")
    }
    val HardDrive: ImageVector by lazy {
        strokeIcon("HardDrive", "M10 16h.01")
    }
    val Cloud: ImageVector by lazy {
        strokeIcon("Cloud", "M17.5 19H9a7 7 0 1 1 6.71-9h1.79a4.5 4.5 0 1 1 0 9Z")
    }
    val Bug: ImageVector by lazy {
        strokeIcon("Bug", "M12 20v-9")
    }
    val List: ImageVector by lazy {
        strokeIcon("List", "M3 5h.01")
    }
    val BookOpen: ImageVector by lazy {
        strokeIcon("BookOpen", "M12 5v16")
    }
    val LifeBuoy: ImageVector by lazy {
        strokeIcon("LifeBuoy", "m4.93 4.93 4.24 4.24")
    }
    val Tag: ImageVector by lazy {
        strokeIcon("Tag", "M12.586 2.586A2 2 0 0 0 11.172 2H4a2 2 0 0 0-2 2v7.172a2 2 0 0 0 .586 1.414l8.704 8.704a2.426 2.426 0 0 0 3.42 0l6.58-6.58a2.426 2.426 0 0 0 0-3.42z")
    }
    val Zap: ImageVector by lazy {
        strokeIcon("Zap", "M15.914 4a1.5 1.5 0 00-2.474-1.561l-9 9A1.5 1.5 0 005.5 14h4.002a.5.5 0 01.471.666L8.086 20a1.5 1.5 0 002.475 1.56l9-9A1.5 1.5 0 0018.5 10h-3.997a.5.5 0 01-.472-.667z")
    }
    val CornerUpLeft: ImageVector by lazy {
        strokeIcon("CornerUpLeft", "M20 20v-7a4 4 0 0 0-4-4H4")
    }
    val Wifi: ImageVector by lazy {
        strokeIcon("Wifi", "M12 20h.01")
    }
    val Database: ImageVector by lazy {
        strokeIcon("Database", "M3 5V19A9 3 0 0 0 21 19V5")
    }
    val MoonStar: ImageVector by lazy {
        strokeIcon("MoonStar", "M18 5h4")
    }
    val EllipsisVertical: ImageVector by lazy {
        strokeIcon("EllipsisVertical", "M11.000,12.000 a1.000,1.000 0 1,0 2.000,0 a1.000,1.000 0 1,0 -2.000,0 M11.000,5.000 a1.000,1.000 0 1,0 2.000,0 a1.000,1.000 0 1,0 -2.000,0 M11.000,19.000 a1.000,1.000 0 1,0 2.000,0 a1.000,1.000 0 1,0 -2.000,0")
    }
    val Smile: ImageVector by lazy {
        strokeIcon("Smile", "M2.000,12.000 a10.000,10.000 0 1,0 20.000,0 a10.000,10.000 0 1,0 -20.000,0 M8 14s1.5 2 4 2 4-2 4-2 M9,9 L9.01,9 M15,9 L15.01,9")
    }
    val FileAudio: ImageVector by lazy {
        strokeIcon("FileAudio", "M17.5 22h.5a2 2 0 0 0 2-2V7l-5-5H6a2 2 0 0 0-2 2v3 M14 2v4a2 2 0 0 0 2 2h4 M2 19a2 2 0 1 1 4 0v1a2 2 0 1 1-4 0v-4a6 6 0 0 1 12 0v4a2 2 0 1 1-4 0v-1a2 2 0 1 1 4 0")
    }
    val Pause: ImageVector by lazy {
        strokeIcon("Pause", "M15.0,4.0 h2.0 a1.0,1.0 0 0 1 1.0,1.0 v14.0 a1.0,1.0 0 0 1 -1.0,1.0 h-2.0 a1.0,1.0 0 0 1 -1.0,-1.0 v-14.0 a1.0,1.0 0 0 1 1.0,-1.0 z M7.0,4.0 h2.0 a1.0,1.0 0 0 1 1.0,1.0 v14.0 a1.0,1.0 0 0 1 -1.0,1.0 h-2.0 a1.0,1.0 0 0 1 -1.0,-1.0 v-14.0 a1.0,1.0 0 0 1 1.0,-1.0 z")
    }
    val Fingerprint: ImageVector by lazy {
        strokeIcon("Fingerprint", "M12 10a2 2 0 0 0-2 2c0 1.02-.1 2.51-.26 4 M14 13.12c0 2.38 0 6.38-1 8.88 M17.29 21.02c.12-.6.43-2.3.5-3.02 M2 12a10 10 0 0 1 18-6 M2 16h.01 M21.8 16c.2-2 .131-5.354 0-6 M5 19.5C5.5 18 6 15 6 12a6 6 0 0 1 .34-2 M8.65 22c.21-.66.45-1.32.57-2 M9 6.8a6 6 0 0 1 9 5.2v2")
    }
    val GripVertical: ImageVector by lazy {
        strokeIcon("GripVertical", "M8.000,12.000 a1.000,1.000 0 1,0 2.000,0 a1.000,1.000 0 1,0 -2.000,0 M8.000,5.000 a1.000,1.000 0 1,0 2.000,0 a1.000,1.000 0 1,0 -2.000,0 M8.000,19.000 a1.000,1.000 0 1,0 2.000,0 a1.000,1.000 0 1,0 -2.000,0 M14.000,12.000 a1.000,1.000 0 1,0 2.000,0 a1.000,1.000 0 1,0 -2.000,0 M14.000,5.000 a1.000,1.000 0 1,0 2.000,0 a1.000,1.000 0 1,0 -2.000,0 M14.000,19.000 a1.000,1.000 0 1,0 2.000,0 a1.000,1.000 0 1,0 -2.000,0")
    }
    val EllipsisHorizontal: ImageVector by lazy {
        strokeIcon("EllipsisHorizontal", "M11.000,12.000 a1.000,1.000 0 1,0 2.000,0 a1.000,1.000 0 1,0 -2.000,0 M18.000,12.000 a1.000,1.000 0 1,0 2.000,0 a1.000,1.000 0 1,0 -2.000,0 M4.000,12.000 a1.000,1.000 0 1,0 2.000,0 a1.000,1.000 0 1,0 -2.000,0")
    }
    val HelpCircle: ImageVector by lazy {
        strokeIcon("HelpCircle", "M2.000,12.000 a10.000,10.000 0 1,0 20.000,0 a10.000,10.000 0 1,0 -20.000,0 M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3 M12 17h.01")
    }
    val AlertCircle: ImageVector by lazy {
        strokeIcon("AlertCircle", "M2.000,12.000 a10.000,10.000 0 1,0 20.000,0 a10.000,10.000 0 1,0 -20.000,0 M12,8 L12,12 M12,16 L12.01,16")
    }
    val Filter: ImageVector by lazy {
        strokeIcon("Filter", "M22 3H2l8 9.46V19l4 2v-8.54L22 3z")
    }
}

private fun strokeIcon(name: String, pathData: String): ImageVector =
    ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    )
    .addPath(
        pathData = PathParser().parsePathString(pathData).toNodes(),
        name = name,
        fill = null,
        stroke = SolidColor(Color.Black),
        strokeAlpha = 1f,
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
    )
    .build()