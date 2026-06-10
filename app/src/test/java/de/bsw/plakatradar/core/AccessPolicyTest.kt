package de.bsw.plakatradar.core

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AccessPolicyTest {
    @Test
    fun leaderWithTeamSecretCanManageTeamSecurityAndSync() {
        val state = LocalTeamState(
            deviceId = "leader-phone",
            deviceName = "David",
            role = MemberRole.LEADER,
            teamId = "team-1",
            teamName = "BSW Nordsachsen",
            teamSecret = "secret-1",
            devices = listOf(
                DeviceRecord(
                    deviceId = "leader-phone",
                    displayName = "David",
                    role = MemberRole.LEADER,
                    approved = true,
                    blocked = false
                )
            )
        )

        assertTrue(AccessPolicy.canShowQr(state))
        assertTrue(AccessPolicy.canManageTeamSecurity(state))
        assertTrue(AccessPolicy.canSync(state))
        assertTrue(AccessPolicy.canShareSyncBundle(state))
    }

    @Test
    fun blockedMemberCannotSyncShareOrExport() {
        val state = LocalTeamState(
            deviceId = "member-phone",
            deviceName = "Patrick",
            role = MemberRole.MEMBER,
            teamId = "team-1",
            teamName = "BSW Nordsachsen",
            teamSecret = "secret-1",
            devices = listOf(
                DeviceRecord(
                    deviceId = "member-phone",
                    displayName = "Patrick",
                    role = MemberRole.MEMBER,
                    approved = true,
                    blocked = true
                )
            )
        )

        assertFalse(AccessPolicy.canSync(state))
        assertFalse(AccessPolicy.canShareSyncBundle(state))
        assertFalse(AccessPolicy.canExportForAuthority(state))
    }

    @Test
    fun offlineNoQrUserCanWorkLocallyAndExportAuthorityZipButCannotSync() {
        val state = LocalTeamState(
            deviceId = "offline-phone",
            deviceName = "Lokales Teammitglied",
            role = MemberRole.MEMBER,
            teamId = "local-offline-team",
            teamName = "Lokaler Modus",
            teamSecret = null,
            devices = emptyList()
        )

        assertTrue(AccessPolicy.canAddPoster(state))
        assertTrue(AccessPolicy.canExportForAuthority(state))
        assertFalse(AccessPolicy.canSync(state))
        assertFalse(AccessPolicy.canShareSyncBundle(state))
    }
}
