package ru.javawebinar.topjava.util;

import ru.javawebinar.topjava.model.UserMeal;
import ru.javawebinar.topjava.model.UserMealWithExceed;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

/**
 * GKislin
 * 31.05.2015.
 */
public class UserMealsUtil {
    public static void main(String[] args) {
        List<UserMeal> mealList = Arrays.asList(
                new UserMeal(LocalDateTime.of(2015, Month.MAY, 30,10,0), "Завтрак", 500),
                new UserMeal(LocalDateTime.of(2015, Month.MAY, 30,13,0), "Обед", 1000),
                new UserMeal(LocalDateTime.of(2015, Month.MAY, 30,20,0), "Ужин", 500),
                new UserMeal(LocalDateTime.of(2015, Month.MAY, 31,10,0), "Завтрак", 1000),
                new UserMeal(LocalDateTime.of(2015, Month.MAY, 31,13,0), "Обед", 500),
                new UserMeal(LocalDateTime.of(2015, Month.MAY, 31,20,0), "Ужин", 510)
        );
        getFilteredWithExceeded(mealList, LocalTime.of(7, 0), LocalTime.of(12,0), 2000).forEach(System.out :: println);
//        .toLocalDate();
//        .toLocalTime();
    }

    public static List<UserMealWithExceed>  getFilteredWithExceeded(List<UserMeal> mealList, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        /*List<LocalDate> dateList = new ArrayList<>();
        int calories = 0;
        LocalDate localDate = mealList.get(0).getDateTime().toLocalDate();
        for (UserMeal x : mealList) {
            if (localDate.isEqual(x.getDateTime().toLocalDate())) {
                calories += x.getCalories();
            } else {
                if (calories > caloriesPerDay) dateList.add(localDate);
                calories = x.getCalories();
                localDate = x.getDateTime().toLocalDate();
            }
            if (calories > caloriesPerDay) dateList.add(localDate);
        }*/

        /*Map<LocalDateTime, Integer> dateIntegerMap = mealList.stream()
                .collect(Collectors.groupingBy(UserMeal::getDateTime, Collectors.summingInt(UserMeal::getCalories)));
        System.out.println(dateIntegerMap);*/

        Map<LocalDate, Integer> dateIntegerMap = mealList.stream()
                .collect(Collectors.groupingBy(x -> x.getDateTime().toLocalDate(), Collectors.summingInt(UserMeal::getCalories)));

        return mealList.stream()
                .filter(x -> TimeUtil.isBetween(x.getDateTime().toLocalTime(), startTime, endTime))
                .map(x -> new UserMealWithExceed(x.getDateTime(), x.getDescription(), x.getCalories(), dateIntegerMap.containsKey(x.getDateTime().toLocalDate()) && dateIntegerMap.get(x.getDateTime().toLocalDate()) > caloriesPerDay))
                .collect(Collectors.toList());
    }
}
