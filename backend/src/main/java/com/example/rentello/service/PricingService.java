package com.example.rentello.service;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.List;

@Service
public class PricingService {

    // Seasonal multipliers
    private static final BigDecimal SUMMER_MULTIPLIER = new BigDecimal("1.3"); // %30 increase
    private static final BigDecimal WINTER_MULTIPLIER = new BigDecimal("0.9"); // %10 decrease
    private static final BigDecimal SPRING_FALL_MULTIPLIER = new BigDecimal("1.1"); // %10 increase
    
    // Weekend multiplier
    private static final BigDecimal WEEKEND_MULTIPLIER = new BigDecimal("1.2"); // %20 increase
    
    // Holiday multiplier
    private static final BigDecimal HOLIDAY_MULTIPLIER = new BigDecimal("1.5"); // %50 increase
    
    // High demand period multiplier
    private static final BigDecimal HIGH_DEMAND_MULTIPLIER = new BigDecimal("1.4"); // %40 increase

    /**
     * Calculate dynamic price based on base rate and rental dates
     */
    public BigDecimal calculateDynamicPrice(BigDecimal baseRate, LocalDate startDate, LocalDate endDate) {
        if (baseRate == null || startDate == null || endDate == null) {
            return baseRate;
        }

        BigDecimal totalPrice = BigDecimal.ZERO;
        LocalDate currentDate = startDate;
        
        while (!currentDate.isAfter(endDate)) {
            BigDecimal dailyRate = calculateDailyRate(baseRate, currentDate);
            totalPrice = totalPrice.add(dailyRate);
            currentDate = currentDate.plusDays(1);
        }
        
        return totalPrice.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculate daily rate with all multipliers applied
     */
    private BigDecimal calculateDailyRate(BigDecimal baseRate, LocalDate date) {
        BigDecimal multiplier = BigDecimal.ONE;
        
        // Apply seasonal multiplier
        multiplier = multiplier.multiply(getSeasonalMultiplier(date));
        
        // Apply weekend multiplier
        if (isWeekend(date)) {
            multiplier = multiplier.multiply(WEEKEND_MULTIPLIER);
        }
        
        // Apply holiday multiplier
        if (isHoliday(date)) {
            multiplier = multiplier.multiply(HOLIDAY_MULTIPLIER);
        }
        
        // Apply high demand period multiplier
        if (isHighDemandPeriod(date)) {
            multiplier = multiplier.multiply(HIGH_DEMAND_MULTIPLIER);
        }
        
        return baseRate.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Get seasonal multiplier based on date
     */
    private BigDecimal getSeasonalMultiplier(LocalDate date) {
        Month month = date.getMonth();
        
        // Summer (June, July, August)
        if (month == Month.JUNE || month == Month.JULY || month == Month.AUGUST) {
            return SUMMER_MULTIPLIER;
        }
        
        // Winter (December, January, February)
        if (month == Month.DECEMBER || month == Month.JANUARY || month == Month.FEBRUARY) {
            return WINTER_MULTIPLIER;
        }
        
        // Spring/Fall (March, April, May, September, October, November)
        return SPRING_FALL_MULTIPLIER;
    }

    /**
     * Check if date is weekend
     */
    private boolean isWeekend(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }

    /**
     * Check if date is a holiday
     */
    private boolean isHoliday(LocalDate date) {
        // Turkish holidays and international holidays
        List<LocalDate> holidays = getHolidays(date.getYear());
        return holidays.contains(date);
    }

    /**
     * Check if date is in high demand period
     */
    private boolean isHighDemandPeriod(LocalDate date) {
        Month month = date.getMonth();
        int day = date.getDayOfMonth();
        
        // Summer vacation period (July 1-31, August 1-15)
        if ((month == Month.JULY) || (month == Month.AUGUST && day <= 15)) {
            return true;
        }
        
        // New Year period (December 25-31, January 1-7)
        if ((month == Month.DECEMBER && day >= 25) || (month == Month.JANUARY && day <= 7)) {
            return true;
        }
        
        // Spring break period (April 15-30)
        if (month == Month.APRIL && day >= 15) {
            return true;
        }
        
        return false;
    }

    /**
     * Get list of holidays for a given year
     */
    private List<LocalDate> getHolidays(int year) {
        return Arrays.asList(
            // Fixed holidays
            LocalDate.of(year, 1, 1),   // New Year's Day
            LocalDate.of(year, 4, 23),  // National Sovereignty Day
            LocalDate.of(year, 5, 1),   // Labor Day
            LocalDate.of(year, 5, 19),  // Commemoration of Atatürk
            LocalDate.of(year, 7, 15),  // Democracy Day
            LocalDate.of(year, 8, 30),  // Victory Day
            LocalDate.of(year, 10, 29), // Republic Day
            LocalDate.of(year, 12, 25), // Christmas
            LocalDate.of(year, 12, 31)  // New Year's Eve
            // Note: Religious holidays (Eid) would need calculation based on lunar calendar
        );
    }

    /**
     * Get pricing breakdown for display
     */
    public PricingBreakdown getPricingBreakdown(BigDecimal baseRate, LocalDate startDate, LocalDate endDate) {
        PricingBreakdown breakdown = new PricingBreakdown();
        breakdown.setBaseRate(baseRate);
        breakdown.setStartDate(startDate);
        breakdown.setEndDate(endDate);
        
        BigDecimal totalPrice = BigDecimal.ZERO;
        int totalDays = 0;
        int weekendDays = 0;
        int holidayDays = 0;
        int highDemandDays = 0;
        
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            totalDays++;
            
            if (isWeekend(currentDate)) weekendDays++;
            if (isHoliday(currentDate)) holidayDays++;
            if (isHighDemandPeriod(currentDate)) highDemandDays++;
            
            BigDecimal dailyRate = calculateDailyRate(baseRate, currentDate);
            totalPrice = totalPrice.add(dailyRate);
            currentDate = currentDate.plusDays(1);
        }
        
        breakdown.setTotalDays(totalDays);
        breakdown.setWeekendDays(weekendDays);
        breakdown.setHolidayDays(holidayDays);
        breakdown.setHighDemandDays(highDemandDays);
        breakdown.setTotalPrice(totalPrice);
        breakdown.setAverageRate(totalPrice.divide(BigDecimal.valueOf(totalDays), 2, RoundingMode.HALF_UP));
        
        return breakdown;
    }

    /**
     * Pricing breakdown DTO
     */
    public static class PricingBreakdown {
        private BigDecimal baseRate;
        private LocalDate startDate;
        private LocalDate endDate;
        private int totalDays;
        private int weekendDays;
        private int holidayDays;
        private int highDemandDays;
        private BigDecimal totalPrice;
        private BigDecimal averageRate;

        // Getters and Setters
        public BigDecimal getBaseRate() { return baseRate; }
        public void setBaseRate(BigDecimal baseRate) { this.baseRate = baseRate; }
        
        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
        
        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
        
        public int getTotalDays() { return totalDays; }
        public void setTotalDays(int totalDays) { this.totalDays = totalDays; }
        
        public int getWeekendDays() { return weekendDays; }
        public void setWeekendDays(int weekendDays) { this.weekendDays = weekendDays; }
        
        public int getHolidayDays() { return holidayDays; }
        public void setHolidayDays(int holidayDays) { this.holidayDays = holidayDays; }
        
        public int getHighDemandDays() { return highDemandDays; }
        public void setHighDemandDays(int highDemandDays) { this.highDemandDays = highDemandDays; }
        
        public BigDecimal getTotalPrice() { return totalPrice; }
        public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
        
        public BigDecimal getAverageRate() { return averageRate; }
        public void setAverageRate(BigDecimal averageRate) { this.averageRate = averageRate; }
    }
} 