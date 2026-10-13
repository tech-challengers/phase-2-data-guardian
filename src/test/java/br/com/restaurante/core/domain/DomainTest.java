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
        u.setPassword("P");
        UserType ut = new UserType(1L, UserType.DONO_DE_RESTAURANTE);
        u.setUserType(ut);

        assertEquals(1L, u.getId());
        assertEquals("N", u.getName());
        assertEquals("E", u.getEmail());
        assertEquals("P", u.getPassword());
        assertEquals(UserType.DONO_DE_RESTAURANTE, u.getUserType().getName());
        assertTrue(u.isDonoDeRestaurante());
        assertFalse(u.isCliente());
    }

    @Test
    void userAssignTypeNullThrows() {
        User u = new User();
        assertThrows(IllegalArgumentException.class, () -> u.assignType(null));
    }

    @Test
    void authenticatedUserMethods() {
        AuthenticatedUser user = new AuthenticatedUser(1L, "d@r.com", "Dono", UserType.DONO_DE_RESTAURANTE);
        assertEquals(1L, user.getId());
        assertEquals("d@r.com", user.getEmail());
        assertEquals("Dono", user.getName());
        assertEquals(UserType.DONO_DE_RESTAURANTE, user.getRole());
        assertTrue(user.isDonoDeRestaurante());
        assertFalse(user.isCliente());

        AuthenticatedUser cliente = new AuthenticatedUser(2L, "c@e.com", "Cli", UserType.CLIENTE);
        assertTrue(cliente.isCliente());
        assertFalse(cliente.isDonoDeRestaurante());
    }
}
