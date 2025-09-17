package com.ppcex.user.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserNoGeneratorTest {

    private UserNoGenerator userNoGenerator;

    @BeforeEach
    void setUp() {
        userNoGenerator = new UserNoGenerator();
    }

    @Test
    void generateUserNo_ShouldStartWithU() {
        String userNo = userNoGenerator.generateUserNo();

        assertNotNull(userNo);
        assertTrue(userNo.startsWith("U"));
    }

    @Test
    void generateUserNo_ShouldHaveCorrectLength() {
        String userNo = userNoGenerator.generateUserNo();

        assertEquals(20, userNo.length()); // U + 14 digits + 4 digit sequence
    }

    @Test
    void generateUserNo_ShouldBeUnique() {
        String userNo1 = userNoGenerator.generateUserNo();
        String userNo2 = userNoGenerator.generateUserNo();

        assertNotEquals(userNo1, userNo2);
    }

    @Test
    void generateInviteCode_ShouldHaveCorrectLength() {
        String inviteCode = userNoGenerator.generateInviteCode();

        assertNotNull(inviteCode);
        assertEquals(8, inviteCode.length());
    }

    @Test
    void generateInviteCode_ShouldContainOnlyAlphanumeric() {
        String inviteCode = userNoGenerator.generateInviteCode();

        assertTrue(inviteCode.matches("^[A-Z0-9]{8}$"));
    }

    @Test
    void generateInviteCode_ShouldBeUnique() {
        String inviteCode1 = userNoGenerator.generateInviteCode();
        String inviteCode2 = userNoGenerator.generateInviteCode();

        assertNotEquals(inviteCode1, inviteCode2);
    }

    @Test
    void generateSessionId_ShouldNotBeNull() {
        String sessionId = userNoGenerator.generateSessionId();

        assertNotNull(sessionId);
        assertTrue(sessionId.length() > 0);
    }

    @Test
    void generateSessionId_ShouldBeUnique() {
        String sessionId1 = userNoGenerator.generateSessionId();
        String sessionId2 = userNoGenerator.generateSessionId();

        assertNotEquals(sessionId1, sessionId2);
    }

    @Test
    void generateSessionId_ShouldContainTimestamp() {
        String sessionId = userNoGenerator.generateSessionId();

        // Should start with a timestamp (13 digits for milliseconds)
        assertTrue(sessionId.matches("^\\d{13}.+"));
    }
}