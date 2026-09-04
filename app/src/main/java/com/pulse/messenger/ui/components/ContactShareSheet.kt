package com.pulse.messenger.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pulse.messenger.R
import com.pulse.messenger.domain.model.User
import com.pulse.messenger.ui.icons.AppIcons
import com.pulse.messenger.ui.theme.AvatarTones
import com.pulse.messenger.ui.theme.PulseIconSizes
import com.pulse.messenger.ui.theme.PulseShapes
import com.pulse.messenger.ui.theme.PulseSpacing
import com.pulse.messenger.ui.theme.PulseTheme

/* =====================================================================
 * M4c - Contact share bottom sheet. Sources the seeded directory from the
 * ContactsRepository flow (User already carries the ContactCard fields:
 * id/display name/phone/username/avatarSeed - no new projection needed).
 * Rows reuse the People-row pattern (Avatar + name + @username, PRD §7)
 * used by the Find Friends screen; tapping one shares that contact card.
 * ===================================================================== */

@Composable
fun ContactShareSheetContent(
    contacts: List<User>,
    onShare: (User) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = PulseTheme.colors
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.conversation_contact_share_title),
            style = MaterialTheme.typography.titleMedium,
            color = c.textPrimary,
            modifier = Modifier.padding(vertical = PulseSpacing.sm),
        )
        Text(
            text = stringResource(R.string.conversation_contact_share_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = c.textSecondary,
            modifier = Modifier.padding(bottom = PulseSpacing.sm),
        )
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(PulseSpacing.xs),
            modifier = Modifier.fillMaxWidth(),
        ) {
            items(contacts, key = { it.id }) { user ->
                ContactShareRow(user = user, onShare = { onShare(user) })
            }
        }
    }
}

@Composable
private fun ContactShareRow(user: User, onShare: () -> Unit) {
    val c = PulseTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(PulseShapes.md)
            .clickable(role = Role.Button, onClick = onShare)
            .padding(vertical = PulseSpacing.sm, horizontal = PulseSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Avatar(
            name = user.displayName,
            avatarTone = AvatarTones.bySeed(user.avatarSeed),
            size = 44.dp,
        )
        Spacer(Modifier.width(PulseSpacing.md))
        Column(Modifier.weight(1f)) {
            Text(
                text = user.displayName,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = c.textPrimary,
                maxLines = 1,
            )
            val meta = listOfNotNull(
                user.phone?.takeIf { it.isNotBlank() },
                user.username.takeIf { it.isNotBlank() }?.let { "@$it" },
            ).joinToString(" · ")
            if (meta.isNotBlank()) {
                Text(
                    text = meta,
                    style = MaterialTheme.typography.bodySmall,
                    color = c.textSecondary,
                    maxLines = 1,
                )
            }
        }
        Spacer(Modifier.width(PulseSpacing.sm))
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(PulseShapes.full)
                .clickable(role = Role.Button, onClick = onShare),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = AppIcons.Send,
                contentDescription = stringResource(R.string.conversation_contact_share_cd),
                modifier = Modifier.size(PulseIconSizes.small),
                tint = c.accent,
            )
        }
    }
}
