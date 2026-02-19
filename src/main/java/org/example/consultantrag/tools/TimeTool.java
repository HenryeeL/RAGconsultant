package org.example.consultantrag.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * 时间工具
 * 提供时间查询、转换、计算等功能
 */
@Component
public class TimeTool {

    /**
     * 获取当前时间
     */
    @Tool("获取当前的日期和时间")
    public String getCurrentTime() {
        LocalDateTime now = LocalDateTime.now();
        return String.format("""
            当前时间信息：
            日期: %s
            时间: %s
            星期: %s
            时间戳: %d
            """,
                now.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日")),
                now.format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                getChineseWeekday(now.getDayOfWeek()),
                now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        );
    }

    /**
     * 获取指定时区的时间
     */
    @Tool("查询指定时区的当前时间")
    public String getTimeInTimezone(@P("时区名称，如：Asia/Shanghai, America/New_York, Europe/London") String timezone) {
        try {
            ZoneId zoneId = ZoneId.of(timezone);
            ZonedDateTime zonedDateTime = ZonedDateTime.now(zoneId);

            return String.format("""
                【%s 时区时间】
                当前时间: %s
                日期: %s
                UTC偏移: %s
                """,
                    timezone,
                    zonedDateTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                    zonedDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                    zonedDateTime.getOffset()
            );
        } catch (Exception e) {
            return "错误：无效的时区名称。常用时区：Asia/Shanghai, America/New_York, Europe/London, Asia/Tokyo";
        }
    }

    /**
     * 计算两个日期之间的天数差
     */
    @Tool("计算两个日期之间相差多少天")
    public String daysBetween(
            @P("起始日期，格式：yyyy-MM-dd") String startDate,
            @P("结束日期，格式：yyyy-MM-dd") String endDate) {
        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            long days = ChronoUnit.DAYS.between(start, end);

            return String.format("从 %s 到 %s 相差 %d 天", startDate, endDate, Math.abs(days));
        } catch (Exception e) {
            return "错误：日期格式不正确，请使用 yyyy-MM-dd 格式，例如：2024-01-01";
        }
    }

    /**
     * 计算未来或过去的日期
     */
    @Tool("计算从今天开始，往前或往后推算指定天数后的日期")
    public String calculateDate(@P("天数，正数表示未来，负数表示过去") int days) {
        LocalDate targetDate = LocalDate.now().plusDays(days);
        String direction = days > 0 ? "后" : "前";

        return String.format("""
            %d天%s的日期是：
            日期: %s
            星期: %s
            距今: %d天
            """,
                Math.abs(days),
                direction,
                targetDate.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日")),
                getChineseWeekday(targetDate.getDayOfWeek()),
                Math.abs(days)
        );
    }

    /**
     * 判断是否为工作日
     */
    @Tool("判断指定日期是否为工作日（周一到周五）")
    public String isWorkday(@P("日期，格式：yyyy-MM-dd") String date) {
        try {
            LocalDate localDate = LocalDate.parse(date);
            DayOfWeek dayOfWeek = localDate.getDayOfWeek();
            boolean isWorkday = dayOfWeek.getValue() >= 1 && dayOfWeek.getValue() <= 5;

            return String.format("%s (%s) %s",
                    date,
                    getChineseWeekday(dayOfWeek),
                    isWorkday ? "是工作日" : "是周末"
            );
        } catch (Exception e) {
            return "错误：日期格式不正确";
        }
    }

    /**
     * 获取本月剩余天数
     */
    @Tool("查询本月还剩多少天")
    public String daysLeftInMonth() {
        LocalDate today = LocalDate.now();
        LocalDate endOfMonth = today.withDayOfMonth(today.lengthOfMonth());
        long daysLeft = ChronoUnit.DAYS.between(today, endOfMonth);

        return String.format("""
            本月信息：
            当前日期: %s
            本月最后一天: %s
            本月剩余: %d天
            本月总天数: %d天
            """,
                today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                endOfMonth.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                daysLeft,
                today.lengthOfMonth()
        );
    }

    /**
     * 辅助方法：将英文星期转换为中文
     */
    private String getChineseWeekday(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> "星期一";
            case TUESDAY -> "星期二";
            case WEDNESDAY -> "星期三";
            case THURSDAY -> "星期四";
            case FRIDAY -> "星期五";
            case SATURDAY -> "星期六";
            case SUNDAY -> "星期日";
        };
    }
}

