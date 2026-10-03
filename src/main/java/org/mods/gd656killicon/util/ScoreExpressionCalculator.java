package org.mods.gd656killicon.util;

import java.util.Map;

public class ScoreExpressionCalculator {
   public static double calculate(String expression, Map<String, Double> variables) {
      if (expression != null && !expression.trim().isEmpty()) {
         String[] tokens = expression.split(" ");
         double result = parseValue(tokens[0], variables);

         for (int i = 1; i < tokens.length && i + 1 < tokens.length; i += 2) {
            String operator = tokens[i];
            double value = parseValue(tokens[i + 1], variables);
            switch (operator) {
               case "+":
                  result += value;
                  break;
               case "-":
                  result -= value;
                  break;
               case "*":
                  result *= value;
                  break;
               case "/":
                  if (value == 0.0) {
                     return 0.0;
                  }

                  result /= value;
            }
         }

         return result;
      } else {
         return 0.0;
      }
   }

   private static double parseValue(String token, Map<String, Double> variables) {
      try {
         return Double.parseDouble(token);
      } catch (NumberFormatException var3) {
         return variables.getOrDefault(token, 0.0);
      }
   }
}
