package com.pulse.messenger.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pulse.messenger.ui.icons.AppIcons
import com.pulse.messenger.ui.theme.AvatarTones
import com.pulse.messenger.ui.theme.PulseSpacing
import com.pulse.messenger.ui.theme.PulseTheme

/*
 * PRD §7 / §13: every component ships with light AND dark previews.
 * Showcases live next to their components; Studio renders them from the
 * design-token theme (no hardcoded colors/typography in the demos).
 */

/* ---------- shared host ---------- */

@Composable
private fun ShowcaseHost(
    light: Boolean,
    content: @Composable () -> Unit,
) {
    PulseTheme(darkTheme = !light) {
        Surface(color = PulseTheme.colors.background) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) { content() }
        }
    }
}

@Composable
private fun DemoLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = PulseTheme.colors.textTertiary,
    )
}

/* ---------- AppButton / AppIconButton ---------- */

@Composable
private fun ButtonsShowcase() {
    DemoLabel("AppButton")
    AppButton(text = "Continue", onClick = {})
    AppButton(text = "Continue with Google", onClick = {}, variant = AppButtonVariant.Secondary, leadingIcon = AppIcons.Mail)
    AppButton(text = "Log in", onClick = {}, variant = AppButtonVariant.Tertiary)
    AppButton(text = "Delete chat", onClick = {}, variant = AppButtonVariant.Destructive)
    AppButton(text = "Loading", onClick = {}, loading = true)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AppButton(text = "Disabled", onClick = {}, enabled = false, fillMaxWidth = false)
        AppIconButton(icon = AppIcons.Send, contentDescription = "Send", onClick = {}, containerColor = PulseTheme.colors.accent, tint = PulseTheme.colors.onAccent)
        AppIconButton(icon = AppIcons.Phone, contentDescription = "Call", onClick = {}, containerColor = PulseTheme.colors.accentContainer, tint = PulseTheme.colors.accent)
    }
}

@Preview(name = "Buttons · Light", showBackground = true, widthDp = 400)
@Composable
private fun ButtonsLight() = ShowcaseHost(light = true) { ButtonsShowcase() }

