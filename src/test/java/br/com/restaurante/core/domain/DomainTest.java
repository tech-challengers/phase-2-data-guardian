package br.com.restaurante.core.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DomainTest {

    @Test
    void userTypeSetters() {
        UserType ut = new UserType();
        ut.setId(1L);
        ut.setName("T");
        assertEquals(1L, ut.getId());
        assertEquals("T", ut.getName());
    }

    @Test
    void userTypeSetBlankNameThrows() {
        UserType ut = new UserType();
        assertThrows(IllegalArgumentException.class, () -> ut.setName(" "));
        assertThrows(IllegalArgumentException.class, () -> ut.setName(null));
    }

    @Test
    void userSetters() {
        User u = new User();
        u.setId(1L);
        u.setName("N");
        u.setEmail("E");
        UserType ut = new UserType(1L, "T");
        u.setUserType(ut);

        assertEquals(1L, u.getId());
        assertEquals("N", u.getName());
        assertEquals("E", u.getEmail());
        assertEquals("T", u.getUserType().getName());
    }

    @Test
    void userAssignTypeNullThrows() {
        User u = new User();
        assertThrows(IllegalArgumentException.class, () -> u.assignType(null));
    }
}
