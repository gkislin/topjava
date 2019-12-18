package ru.javawebinar.topjava.service.datajpa;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import ru.javawebinar.topjava.MealTestData;
import ru.javawebinar.topjava.model.User;
import ru.javawebinar.topjava.service.AbstractUserServiceTest;
import ru.javawebinar.topjava.util.exception.NotFoundException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static ru.javawebinar.topjava.Profiles.DATAJPA;
import static ru.javawebinar.topjava.UserTestData.*;

@ActiveProfiles(DATAJPA)
class DataJpaUserServiceTest extends AbstractUserServiceTest {
    @Test
    void getWithMeals() throws Exception {
        User admin = service.getWithMeals(ADMIN_ID);
        USER_MATCHERS.assertMatch(admin, ADMIN);
        MealTestData.MEAL_MATCHERS.assertMatch(admin.getMeals(), MealTestData.ADMIN_MEAL2, MealTestData.ADMIN_MEAL1);
    }

    @Test
    void getWithMealsNotFound() throws Exception {
        assertThrows(NotFoundException.class, () ->
                service.getWithMeals(1));
    }
}