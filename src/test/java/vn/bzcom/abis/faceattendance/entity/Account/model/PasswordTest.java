package vn.bzcom.abis.faceattendance.entity.Account.model;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import vn.bzcom.abis.faceattendance.common.model.Password;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class PasswordTest {

    private String passwordValue;
    private Password password;

    @Before
    public void setUp() {
        passwordValue = "password001";
        password = Password.builder().value(passwordValue).build();
    }

    @Test
    public void testPassword() {
        assertThat(password.isMatched(passwordValue), is(true));
        assertThat(password.getValue(), is(notNullValue()));
    }

    @Test
    public void changePassword() {
        final String newPassword = "newPassword";
        password.changePassword(newPassword, passwordValue);

        assertThat(password.isMatched(newPassword), is(true));
    }
}