@Preview(name = "Buttons · Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, widthDp = 400)
@Composable
private fun ButtonsDark() = ShowcaseHost(light = false) { ButtonsShowcase() }

/* ---------- AppTextField ---------- */

@Composable
private fun TextFieldShowcase() {
    var value by remember { mutableStateOf("") }
    var secret by remember { mutableStateOf("") }
    DemoLabel("AppTextField")
    AppTextField(
        value = value,
        onValueChange = { value = it },
        label = "Email",
        placeholder = "you@example.com",
        leadingIcon = AppIcons.Mail,
        supportingText = "We will never share your email.",
    )
    AppTextField(
        value = "aria@example.com",
        onValueChange = {},
        label = "Email with value",
        leadingIcon = AppIcons.Mail,
    )
    AppTextField(
        value = "not-an-email",
        onValueChange = {},
        label = "Invalid input",
        leadingIcon = AppIcons.Mail,
        isError = true,
        supportingText = "Enter a valid email address.",
    )
    AppTextField(
        value = secret,
        onValueChange = { secret = it },
        label = "Password",
        placeholder = "8+ characters",
        isPassword = true,
    )
    AppTextField(
        value = value,
        onValueChange = { value = it },
        placeholder = "Message",
        trailingIcon = AppIcons.Smile,
    )
}

@Preview(name = "TextFields · Light", showBackground = true, widthDp = 400)
@Composable
private fun TextFieldsLight() = ShowcaseHost(light = true) { TextFieldShowcase() }

@Preview(name = "TextFields · Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, widthDp = 400)
@Composable
private fun TextFieldsDark() = ShowcaseHost(light = false) { TextFieldShowcase() }

/* ---------- AppTopBar ---------- */

@Composable
private fun TopBarsShowcase() {
    DemoLabel("AppTopBar · Large (tab title)")
    AppTopBar(
        title = "Chats",
        style = AppTopBarStyle.Large,
        actions = {
            AppIconButton(icon = AppIcons.Search, contentDescription = "Search", onClick = {})
            AppIconButton(icon = AppIcons.EllipsisVertical, contentDescription = "More", onClick = {})
        },
    )
    DemoLabel("AppTopBar · Medium (screen)")
    AppTopBar(
        title = "Aria Sharma",
        subtitle = "online",
        navigationIcon = { AppBackButton(onBack = {}) },
        actions = {
            AppIconButton(icon = AppIcons.Phone, contentDescription = "Voice call", onClick = {})
            AppIconButton(icon = AppIcons.Video, contentDescription = "Video call", onClick = {})
        },
    )
    DemoLabel("AppTopBar · Divider")
    AppTopBar(title = "Archived", navigationIcon = { AppBackButton(onBack = {}) }, showDivider = true)
}

@Preview(name = "TopBars · Light", showBackground = true, widthDp = 400)
@Composable
private fun TopBarsLight() = ShowcaseHost(light = true) { TopBarsShowcase() }

@Preview(name = "TopBars · Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, widthDp = 400)
@Composable
private fun TopBarsDark() = ShowcaseHost(light = false) { TopBarsShowcase() }

/* ---------- Avatar ---------- */

@Composable
private fun AvatarsShowcase() {
    DemoLabel("Avatar · sizes, initials, online, group")
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Avatar(name = "Aria Sharma", avatarTone = AvatarTones.Clay, size = 52.dp, isOnline = true)
        Avatar(name = "Noah Khan", avatarTone = AvatarTones.Sage, size = 44.dp)
        Avatar(name = "Mira Patel", avatarTone = AvatarTones.Dusk, size = 40.dp, isOnline = true)
        Avatar(name = "Dev Singh", avatarTone = AvatarTones.Steel, size = 36.dp)
        Avatar(name = "Zara Ali", avatarTone = AvatarTones.Plum, size = 32.dp)
        Avatar(name = "", size = 28.dp)
    }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Avatar(name = null, isGroup = true, size = 52.dp)
        Avatar(
            name = null,
            isGroup = true,
            groupMemberNames = listOf("Aria Sharma", "Mira Patel"),
            size = 52.dp,
        )
        Avatar(
            name = "Aria Sharma",
            avatarTone = AvatarTones.Slate,
            size = 72.dp,
            isOnline = true,
        )
    }
}

@Preview(name = "Avatars · Light", showBackground = true, widthDp = 400)
@Composable
private fun AvatarsLight() = ShowcaseHost(light = true) { AvatarsShowcase() }

@Preview(name = "Avatars · Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, widthDp = 400)
@Composable
private fun AvatarsDark() = ShowcaseHost(light = false) { AvatarsShowcase() }

/* ---------- AppCard / AppDivider ---------- */

@Composable
private fun CardsShowcase() {
    DemoLabel("AppCard")
    AppCard(contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)) {
        Text("Surface card", style = MaterialTheme.typography.titleMedium, color = PulseTheme.colors.textPrimary)
        Spacer(Modifier.height(4.dp))
        Text("Cards hold grouped content with a calm, flat surface.", style = MaterialTheme.typography.bodyMedium, color = PulseTheme.colors.textSecondary)
    }
    DemoLabel("AppCard · bordered + inset dividers")
    AppCard(borderVisible = true, contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text("Bordered card", style = MaterialTheme.typography.titleMedium, color = PulseTheme.colors.textPrimary)
        }
        AppDivider(insetStart = 16.dp)
        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text("Divided section", style = MaterialTheme.typography.bodyMedium, color = PulseTheme.colors.textSecondary)
        }
    }
}

@Preview(name = "Cards · Light", showBackground = true, widthDp = 400)
@Composable
private fun CardsLight() = ShowcaseHost(light = true) { CardsShowcase() }

