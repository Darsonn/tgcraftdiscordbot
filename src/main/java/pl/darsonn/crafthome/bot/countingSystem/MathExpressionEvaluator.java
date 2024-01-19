package pl.darsonn.crafthome.bot.countingSystem;

public class MathExpressionEvaluator {
    public static int evaluateMathExpression(String expression) {
        expression = expression.replaceAll("\\s", ""); // Usuń białe znaki
        return evaluateExpressionRecursively(expression);
    }

    private static int evaluateExpressionRecursively(String expression) {
        int result = 0;
        int currentNumber = 0;
        char operation = '+';

        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);

            if (Character.isDigit(c)) {
                currentNumber = currentNumber * 10 + Character.getNumericValue(c);
            } else if (c == '+' || c == '-') {
                result = applyOperation(result, currentNumber, operation);
                currentNumber = 0;
                operation = c;
            } else if (c == '*' || c == '/') {
                int nextNumber = getNextNumber(expression.substring(i + 1));
                currentNumber = applyOperation(currentNumber, nextNumber, c);
                i += Integer.toString(nextNumber).length(); // Przeskocz do następnego operatora lub końca wyrażenia
            } else {
                return -125743;
            }

            if (i == expression.length() - 1) {
                result = applyOperation(result, currentNumber, operation);
            }
        }

        return result;
    }

    private static int getNextNumber(String expression) {
        int currentNumber = 0;
        for (char c : expression.toCharArray()) {
            if (Character.isDigit(c)) {
                currentNumber = currentNumber * 10 + Character.getNumericValue(c);
            } else {
                break;
            }
        }
        return currentNumber;
    }

    private static int applyOperation(int result, int currentNumber, char operation) {
        switch (operation) {
            case '+':
                return result + currentNumber;
            case '-':
                return result - currentNumber;
            case '*':
                return result * currentNumber;
            case '/':
                if (currentNumber != 0) {
                    return result / currentNumber;
                } else {
                    return -125744;
                }
            default:
                return result;
        }
    }
}
