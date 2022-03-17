package ru.javawebinar.topjava.util;

import ru.javawebinar.topjava.model.UserMeal;
import ru.javawebinar.topjava.model.UserMealWithExcess;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.chrono.ChronoLocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class UserMealsUtil {
    public static void main(String[] args) {
        List<UserMeal> meals = Arrays.asList(
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 30, 10, 0), "Завтрак", 500),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 30, 13, 0), "Обед", 1000),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 30, 20, 0), "Ужин", 500),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 0, 0), "Еда на граничное значение", 100),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 10, 0), "Завтрак", 1000),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 13, 0), "Обед", 500),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 20, 0), "Ужин", 410)
        );

        //List<UserMealWithExcess> mealsTo = filteredByCycles(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000);
        //mealsTo.forEach(System.out::println);
        System.out.println(filteredByCycles(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000));
        System.out.println(filteredByStreams(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000));
    }

    public static List<UserMealWithExcess> filteredByCycles(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        Map<LocalDate, Integer> mapForAmountOfCaloriesPerDay = new HashMap<>();
        for (UserMeal userMeal : meals) {
            mapForAmountOfCaloriesPerDay.merge(userMeal.getDateTime().toLocalDate(), userMeal.getCalories(), Integer::sum);
        }
        List<UserMealWithExcess> filteredList = new ArrayList<>();
        for (UserMeal userMeal : meals) {
            if (TimeUtil.isBetweenHalfOpen(userMeal.getDateTime().toLocalTime(), startTime, endTime)) {
                filteredList.add(new UserMealWithExcess(userMeal.getDateTime(), userMeal.getDescription(), userMeal.getCalories(), mapForAmountOfCaloriesPerDay.get(userMeal.getDateTime().toLocalDate()) > caloriesPerDay));
            }
        }
        // TODO return filtered list with excess. Implement by cycles
        return filteredList;
    }

    public static List<UserMealWithExcess> filteredByStreams(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        Map<LocalDate, Integer> mapForAmountOfCaloriesPerDay = meals.stream().collect(Collectors.toMap(userMeal -> userMeal.getDateTime().toLocalDate(), UserMeal::getCalories, Integer::sum));
        //Map<LocalDate, Integer> mapForAmountOfCaloriesPerDay = meals.stream().collect(Collectors.groupingBy(userMeal -> userMeal.getDateTime().toLocalDate(), HashMap::new, Collectors.summingInt(UserMeal::getCalories)));
        return meals.stream().filter(userMeal -> TimeUtil.isBetweenHalfOpen(userMeal.getDateTime().toLocalTime(), startTime, endTime)).map(userMeal -> new UserMealWithExcess(userMeal.getDateTime(), userMeal.getDescription(), userMeal.getCalories(), mapForAmountOfCaloriesPerDay.get(userMeal.getDateTime().toLocalDate()) > caloriesPerDay)).collect(Collectors.toList());
        // TODO Implement by streams
    }
}