@Preview(name = "Cards · Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, widthDp = 400)
@Composable
private fun CardsDark() = ShowcaseHost(light = false) { CardsShowcase() }

/* ---------- AppChip / Badge / Tag ---------- */

@Composable
private fun ChipsShowcase() {
    var selected by remember { mutableStateOf(0) }
    DemoLabel("AppChip · filter row")
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("All", "Unread", "Groups", "Personal").forEachIndexed { i, label ->
            AppChip(label = label, selected = selected == i, onClick = { selected = i })
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AppChip("New group", selected = false, onClick = {}, leadingIcon = AppIcons.Users)
        AppChip("Starred", selected = true, onClick = {}, leadingIcon = AppIcons.Star)
    }
    DemoLabel("Badge · Tag")
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Badge(count = 3)
        Badge(count = 128)
        Badge(count = 2, muted = true)
        Tag("Admin")
        Tag("You", containerColor = PulseTheme.colors.accentContainer, contentColor = PulseTheme.colors.onAccentContainer)
    }
}

@Preview(name = "Chips & Badges · Light", showBackground = true, widthDp = 400)
@Composable
private fun ChipsLight() = ShowcaseHost(light = true) { ChipsShowcase() }

@Preview(name = "Chips & Badges · Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, widthDp = 400)
@Composable
private fun ChipsDark() = ShowcaseHost(light = false) { ChipsShowcase() }

/* ---------- SearchBar ---------- */

@Composable
private fun SearchShowcase() {
    var query by remember { mutableStateOf("") }
    DemoLabel("SearchBar · collapsed")
    SearchBar(value = query, onValueChange = { query = it }, active = false, onActiveChange = {})
    DemoLabel("SearchBar · active with query")
    SearchBar(value = "design", onValueChange = { query = it }, active = true, onActiveChange = {})
}

@Preview(name = "SearchBar · Light", showBackground = true, widthDp = 400)
@Composable
private fun SearchLight() = ShowcaseHost(light = true) { SearchShowcase() }

@Preview(name = "SearchBar · Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, widthDp = 400)
@Composable
private fun SearchDark() = ShowcaseHost(light = false) { SearchShowcase() }

/* ---------- SectionHeader + Settings rows ---------- */

@Composable
private fun SettingsShowcase() {
    DemoLabel("SectionHeader")
    SectionHeader(title = "PREFERENCES", actionLabel = "Edit", onAction = {})
    SettingsItem(icon = AppIcons.Bell, title = "Notifications", subtitle = "Messages, groups, calls", value = "On", onClick = {})
    SettingsSwitchItem(
        icon = AppIcons.Check,
        title = "Read receipts",
        subtitle = "Let people know when you read their messages",
        checked = true,
        onCheckedChange = {},
    )
    SettingsSwitchItem(icon = AppIcons.Lock, title = "Screen lock", checked = false, onCheckedChange = {})
    SettingsItem(icon = AppIcons.Trash, title = "Log out", onClick = {}, destructive = true)
}

@Preview(name = "Settings rows · Light", showBackground = true, widthDp = 400)
@Composable
private fun SettingsLight() = ShowcaseHost(light = true) { SettingsShowcase() }

@Preview(name = "Settings rows · Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, widthDp = 400)
@Composable
private fun SettingsDark() = ShowcaseHost(light = false) { SettingsShowcase() }

/* ---------- SegmentedControl / AppSwitch ---------- */

@Composable
private fun ControlsShowcase() {
    var index by remember { mutableStateOf(1) }
    var checked by remember { mutableStateOf(true) }
    DemoLabel("SegmentedControl")
    SegmentedControl(options = listOf("System", "Light", "Dark"), selectedIndex = index, onSelect = { index = it })
    DemoLabel("AppSwitch")
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        AppSwitch(checked = checked, onCheckedChange = { checked = it })
        AppSwitch(checked = false, onCheckedChange = {})
        AppSwitch(checked = true, onCheckedChange = {}, enabled = false)
    }
}

@Preview(name = "Controls · Light", showBackground = true, widthDp = 400)
@Composable
private fun ControlsLight() = ShowcaseHost(light = true) { ControlsShowcase() }

@Preview(name = "Controls · Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, widthDp = 400)
@Composable
private fun ControlsDark() = ShowcaseHost(light = false) { ControlsShowcase() }

/* ---------- EmptyState / ErrorState / SkeletonLoader ---------- */

@Composable
private fun StatesShowcase() {
    EmptyState(
        icon = AppIcons.MessageCircle,
        title = "No conversations yet",
        subtitle = "Every message you send and receive will live here.",
        actionLabel = "Start a chat",
        onAction = {},
    )
    ErrorState(message = "We could not load your chats.", onRetry = {})
    DemoLabel("SkeletonLoader")
    SkeletonLoader(rows = 4)
}

@Preview(name = "States · Light", showBackground = true, widthDp = 400)
@Composable
private fun StatesLight() = ShowcaseHost(light = true) { StatesShowcase() }

@Preview(name = "States · Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, widthDp = 400)
@Composable
private fun StatesDark() = ShowcaseHost(light = false) { StatesShowcase() }

/* ---------- Logo ---------- */

@Composable
private fun LogoShowcase() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            PulseLogoMark(size = 56.dp)
            Spacer(Modifier.height(8.dp))
            Text("Mark", style = MaterialTheme.typography.labelMedium, color = PulseTheme.colors.textTertiary)
        }
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            PulseLogoLockup(markSize = 56.dp)
            Text("Lockup", style = MaterialTheme.typography.labelMedium, color = PulseTheme.colors.textTertiary)
        }
    }
}

