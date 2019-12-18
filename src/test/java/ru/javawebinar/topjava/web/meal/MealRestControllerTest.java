package ru.javawebinar.topjava.web.meal;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.javawebinar.topjava.MealTestData;
import ru.javawebinar.topjava.model.Meal;
import ru.javawebinar.topjava.service.MealService;
import ru.javawebinar.topjava.util.exception.NotFoundException;
import ru.javawebinar.topjava.web.AbstractControllerTest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.javawebinar.topjava.MealTestData.*;
import static ru.javawebinar.topjava.TestUtil.readFromJson;
import static ru.javawebinar.topjava.TestUtil.readFromJsonMvcResult;
import static ru.javawebinar.topjava.UserTestData.*;
import static ru.javawebinar.topjava.model.AbstractBaseEntity.START_SEQ;
import static ru.javawebinar.topjava.util.MealsUtil.createTo;
import static ru.javawebinar.topjava.util.MealsUtil.getTos;
import static ru.javawebinar.topjava.util.exception.ErrorType.VALIDATION_ERROR;
import static ru.javawebinar.topjava.web.ExceptionInfoHandler.EXCEPTION_DUPLICATE_DATETIME;

class MealRestControllerTest extends AbstractControllerTest {

    @Autowired
    private MealService mealService;

    MealRestControllerTest() {
        super(MealRestController.REST_URL);
    }

    @Test
    void get() throws Exception {
        perform(doGet(ADMIN_MEAL_ID).basicAuth(ADMIN))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(result -> MEAL_MATCHERS.assertMatch(readFromJsonMvcResult(result, Meal.class), ADMIN_MEAL1));
    }


    @Test
    void getUnauth() throws Exception {
        perform(doGet(MEAL1_ID))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getNotFound() throws Exception {
        perform(doGet(ADMIN_MEAL_ID).basicAuth(USER))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void delete() throws Exception {
        perform(doDelete(MEAL1_ID).basicAuth(USER))
                .andExpect(status().isNoContent());
        assertThrows(NotFoundException.class, () -> mealService.get(MEAL1_ID, USER_ID));
    }

    @Test
    void deleteNotFound() throws Exception {
        perform(doDelete(ADMIN_MEAL_ID).basicAuth(USER))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void update() throws Exception {
        Meal updated = MealTestData.getUpdated();
        perform(doPut(MEAL1_ID).jsonBody(updated).basicAuth(USER))
                .andExpect(status().isNoContent());

        MEAL_MATCHERS.assertMatch(mealService.get(MEAL1_ID, START_SEQ), updated);
    }

    @Test
    void createWithLocation() throws Exception {
        Meal newMeal = MealTestData.getNew();
        ResultActions action = perform(doPost().jsonBody(newMeal).basicAuth(USER));

        Meal created = readFromJson(action, Meal.class);
        Integer newId = created.getId();
        newMeal.setId(newId);
        MEAL_MATCHERS.assertMatch(created, newMeal);
        MEAL_MATCHERS.assertMatch(mealService.get(newId, USER_ID), newMeal);
    }

    @Test
    void getAll() throws Exception {
        perform(doGet().basicAuth(USER))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(MEAL_TO_MATCHERS.contentJson(getTos(MEALS, USER.getCaloriesPerDay())));
    }

    @Test
    void filter() throws Exception {
        perform(doGet("filter").basicAuth(USER).unwrap()
                .param("startDate", "2015-05-30").param("startTime", "07:00")
                .param("endDate", "2015-05-31").param("endTime", "11:00"))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(MEAL_TO_MATCHERS.contentJson(createTo(MEAL5, true), createTo(MEAL1, false)));
    }

    @Test
    void filterAll() throws Exception {
        perform(doGet("filter?startDate=&endTime=").basicAuth(USER))
                .andExpect(status().isOk())
                .andExpect(MEAL_TO_MATCHERS.contentJson(getTos(MEALS, USER.getCaloriesPerDay())));
    }

    @Test
    void createInvalid() throws Exception {
        Meal invalid = new Meal(null, null, "Dummy", 200);
        perform(doPost().jsonBody(invalid).basicAuth(ADMIN))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(errorType(VALIDATION_ERROR))
                .andDo(print());
    }

    @Test
    void updateInvalid() throws Exception {
        Meal invalid = new Meal(MEAL1_ID, null, null, 6000);
        perform(doPut(MEAL1_ID).jsonBody(invalid).basicAuth(USER))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(errorType(VALIDATION_ERROR))
                .andDo(print());
    }

    @Test
    @Transactional(propagation = Propagation.NEVER)
    void updateDuplicate() throws Exception {
        Meal invalid = new Meal(MEAL1_ID, MEAL2.getDateTime(), "Dummy", 200);

        perform(doPut(MEAL1_ID).jsonBody(invalid).basicAuth(USER))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(errorType(VALIDATION_ERROR))
                .andExpect(detailMessage(EXCEPTION_DUPLICATE_DATETIME));
    }

    @Test
    @Transactional(propagation = Propagation.NEVER)
    void createDuplicate() throws Exception {
        Meal invalid = new Meal(null, ADMIN_MEAL1.getDateTime(), "Dummy", 200);
        perform(doPost().jsonBody(invalid).basicAuth(ADMIN))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(errorType(VALIDATION_ERROR))
                .andExpect(detailMessage(EXCEPTION_DUPLICATE_DATETIME));
    }

    @Test
    void updateHtmlUnsafe() throws Exception {
        Meal invalid = new Meal(MEAL1_ID, LocalDateTime.now(), "<script>alert(123)</script>", 200);
        perform(doPut(MEAL1_ID).jsonBody(invalid).basicAuth(USER))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(errorType(VALIDATION_ERROR))
                .andDo(print());
    }
}