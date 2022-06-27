package com.step.hryshkin.servlet5;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class CalculatorCore {
    private static boolean calculatorFirstLaunchedMode = true;
    private static boolean errorStatusMode = false;
    private static boolean digitInputOnGoingMode = true;
    private static boolean digitInputOnGoingAfterDotMode = false;
    private static boolean bottomFiledDigitIsNegativeMode = false;
    private static StringBuilder topFieldDisplayed = new StringBuilder();
    private static StringBuilder bottomFieldDisplayed = new StringBuilder(" 0");
    private static BigDecimal firstNumber = BigDecimal.valueOf(0);
    private static BigDecimal secondNumber = BigDecimal.valueOf(0);
    private static char currentOperation = 'n';
    private static char lastPressedButton = '\u0000';
    private static int digitInputCount = 1;

    private CalculatorCore() {
    }

    private static void resetDefaultsAll() {
        calculatorFirstLaunchedMode = true;
        errorStatusMode = false;
        digitInputOnGoingMode = true;
        digitInputOnGoingAfterDotMode = false;
        bottomFiledDigitIsNegativeMode = false;
        topFieldDisplayed = new StringBuilder();
        bottomFieldDisplayed = new StringBuilder(" 0");
        firstNumber = BigDecimal.valueOf(0);
        secondNumber = BigDecimal.valueOf(0);
        currentOperation = 'n';
        lastPressedButton = '\u0000';
        digitInputCount = 1;
    }

    private static void resetDefaultsWhenStartingNewOperation() {
        calculatorFirstLaunchedMode = true;
        errorStatusMode = false;
        digitInputOnGoingMode = true;
        digitInputOnGoingAfterDotMode = false;
        bottomFiledDigitIsNegativeMode = false;
        topFieldDisplayed = new StringBuilder();
        bottomFieldDisplayed = new StringBuilder(" 0");
        firstNumber = BigDecimal.valueOf(0);
        secondNumber = BigDecimal.valueOf(0);
        currentOperation = 'n';
        digitInputCount = 1;
    }

    private static void resetDefaultsCurrent() {
        bottomFieldDisplayed = new StringBuilder(" 0");
        bottomFiledDigitIsNegativeMode = false;
        digitInputOnGoingAfterDotMode = false;
        digitInputOnGoingMode = true;
        digitInputCount = 1;
    }

    protected static void processing(HttpServletRequest req) {
        if (!calculatorFirstLaunchedMode) checkingForInput(req);
        if (errorStatusMode) errorModeSwitchOff();
        if (!calculatorFirstLaunchedMode) actionsAndCalculations();
        calculatorFirstLaunchedMode = false;
    }

    protected static String contextPageToString() {
        String displayedPage = "";
        String resourcePath = "calc_page_source.html";
        if (errorStatusMode) resourcePath = "calc_page_error_source.html";
        try {
            displayedPage = String.format(ResourceReader
                    .webPageContentToString(resourcePath), topFieldDisplayed, bottomFieldDisplayed, debugReportPrint());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return displayedPage;
    }

    private static void checkingForInput(HttpServletRequest req) {
        String input = req.getParameter("answer");
        if (input != null) lastPressedButton = receivedRequestProcessing(req.getParameter("answer"));
        else resetDefaultsAll();
    }

    private static void errorModeSwitchOff() {
        if (lastPressedButton == 'c') resetDefaultsAll();
        else calculatorFirstLaunchedMode = true;
    }

    private static void actionsAndCalculations() {
        switch (lastPressedButton) {
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                actionsOnClickDigits();
                break;
            case '0':
                actionsOnClickZero();
                break;
            case '.':
                actionsOnClickDot();
                break;
            case 'b':
                actionsOnClickBackspace();
                break;
            case 'e':
                resetDefaultsCurrent();
                break;
            case 'c':
                resetDefaultsAll();
                break;
            case '&':
                if (digitInputOnGoingMode) actionsOnClickSignChangeButton();
                break;
            case '+':
                actionsOnClickSumSubDivMulCommon();
                if ((currentOperation != 'n') & (digitInputOnGoingMode)) actionsOnClickSumSpecific();
                actionsOnClickSumSubDivMulCommonFinal();
                break;
            case '-':
                actionsOnClickSumSubDivMulCommon();
                if ((currentOperation != 'n') & (digitInputOnGoingMode)) actionsOnClickSubtractionSpecific();
                actionsOnClickSumSubDivMulCommonFinal();
                break;
            case '/':
                actionsOnClickSumSubDivMulCommon();
                if ((currentOperation != 'n') & (digitInputOnGoingMode)) actionsOnClickDivisionSpecific();
                actionsOnClickSumSubDivMulCommonFinal();
                break;
            case '*':
                actionsOnClickSumSubDivMulCommon();
                if ((currentOperation != 'n') & (digitInputOnGoingMode)) actionsOnClickMultiplicationSpecific();
                actionsOnClickSumSubDivMulCommonFinal();
                break;
            case 's':
                actionsOnClickSquare();
                actionsOnClickResultCommonFinal();
                break;
            case 'r':
                actionsOnClickRoot();
                actionsOnClickResultCommonFinal();
                break;
            case '%':
                actionsOnClickPercentage();
                actionsOnClickResultCommonFinal();
                break;
            case '=':
                actionsOnClickResultCommon();
                if (currentOperation != 'n') {
                    switch (currentOperation) {
                        case '+':
                            actionsOnClickResultSum();
                            actionsOnClickResultCommonFinal();
                            break;
                        case '-':
                            actionsOnClickResultSubtraction();
                            actionsOnClickResultCommonFinal();
                            break;
                        case '/':
                            actionsOnClickResultDivision();
                            actionsOnClickResultCommonFinal();
                            break;
                        case '*':
                            actionsOnClickResultMultiplication();
                            actionsOnClickResultCommonFinal();
                            break;
                    }
                }
                break;
        }
    }

    private static void actionsOnClickDigits() {
        if ((topFieldDisplayed.length() != 0) && (topFieldDisplayed.charAt(topFieldDisplayed.length() - 1) == '=')) {
            resetDefaultsWhenStartingNewOperation();
        }

        if ((topFieldDisplayed.length() != 0) && (topFieldDisplayed.charAt(topFieldDisplayed.length() - 1) == '²')) {
            if (currentOperation == 'n') {
                topFieldDisplayed = new StringBuilder();
            } else {
                topFieldDisplayed = new StringBuilder(firstNumber.toPlainString()).append(currentOperation);
            }
        }

        if ((topFieldDisplayed.length() != 0) && (topFieldDisplayed.toString().contains("√"))) {
            if (currentOperation == 'n') {
                topFieldDisplayed = new StringBuilder();
            } else {
                System.out.println("я тут??");
                topFieldDisplayed = new StringBuilder(firstNumber.toPlainString()).append(currentOperation);
            }
        }

        if (!digitInputOnGoingMode) {
            bottomFieldDisplayed = new StringBuilder(" ");
            digitInputCount = 0;
        } else if ((bottomFieldDisplayed.charAt(1) == '0') & (bottomFieldDisplayed.length() == 2)) {
            bottomFieldDisplayed = new StringBuilder(" ");
            digitInputCount = 0;
        }
        if (digitInputCount < 16) {
            bottomFieldDisplayed.append(lastPressedButton);
            digitInputOnGoingMode = true;
            digitInputCount = digitInputCount + 1;
        }
    }

    private static void actionsOnClickZero() {
        if (!digitInputOnGoingMode) {
            resetDefaultsCurrent();
        } else if (digitInputCount < 16) {
            if ((bottomFieldDisplayed.length() == 2) & (bottomFieldDisplayed.charAt(1) == '0')) {
                digitInputOnGoingMode = true;
            } else if ((bottomFieldDisplayed.length() == 2) & (bottomFieldDisplayed.charAt(1) != '0')) {
                bottomFieldDisplayed.append("0");
                digitInputOnGoingMode = true;
                digitInputCount = digitInputCount + 1;
            } else if (bottomFieldDisplayed.length() > 2) {
                bottomFieldDisplayed.append("0");
                digitInputOnGoingMode = true;
                digitInputCount = digitInputCount + 1;
            }
        }
    }

    private static void actionsOnClickDot() {
        if (!digitInputOnGoingAfterDotMode) {
            bottomFieldDisplayed.append(".");
            digitInputOnGoingMode = true;
            digitInputOnGoingAfterDotMode = true;
        }
    }

    private static void actionsOnClickBackspace() {
        if (digitInputOnGoingMode) {
            int currentBottomFieldLength = bottomFieldDisplayed.length();
            if (currentBottomFieldLength == 2) {
                resetDefaultsCurrent();
            } else if ((currentBottomFieldLength == 4) & (bottomFieldDisplayed.charAt(1) == '0') & digitInputOnGoingAfterDotMode) {
                resetDefaultsCurrent();
            } else if ((currentBottomFieldLength == 3) & (bottomFieldDisplayed.charAt(1) == '0') & digitInputOnGoingAfterDotMode) {
                resetDefaultsCurrent();
            } else if (bottomFieldDisplayed.charAt(currentBottomFieldLength - 1) == '.') {
                bottomFieldDisplayed.setLength(currentBottomFieldLength - 1);
                digitInputOnGoingAfterDotMode = false;
            } else {
                bottomFieldDisplayed.setLength(currentBottomFieldLength - 1);
                digitInputCount = digitInputCount - 1;
            }
        }
    }

    private static void actionsOnClickSignChangeButton() {
        if ((bottomFieldDisplayed.length() == 2) & (bottomFieldDisplayed.charAt(1) != '0')) {
            if (!bottomFiledDigitIsNegativeMode) {
                bottomFieldDisplayed.setCharAt(0, '-');
                bottomFiledDigitIsNegativeMode = true;
            } else {
                bottomFieldDisplayed.setCharAt(0, ' ');
                bottomFiledDigitIsNegativeMode = false;
            }
        } else if (bottomFieldDisplayed.length() >= 3) {
            if (!bottomFiledDigitIsNegativeMode) {
                bottomFieldDisplayed.setCharAt(0, '-');
                bottomFiledDigitIsNegativeMode = true;
            } else {
                bottomFieldDisplayed.setCharAt(0, ' ');
                bottomFiledDigitIsNegativeMode = false;
            }
        }
    }

    private static void actionsOnClickSumSubDivMulCommon() {
        if (currentOperation == 'n') {
            topFieldDisplayed = new StringBuilder(cleanInputBeforeOperation(bottomFieldDisplayed));
            firstNumber = new BigDecimal(String.valueOf(topFieldDisplayed));
            bottomFieldDisplayed = new StringBuilder(topFieldDisplayed);
            secondNumber = firstNumber;
            topFieldDisplayed.append(lastPressedButton);
        }
        if ((currentOperation != 'n') & (!digitInputOnGoingMode)) {
            topFieldDisplayed = new StringBuilder(bottomFieldDisplayed).append(lastPressedButton);
        }
    }

    private static void actionsOnClickSumSpecific() {
        bottomFieldDisplayed = new StringBuilder(cleanInputBeforeOperation(bottomFieldDisplayed));
        secondNumber = new BigDecimal(String.valueOf(bottomFieldDisplayed));
        firstNumber = firstNumber.add(secondNumber).stripTrailingZeros();
        outOfBoundsCheck(firstNumber);
        if (!errorStatusMode) {
            secondNumber = firstNumber;
            bottomFieldDisplayed = new StringBuilder(secondNumber.toPlainString());
            stripTrailingDigitsAfterDotForBottomField();
            topFieldDisplayed = new StringBuilder(secondNumber.toPlainString()).append(lastPressedButton);
        }
    }

    private static void actionsOnClickSubtractionSpecific() {
        bottomFieldDisplayed = new StringBuilder(cleanInputBeforeOperation(bottomFieldDisplayed));
        secondNumber = new BigDecimal(String.valueOf(bottomFieldDisplayed));
        firstNumber = firstNumber.subtract(secondNumber).stripTrailingZeros();
        outOfBoundsCheck(firstNumber);
        if (!errorStatusMode) {
            secondNumber = firstNumber;
            bottomFieldDisplayed = new StringBuilder(secondNumber.toPlainString());
            stripTrailingDigitsAfterDotForBottomField();
            topFieldDisplayed = new StringBuilder(secondNumber.toPlainString()).append(lastPressedButton);
        }
    }

    private static void actionsOnClickDivisionSpecific() {
        bottomFieldDisplayed = new StringBuilder(cleanInputBeforeOperation(bottomFieldDisplayed));
        secondNumber = new BigDecimal(String.valueOf(bottomFieldDisplayed));
        if (secondNumber.compareTo(new BigDecimal(0)) == 0) {
            actionOnDivisionByZero();
        } else {
            firstNumber = firstNumber.divide(secondNumber, 16, RoundingMode.HALF_UP)
                    .stripTrailingZeros();
            outOfBoundsCheck(firstNumber);
            if (!errorStatusMode) {
                secondNumber = firstNumber;
                bottomFieldDisplayed = new StringBuilder(secondNumber.toPlainString());
                stripTrailingDigitsAfterDotForBottomField();
                topFieldDisplayed = new StringBuilder(secondNumber.toPlainString()).append(lastPressedButton);
            }
        }
    }

    private static void actionsOnClickMultiplicationSpecific() {
        bottomFieldDisplayed = new StringBuilder(cleanInputBeforeOperation(bottomFieldDisplayed));
        secondNumber = new BigDecimal(String.valueOf(bottomFieldDisplayed));
        firstNumber = firstNumber.multiply(secondNumber).stripTrailingZeros();
        outOfBoundsCheck(firstNumber);
        if (!errorStatusMode) {
            secondNumber = firstNumber;
            bottomFieldDisplayed = new StringBuilder(secondNumber.toPlainString());
            stripTrailingDigitsAfterDotForBottomField();
            topFieldDisplayed = new StringBuilder(secondNumber.toPlainString()).append(lastPressedButton);
        }
    }

    private static void actionsOnClickSquare() {
        if ((topFieldDisplayed.length() != 0) && (topFieldDisplayed.charAt(topFieldDisplayed.length() - 1) == '=')) {
            currentOperation = 'n';
        }
        if (currentOperation == 'n') {
            topFieldDisplayed = new StringBuilder(cleanInputBeforeOperation(bottomFieldDisplayed));
            firstNumber = new BigDecimal(String.valueOf(topFieldDisplayed));
            firstNumber = firstNumber.multiply(firstNumber).stripTrailingZeros();
            outOfBoundsCheck(firstNumber);
            if (!errorStatusMode) {
                topFieldDisplayed.append("²");
                bottomFieldDisplayed = new StringBuilder(firstNumber.toPlainString());
                stripTrailingDigitsAfterDotForBottomField();
            }
        } else {
            bottomFieldDisplayed = new StringBuilder(cleanInputBeforeOperation(bottomFieldDisplayed));
            topFieldDisplayed = new StringBuilder(firstNumber.toPlainString())
                    .append(currentOperation)
                    .append(cleanInputBeforeOperation(bottomFieldDisplayed))
                    .append("²");
            secondNumber = new BigDecimal(String.valueOf(bottomFieldDisplayed));
            secondNumber = secondNumber.multiply(secondNumber).stripTrailingZeros();
            outOfBoundsCheck(secondNumber);
            if (!errorStatusMode) {
                bottomFieldDisplayed = new StringBuilder(secondNumber.toPlainString());
                stripTrailingDigitsAfterDotForBottomField();
            }
        }
    }

    private static void actionsOnClickRoot() {
        if ((topFieldDisplayed.length() != 0) && (topFieldDisplayed.charAt(topFieldDisplayed.length() - 1) == '=')) {
            currentOperation = 'n';
        }
        if (currentOperation == 'n') {
            firstNumber = new BigDecimal(cleanInputBeforeOperation(bottomFieldDisplayed));
            if (firstNumber.compareTo(new BigDecimal("0")) < 0) {
                topFieldDisplayed = new StringBuilder("Квадратный корень отрицательного числа?");
                bottomFieldDisplayed = new StringBuilder("ОШИБКА");
                errorStatusMode = true;
            }
            if (!errorStatusMode) {
                firstNumber = BigDecimal.valueOf(Math.sqrt(firstNumber.doubleValue())).stripTrailingZeros();
                outOfBoundsCheck(firstNumber);
                if (!errorStatusMode) {
                    topFieldDisplayed = new StringBuilder("√")
                            .append(cleanInputBeforeOperation(bottomFieldDisplayed));
                    bottomFieldDisplayed = new StringBuilder(firstNumber.toPlainString());
                    stripTrailingDigitsAfterDotForBottomField();
                }
            }

        } else {
            bottomFieldDisplayed = new StringBuilder(cleanInputBeforeOperation(bottomFieldDisplayed));
            topFieldDisplayed = new StringBuilder(firstNumber.toPlainString())
                    .append(currentOperation)
                    .append("√")
                    .append(cleanInputBeforeOperation(bottomFieldDisplayed));
            secondNumber = new BigDecimal(String.valueOf(bottomFieldDisplayed));
            if (secondNumber.compareTo(new BigDecimal("0")) < 0) {
                topFieldDisplayed = new StringBuilder("Квадратный корень отрицательного числа?");
                bottomFieldDisplayed = new StringBuilder("ОШИБКА");
                errorStatusMode = true;
            }
            if (!errorStatusMode) {
                secondNumber = BigDecimal.valueOf(Math.sqrt(secondNumber.doubleValue()));
                outOfBoundsCheck(secondNumber);
                if (!errorStatusMode) {
                    bottomFieldDisplayed = new StringBuilder(secondNumber.toPlainString());
                    stripTrailingDigitsAfterDotForBottomField();
                }
            }
        }
    }

    private static void actionsOnClickPercentage() {
        if (currentOperation == '/') {
            bottomFieldDisplayed = new StringBuilder(cleanInputBeforeOperation(bottomFieldDisplayed));
            secondNumber = new BigDecimal(String.valueOf(bottomFieldDisplayed));
            if (secondNumber.compareTo(new BigDecimal(0)) == 0) {
                actionOnDivisionByZero();
            } else {
                firstNumber = firstNumber.divide(secondNumber, 16, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"))
                        .stripTrailingZeros();
                outOfBoundsCheck(firstNumber);
                if (!errorStatusMode) {
                    bottomFieldDisplayed = new StringBuilder(firstNumber.toPlainString());
                    stripTrailingDigitsAfterDotForBottomField();
                    topFieldDisplayed.append(secondNumber.toPlainString()).append("%");

                    calculatorFirstLaunchedMode = true;
                    digitInputOnGoingMode = true;
                    digitInputOnGoingAfterDotMode = false;
                    bottomFiledDigitIsNegativeMode = false;
                    firstNumber = BigDecimal.valueOf(0);
                    secondNumber = BigDecimal.valueOf(0);
                    currentOperation = 'n';
                    lastPressedButton = '\u0000';
                    digitInputCount = 1;
                }
            }
        } else {
            resetDefaultsAll();
        }
    }

    private static void actionsOnClickSumSubDivMulCommonFinal() {
        digitInputOnGoingMode = false;
        digitInputOnGoingAfterDotMode = false;
        bottomFiledDigitIsNegativeMode = false;
        digitInputCount = 0;
        currentOperation = lastPressedButton;
    }

    private static void actionsOnClickResultCommon() {
        if (currentOperation == 'n') {
            topFieldDisplayed = new StringBuilder(cleanInputBeforeOperation(bottomFieldDisplayed));
            firstNumber = new BigDecimal(String.valueOf(topFieldDisplayed));
            bottomFieldDisplayed = new StringBuilder(topFieldDisplayed);
            secondNumber = firstNumber;
            topFieldDisplayed.append(lastPressedButton);
        }
    }

    private static void actionsOnClickResultSum() {
        if (digitInputOnGoingMode) {
            bottomFieldDisplayed = new StringBuilder(cleanInputBeforeOperation(bottomFieldDisplayed));
            topFieldDisplayed.append(bottomFieldDisplayed).append(lastPressedButton);
            secondNumber = new BigDecimal(String.valueOf(bottomFieldDisplayed));
        } else {
            topFieldDisplayed = new StringBuilder(firstNumber.toPlainString())
                    .append(currentOperation)
                    .append(secondNumber.toPlainString())
                    .append(lastPressedButton);
        }
        firstNumber = firstNumber.add(secondNumber).stripTrailingZeros();
        outOfBoundsCheck(firstNumber);
        if (!errorStatusMode) {
            bottomFieldDisplayed = new StringBuilder(firstNumber.toPlainString());
            stripTrailingDigitsAfterDotForBottomField();
        }
    }

    private static void actionsOnClickResultSubtraction() {
        if (digitInputOnGoingMode) {
            bottomFieldDisplayed = new StringBuilder(cleanInputBeforeOperation(bottomFieldDisplayed));
            topFieldDisplayed.append(bottomFieldDisplayed).append(lastPressedButton);
            secondNumber = new BigDecimal(String.valueOf(bottomFieldDisplayed));
        } else {
            topFieldDisplayed = new StringBuilder(bottomFieldDisplayed)
                    .append(currentOperation)
                    .append(secondNumber)
                    .append(lastPressedButton);
        }
        firstNumber = firstNumber.subtract(secondNumber).stripTrailingZeros();
        outOfBoundsCheck(firstNumber);
        if (!errorStatusMode) {
            bottomFieldDisplayed = new StringBuilder(firstNumber.toPlainString());
            stripTrailingDigitsAfterDotForBottomField();
        }
    }

    private static void actionsOnClickResultDivision() {
        if (digitInputOnGoingMode) {
            bottomFieldDisplayed = new StringBuilder(cleanInputBeforeOperation(bottomFieldDisplayed));
            topFieldDisplayed.append(bottomFieldDisplayed).append(lastPressedButton);
            secondNumber = new BigDecimal(String.valueOf(bottomFieldDisplayed));
        } else {
            topFieldDisplayed = new StringBuilder(bottomFieldDisplayed)
                    .append(currentOperation)
                    .append(secondNumber)
                    .append(lastPressedButton);
        }
        if (secondNumber.compareTo(new BigDecimal(0)) == 0) {
            actionOnDivisionByZero();
        } else {
            firstNumber = firstNumber.divide(secondNumber, 16, RoundingMode.HALF_UP)
                    .stripTrailingZeros();
            outOfBoundsCheck(firstNumber);
            if (!errorStatusMode) {
                bottomFieldDisplayed = new StringBuilder(firstNumber.toPlainString());
                stripTrailingDigitsAfterDotForBottomField();
            }
        }
    }

    private static void actionsOnClickResultMultiplication() {
        if (digitInputOnGoingMode) {
            bottomFieldDisplayed = new StringBuilder(cleanInputBeforeOperation(bottomFieldDisplayed));
            topFieldDisplayed.append(bottomFieldDisplayed).append(lastPressedButton);
            secondNumber = new BigDecimal(String.valueOf(bottomFieldDisplayed));
        } else {
            topFieldDisplayed = new StringBuilder(bottomFieldDisplayed)
                    .append(currentOperation)
                    .append(secondNumber)
                    .append(lastPressedButton);
        }
        firstNumber = firstNumber.multiply(secondNumber).stripTrailingZeros();
        outOfBoundsCheck(firstNumber);
        if (!errorStatusMode) {
            bottomFieldDisplayed = new StringBuilder(firstNumber.toPlainString());
            stripTrailingDigitsAfterDotForBottomField();
        }
    }

    private static void actionsOnClickResultCommonFinal() {
        digitInputOnGoingMode = false;
        digitInputOnGoingAfterDotMode = false;
        bottomFiledDigitIsNegativeMode = false;
        digitInputCount = 0;
    }

    private static char receivedRequestProcessing(String input) {
        switch (input) {
            case "СЕ":
                return 'e';
            case "С":
                return 'c';
            case "⌫":
                return 'b';
            case "√х":
                return 'r';
            case "%":
                return '%';
            case "x²":
                return 's';
            case "÷":
                return '/';
            case "7":
                return '7';
            case "8":
                return '8';
            case "9":
                return '9';
            case "×":
                return '*';
            case "4":
                return '4';
            case "5":
                return '5';
            case "6":
                return '6';
            case "-":
                return '-';
            case "1":
                return '1';
            case "2":
                return '2';
            case "3":
                return '3';
            case "+":
                return '+';
            case "±":
                return '&';
            case "0":
                return '0';
            case ",":
                return '.';
            default:
                return '=';
        }
    }

    private static String cleanInputBeforeOperation(StringBuilder input) {
        if (input.charAt(0) == ' ') input.deleteCharAt(0);
        if (digitInputOnGoingAfterDotMode) {
            while ((input.length() > 2) && (input.charAt(input.length() - 1) == '0')) {
                input.setLength(input.length() - 1);
            }
        }
        if (input.charAt(input.length() - 1) == '.') input.deleteCharAt(input.length() - 1);
        if (((input.charAt(0) == '-') & (input.length() == 2)) && (input.charAt(1) == '0')) input.deleteCharAt(0);
        return input.toString();
    }

    private static void outOfBoundsCheck(BigDecimal number) {
        if (number.compareTo(new BigDecimal("0")) < 0) number = number.multiply(new BigDecimal(-1));
        if (number.compareTo(new BigDecimal("9999999999999999")) > 0) {
            topFieldDisplayed = new StringBuilder("Ошибка. Выход за пределы счета");
            bottomFieldDisplayed = new StringBuilder("9.999999999999999Е");
            errorStatusMode = true;
        }
    }

    private static void actionOnDivisionByZero() {
        topFieldDisplayed = new StringBuilder("Деление на ноль невозможно!");
        bottomFieldDisplayed = new StringBuilder("ОШИБКА");
        errorStatusMode = true;
    }

    private static void stripTrailingDigitsAfterDotForBottomField() {
        if (bottomFieldDisplayed.length() >= 18) {
            if (bottomFieldDisplayed.charAt(0) == '-') {
                bottomFieldDisplayed.setLength(18);
            } else {
                bottomFieldDisplayed.setLength(17);
            }
        }
        if (bottomFieldDisplayed.charAt(bottomFieldDisplayed.length() - 1) == '.') {
            bottomFieldDisplayed.setLength(bottomFieldDisplayed.length() - 1);
        }
    }

    private static String debugReportPrint() {
        return "<p class=\"debug\"><br> calculatorFirstLaunchedMode: " + calculatorFirstLaunchedMode +
                "<br> errorStatusMode: " + errorStatusMode +
                "<br> digitInputOnGoingMode: " + digitInputOnGoingMode +
                "<br> digitInputOnGoingAfterDotMode: " + digitInputOnGoingAfterDotMode +
                "<br> bottomFiledDigitIsNegativeMode: " + bottomFiledDigitIsNegativeMode +
                "<br> topFieldDisplayed: \"" + topFieldDisplayed + "\"" +
                "<br> bottomFieldDisplayed: \"" + bottomFieldDisplayed + "\"" +
                "<br> firstNumber: \"" + firstNumber + "\"" +
                "<br> secondNumber: \"" + secondNumber + "\"" +
                "<br> currentOperation: \"" + currentOperation + "\"" +
                "<br> lastPressedButton: \"" + lastPressedButton + "\"" +
                "<br> digitInputCount: \"" + digitInputCount + "\"</p>";
    }
}