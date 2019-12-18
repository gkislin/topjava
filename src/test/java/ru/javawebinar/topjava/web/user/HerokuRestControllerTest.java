package ru.javawebinar.topjava.web.user;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePropertySource;
import org.springframework.test.context.ActiveProfiles;
import ru.javawebinar.topjava.util.exception.ErrorType;
import ru.javawebinar.topjava.web.AbstractControllerTest;

import java.io.IOException;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.javawebinar.topjava.Profiles.HEROKU;
import static ru.javawebinar.topjava.UserTestData.*;
import static ru.javawebinar.topjava.util.exception.ModificationRestrictionException.EXCEPTION_MODIFICATION_RESTRICTION;

@ActiveProfiles({HEROKU})
class HerokuRestControllerTest extends AbstractControllerTest {

    public HerokuRestControllerTest() {
        super(AdminRestController.REST_URL);
    }

    // Set DATABASE_URL environment for heroku profile
    static {
        Resource resource = new ClassPathResource("db/postgres.properties");
        try {
            ResourcePropertySource propertySource = new ResourcePropertySource(resource);
            String herokuDbUrl = String.format("postgres://%s:%s@%s",
                    propertySource.getProperty("database.username"),
                    propertySource.getProperty("database.password"),
                    ((String) propertySource.getProperty("database.url")).substring(18));
            System.out.println(herokuDbUrl);

            System.setProperty("DATABASE_URL", herokuDbUrl);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    void delete() throws Exception {
        perform(doDelete(USER_ID).basicAuth(ADMIN))
                .andDo(print())
                .andExpect(errorType(ErrorType.VALIDATION_ERROR))
                .andExpect(detailMessage(EXCEPTION_MODIFICATION_RESTRICTION))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void update() throws Exception {
        perform(doPut(USER_ID).jsonUserWithPassword(USER).basicAuth(ADMIN))
                .andExpect(errorType(ErrorType.VALIDATION_ERROR))
                .andExpect(detailMessage(EXCEPTION_MODIFICATION_RESTRICTION))
                .andExpect(status().isUnprocessableEntity());
    }
}