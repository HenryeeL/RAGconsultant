package org.example.consultantrag.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 计算器工具
 * 用于精确的数学运算，弥补 LLM 逻辑计算能力的不足
 */
@Component("calculatorTool")
public class CalculatorTool {

    @Tool("执行基础数学运算：加、减、乘、除")
    public String calculate(
            @P("操作数 A") double a,
            @P("运算符，支持：+, -, *, /") String operator,
            @P("操作数 B") double b) {

        BigDecimal b1 = BigDecimal.valueOf(a);
        BigDecimal b2 = BigDecimal.valueOf(b);
        BigDecimal result;

        try {
            switch (operator) {
                case "+" -> result = b1.add(b2);
                case "-" -> result = b1.subtract(b2);
                case "*" -> result = b1.multiply(b2);
                case "/" -> {
                    if (b == 0) return "错误：除数不能为零";
                    result = b1.divide(b2, 4, RoundingMode.HALF_UP);
                }
                default -> {
                    return "错误：不支持该运算符 " + operator;
                }
            }
            return String.format("%s %s %s = %s", a, operator, b, result.stripTrailingZeros().toPlainString());
        } catch (Exception e) {
            return "计算出错: " + e.getMessage();
        }
    }
}