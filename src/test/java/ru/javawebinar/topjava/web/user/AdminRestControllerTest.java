package ru.javawebinar.topjava.web.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.javawebinar.topjava.TestUtil;
import ru.javawebinar.topjava.UserTestData;
import ru.javawebinar.topjava.model.Role;
import ru.javawebinar.topjava.model.User;
import ru.javawebinar.topjava.service.UserService;
import ru.javawebinar.topjava.util.exception.NotFoundException;
import ru.javawebinar.topjava.web.AbstractControllerTest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.javawebinar.topjava.UserTestData.*;
import static ru.javawebinar.topjava.util.exception.ErrorType.VALIDATION_ERROR;
import static ru.javawebinar.topjava.web.ExceptionInfoHandler.EXCEPTION_DUPLICATE_EMAIL;

class AdminRestControllerTest extends AbstractControllerTest {

    @Autowired
    private UserService userService;

    AdminRestControllerTest() {
        super(AdminRestController.REST_URL);
    }

    @Test
    void get() throws Exception {
        perform(doGet(ADMIN_ID).basicAuth(ADMIN))
                .andExpect(status().isOk())
                .andDo(print())
                // https://jira.spring.io/browse/SPR-14472
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(USER_MATCHERS.contentJson(ADMIN));
    }

    @Test
    void getNotFound() throws Exception {
        perform(doGet(1).basicAuth(ADMIN))
                .andExpect(status().isUnprocessableEntity())
                .andDo(print());
    }

    @Test
    void getByEmail() throws Exception {
        perform(doGet("by?email={email}", ADMIN.getEmail()).basicAuth(ADMIN))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(USER_MATCHERS.contentJson(ADMIN));
    }

    @Test
    void delete() throws Exception {
        perform(doDelete(USER_ID).basicAuth(ADMIN))
                .andDo(print())
                .andExpect(status().isNoContent());
        assertThrows(NotFoundException.class, () -> userService.get(USER_ID));
    }

    @Test
    void deleteNotFound() throws Exception {
        perform(doDelete(1).basicAuth(ADMIN))
                .andExpect(status().isUnprocessableEntity())
                .andDo(print());
    }

    @Test
    void getUnAuth() throws Exception {
        perform(doGet())
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getForbidden() throws Exception {
        perform(doGet().basicAuth(USER))
                .andExpect(status().isForbidden());
    }

    @Test
    void update() throws Exception {
        User updated = UserTestData.getUpdated();
        perform(doPut(USER_ID).jsonUserWithPassword(updated).basicAuth(ADMIN))
                .andExpect(status().isNoContent());

        USER_MATCHERS.assertMatch(userService.get(USER_ID), updated);
    }

    @Test
    void createWithLocation() throws Exception {
        User newUser = UserTestData.getNew();
        ResultActions action = perform(doPost().jsonUserWithPassword(newUser).basicAuth(ADMIN))
                .andExpect(status().isCreated());

        User created = TestUtil.readFromJson(action, User.class);
        Integer newId = created.getId();
        newUser.setId(newId);
        USER_MATCHERS.assertMatch(created, newUser);
        USER_MATCHERS.assertMatch(userService.get(newId), newUser);
    }

    @Test
    void getAll() throws Exception {
        perform(doGet().basicAuth(ADMIN))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(USER_MATCHERS.contentJson(ADMIN, USER));
    }

    @Test
    void enable() throws Exception {
        perform(doPatch(USER_ID).basicAuth(ADMIN).unwrap()
                .param("enabled", "false")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNoContent());

        assertFalse(userService.get(USER_ID).isEnabled());
    }


    @Test
    void createInvalid() throws Exception {
        User expected = new User(null, null, "", "newPass", 7300, Role.ROLE_USER, Role.ROLE_ADMIN);
        perform(doPost().jsonBody(expected).basicAuth(ADMIN))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(errorType(VALIDATION_ERROR))
                .andDo(print());
    }

    @Test
    void updateInvalid() throws Exception {
        User updated = new User(USER);
        updated.setName("");
        perform(doPut(USER_ID).jsonBody(updated).basicAuth(ADMIN))
                .andExpect(status().isUnprocessableEntity())
                .andDo(print())
                .andExpect(errorType(VALIDATION_ERROR))
                .andDo(print());
    }

    @Test
    @Transactional(propagation = Propagation.NEVER)
    void updateDuplicate() throws Exception {
        User updated = new User(USER);
        updated.setEmail("admin@gmail.com");
        perform(doPut(USER_ID).jsonUserWithPassword(updated).basicAuth(ADMIN))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(errorType(VALIDATION_ERROR))
                .andExpect(detailMessage(EXCEPTION_DUPLICATE_EMAIL))
                .andDo(print());
    }

    @Test
    @Transactional(propagation = Propagation.NEVER)
    void createDuplicate() throws Exception {
        User expected = new User(null, "New", "user@yandex.ru", "newPass", 2300, Role.ROLE_USER, Role.ROLE_ADMIN);
        perform(doPost().jsonUserWithPassword(expected).basicAuth(ADMIN))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(errorType(VALIDATION_ERROR))
                .andExpect(detailMessage(EXCEPTION_DUPLICATE_EMAIL));

    }
}