@Preview(name = "Logo · Light", showBackground = true, widthDp = 400)
@Composable
private fun LogoLight() = ShowcaseHost(light = true) { LogoShowcase() }

@Preview(name = "Logo · Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, widthDp = 400)
@Composable
private fun LogoDark() = ShowcaseHost(light = false) { LogoShowcase() }

/* ---------- AppDialog / ConfirmDialog ---------- */

@Composable
private fun DialogsShowcase() {
    var confirmOpen by remember { mutableStateOf(true) }
    var deleteOpen by remember { mutableStateOf(true) }
    DemoLabel("AppDialog")
    if (confirmOpen) {
        AppDialog(
            title = "Disappearing messages",
            text = "New messages in this chat will disappear after the selected time.",
            confirmLabel = "Turn on",
            onConfirm = { confirmOpen = false },
            onDismiss = { confirmOpen = false },
            icon = AppIcons.Clock,
        )
    }
    DemoLabel("ConfirmDialog")
    if (deleteOpen) {
        ConfirmDialog(
            title = "Delete chat?",
            text = "This will delete the conversation for you. Messages can not be restored.",
            confirmLabel = "Delete",
            onConfirm = { deleteOpen = false },
            onDismiss = { deleteOpen = false },
        )
    }
}

@Preview(name = "Dialogs · Light", showBackground = true, widthDp = 400)
@Composable
private fun DialogsLight() = ShowcaseHost(light = true) { DialogsShowcase() }

@Preview(name = "Dialogs · Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, widthDp = 400)
@Composable
private fun DialogsDark() = ShowcaseHost(light = false) { DialogsShowcase() }

/* ---------- AppBottomSheet ---------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SheetShowcase() {
    var open by remember { mutableStateOf(true) }
    DemoLabel("AppBottomSheet")
    if (open) {
        AppBottomSheet(
            onDismissRequest = { open = false },
        ) {
            Text(
                text = "New chat",
                style = MaterialTheme.typography.titleLarge,
                color = PulseTheme.colors.textPrimary,
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Avatar(name = "Aria Sharma", avatarTone = AvatarTones.Clay)
                Column {
                    Text("Aria Sharma", style = MaterialTheme.typography.titleMedium, color = PulseTheme.colors.textPrimary)
                    Text("+91 98110 10001", style = MaterialTheme.typography.bodyMedium, color = PulseTheme.colors.textSecondary)
                }
            }
            Spacer(Modifier.height(16.dp))
            AppButton(text = "Start chat", onClick = { open = false })
        }
    }
}

@Preview(name = "BottomSheet · Light", showBackground = true, widthDp = 400)
@Composable
private fun SheetLight() = ShowcaseHost(light = true) { SheetShowcase() }

@Preview(name = "BottomSheet · Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, widthDp = 400)
@Composable
private fun SheetDark() = ShowcaseHost(light = false) { SheetShowcase() }
