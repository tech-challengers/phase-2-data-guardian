package br.com.restaurante.infrastructure.persistence.entities;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EntitiesTest {
    @Test
    void testUserTypeEntity() {
        UserTypeEntity ut = new UserTypeEntity();
        ut.setId(1L);
        ut.setName("A");
        assertEquals(1L, ut.getId());
        assertEquals("A", ut.getName());
    }

    @Test
    void testUserEntity() {
        UserEntity u = new UserEntity();
        u.setId(1L);
        u.setName("A");
        u.setEmail("B");
        u.setPassword("pass");
        UserTypeEntity ut = new UserTypeEntity(1L, "C");
        u.setUserType(ut);
        assertEquals(1L, u.getId());
        assertEquals("A", u.getName());
        assertEquals("B", u.getEmail());
        assertEquals("pass", u.getPassword());
        assertEquals("C", u.getUserType().getName());
    }
}
