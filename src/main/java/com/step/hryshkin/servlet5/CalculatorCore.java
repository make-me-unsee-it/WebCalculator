package com.step.hryshkin.servlet5;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class CalculatorCore {
    // РЕЖИМЫ РАБОТЫ СИСТЕМЫ
    private static boolean calculatorFirstLaunched = true;               // РЕЖИМ "ПЕРВЫЙ ЗАПУСК"
    private static boolean errorStatus = false;                          // РЕЖИМ "ОШИБКА КАЛЬКУЛЯТОРА"

    // РЕЖИМЫ ВВОДА ЦИФР
    private static boolean digitInputOnGoing = true;                     // ВВОД ЧИСЛА НАЧАТ
    private static boolean digitInputOnGoingAfterDot = false;            // ВВОД ПОСЛЕ ЗАПЯТОЙ
    private static boolean bottomFiledDigitIsNegative = false;           // МОДУЛЬ ОТРИЦАТЕЛЬНОГО ЧИСЛА

    // ВЫВОД НА ЭКРАН
    private static StringBuilder topField = new StringBuilder();         // ОТОБРАЖАЕМОЕ ВЕРХННЕ ПОЛЕ
    private static StringBuilder bottomField = new StringBuilder(" 0");  // ОТОБРАЖАЕМОЕ НИЖНЕЕ ПОЛЕ

    // ДАННЫЕ ВЫЧИСЛЕНИЙ
    private static BigDecimal firstNumber = BigDecimal.valueOf(0);       // ЗНАЧЕНИЕ ПЕРВОГО ОПЕРАНДА
    private static BigDecimal secondNumber = BigDecimal.valueOf(0);       // ЗНАЧЕНИЕ ВТОРОГО ОПЕРАНДА
    private static char currentOperation = 'n';                          // ЗНАЧЕНИЕ ОПЕРАТОРА
    private static char lastPressedButton = '\u0000';                    // ПОСЛЕДНЯЯ НАЖАТАЯ КНОПКА

    // СБРОС ПАРАМЕТРОВ КАЛЬКУЛЯТОРА ПО УМОЛЧАНИЮ
    private static void resetDefaults() {
        calculatorFirstLaunched = true;
        errorStatus = false;
        digitInputOnGoing = true;
        digitInputOnGoingAfterDot = false;
        bottomFiledDigitIsNegative = false;
        topField = new StringBuilder();
        bottomField = new StringBuilder(" 0");
        firstNumber = BigDecimal.valueOf(0);
        secondNumber = BigDecimal.valueOf(0);
        currentOperation = 'n';
        lastPressedButton = '\u0000';
    }

    // ВЫЗЫВАЕМЫЙ ИЗ КЛАССА-СЕРВЛЕТА МЕТОД ПОСЛЕДОВАТЕЛЬНОСТИ ВЫЧИСЛЕНИЙ
    protected static void processing(HttpServletRequest req) {
        // 1 ШАГ (НЕ ОБРАБАТЫВАЕТСЯ ПРИ ПЕРВОМ ЗАПУСКЕ КАЛЬКУЛЯТОРА ДО НАЖАТИЯ КНОПОК ИНТЕРФЕЙСА)
        if (!calculatorFirstLaunched) checkingForInput(req);

        // 2 ШАГ
        // БЛОК РАБОТАЕТ ПРИ АКТИВНОМ РЕЖИМЕ "ОШИБКА КАЛЬКУЛЯТОРА"
        if (errorStatus) errorModeSwitchOff();

        // 3 ШАГ
        // БЛОК ОБРАБОТКИ НАЖАТИЯ КНОПОК И ВЫЧИСЛЕНИЙ
        // НЕ ОБРАБАТЫВАЕТСЯ ПРИ ПЕРВОМ ЗАПУСКЕ И В РЕЖИМЕ "ОШИБКА КАЛЬКУЛЯТОРА"
        if (!calculatorFirstLaunched) actionsAndCalculations();

        // СБРОС ФЛАЖКА ПРОВЕРКИ РЕЖИМА "ПЕРВЫЙ ЗАПУСК"
        calculatorFirstLaunched = false;
    }

    // ВЫЗЫВАЕМЫЙ ИЗ КЛАССА-СЕРВЛЕТА МЕТОД ВЫВОДА ВЕБ-СТРАНИЦЫ В ФОРМАТЕ STRING
    protected static String contextPageToString() {
        String displayedPage = "";
        try {
            // В ОБЫЧНОМ РЕЖИМЕ
            if (!errorStatus) {
                displayedPage = String.format(ResourceReader
                        .webPageContentToString("calc_page_source.html"), topField, bottomField, debugReportPrint());

                // В РЕЖИМЕ "ОШИБКА КАЛЬКУЛЯТОРА"
            } else {
                displayedPage = String.format(ResourceReader
                        .webPageContentToString("calc_page_error_source.html"), topField, bottomField, debugReportPrint());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return displayedPage;
    }

    // МЕТОД: ПРОВЕРКА БЫЛА ЛИ НАЖАТА КНОПКА И КАКАЯ ИМЕННО
    private static void checkingForInput(HttpServletRequest req) {
        String input = req.getParameter("answer");
        // ЕСЛИ РЕКВЕСТ НЕ ПУСТОЙ (КНОПКА НАЖАТА) - РАСПОЗНАЕМ ЗНАЧЕНИЕ КНОПКИ
        if (input != null) lastPressedButton = receivedRequestProcessing(req.getParameter("answer"));
            // ЕСЛИ РЕКВЕСТ ПУСТОЙ (СТРАНИЦА БРАУЗЕРА ПЕРЕЗАГРУЖЕНА) - ОБНУЛЯЕМ ВСЕ ПАРАМЕТРЫ
        else resetDefaults();
    }

    // МЕТОД: ВЫХОДА ИЗ РЕЖИМА "ОШИБКА КАЛЬКУЛЯТОРА" ПО НАЖАТИЮ КЛАВИШИ CLEAR
    private static void errorModeSwitchOff() {
        // ПРИ НАЖАТИИ КНОПКИ "CLEAR" - ВСЕ ПАРАМЕТРЫ ОБНУЛЯЮТСЯ
        if (lastPressedButton == 'c') resetDefaults();

            // ПРИ НАЖАТИИ ЛЮБЫХ ДРУГИХ КНОПОК -
            // ВКЛЮЧАЕТСЯ РЕЖИМ "ПЕРВЫЙ ЗАПУСК", ЧТОБЫ ПРОПУСТИТЬ НИЖЕИДУЩИЙ БЛОК ВЫЧИСЛЕНИЙ
        else calculatorFirstLaunched = true;
    }

    // МЕТОД: ОБРАБОТКА НАЖАТИЯ КНОПОК И ВЫЧИСЛЕНИЯ
    private static void actionsAndCalculations() {
        // ВВОД ЦИФР 1-9
        if ((lastPressedButton == '1') | (lastPressedButton == '2') | (lastPressedButton == '3') |
                (lastPressedButton == '4') | (lastPressedButton == '5') | (lastPressedButton == '6') |
                (lastPressedButton == '7') | (lastPressedButton == '8') | (lastPressedButton == '9')) {
            // ЕСЛИ ВВОД ЧИСЛА НЕ НАЧАТ И ТЕКУЩАЯ ЦИФРА - ПЕРВАЯ
            if (!digitInputOnGoing) {
                bottomField = new StringBuilder(" ");                             // НУЖНА ОПТИМИЗАЦИЯ!
            }
            // ЗАЩИТА ОТ ОШИБКИ ВВОДА ЛИШНЕГО НУЛЯ В НАЧАЛЕ (" 0" > "09" > " 0")
            else if ((bottomField.charAt(1) == '0') & (bottomField.length() == 2)) {
                bottomField = new StringBuilder(" ");
            }
            bottomField.append(lastPressedButton);
            digitInputOnGoing = true;
            // ЗДЕСЬ СЛЕДУЕТ ДОБАВИТЬ ЗАЩИТУ ОТ ВЫХОДА ЗА ПРЕДЕЛЫ РАЗРЯДНОСТИ!!!!!

            // ВВОД ЦИФРЫ 0
        } else if (lastPressedButton == '0') {
            // ЕСЛИ ВВОД ЧИСЛА НЕ НАЧАТ
            if (!digitInputOnGoing) {
                bottomField = new StringBuilder(" 0");
                digitInputOnGoing = true;
                // ЕСЛИ ВВОД ЧИСЛА НАЧАТ И ПРОДОЛЖАЕТСЯ
            } else {
                // ЗАЩИТА ОТ ОШИБКИ ВВОДА НУЛЯ В НАЧАЛЕ (" 0" > "00" > " 0")
                if ((bottomField.length() == 2) & (bottomField.charAt(1) == '0')) {
                    digitInputOnGoing = true;
                    // ВВОД НУЛЯ В НАЧАЛЕ (" 1" > " 10")                          // НУЖНА ОПТИМИЗАЦИЯ!
                } else if ((bottomField.length() == 2) & (bottomField.charAt(1) != '0')) {
                    bottomField.append("0");
                    digitInputOnGoing = true;
                    // ВВОД НУЛЯ В ЛЮБОМ ДРУГОМ МЕСТЕ ("-999" > "-9990")
                } else if (bottomField.length() > 2) {
                    bottomField.append("0");
                    digitInputOnGoing = true;
                }
            }
            // ЗДЕСЬ СЛЕДУЕТ ДОБАВИТЬ ЗАЩИТУ ОТ ВЫХОДА ЗА ПРЕДЕЛЫ РАЗРЯДНОСТИ!!!!!

            // ВВОД ТОЧКИ
        } else if (lastPressedButton == '.') {
            if (!digitInputOnGoingAfterDot) {
                bottomField.append(".");
                digitInputOnGoing = true;
                digitInputOnGoingAfterDot = true;
            }

            // УДАЛЕНИЕ ПОСЛЕДНЕГО ВВЕДЕННОГО ЗНАКА
        } else if (lastPressedButton == 'b') {
            if (digitInputOnGoing) {
                int currentBottomFieldLength = bottomField.length();
                if (currentBottomFieldLength == 2) {
                    bottomField = new StringBuilder(" 0");
                    bottomFiledDigitIsNegative = false;
                    digitInputOnGoingAfterDot = false;
                } else if ((currentBottomFieldLength == 4) & (bottomField.charAt(1) == '0') & digitInputOnGoingAfterDot) {
                    bottomField = new StringBuilder(" 0");
                    bottomFiledDigitIsNegative = false;
                    digitInputOnGoingAfterDot = false;
                } else if ((currentBottomFieldLength == 3) & (bottomField.charAt(1) == '0') & digitInputOnGoingAfterDot) {
                    bottomField = new StringBuilder(" 0");
                    bottomFiledDigitIsNegative = false;
                    digitInputOnGoingAfterDot = false;
                } else if (bottomField.charAt(currentBottomFieldLength - 1) == '.') {
                    bottomField.setLength(currentBottomFieldLength - 1);
                    digitInputOnGoingAfterDot = false;
                } else bottomField.setLength(currentBottomFieldLength - 1);
            }

            // СБРОС ТЕКУЩЕГО ВВОДА
        } else if (lastPressedButton == 'e') {
            bottomField = new StringBuilder(" 0");
            firstNumber = BigDecimal.valueOf(0);
            digitInputOnGoingAfterDot = false;
            digitInputOnGoing = true;
            bottomFiledDigitIsNegative = false;

            // ОБЩИЙ СБРОС
        } else if (lastPressedButton == 'c') {
            bottomField = new StringBuilder(" 0");
            firstNumber = BigDecimal.valueOf(0);
            topField = new StringBuilder();
            firstNumber = BigDecimal.valueOf(0);
            digitInputOnGoingAfterDot = false;
            digitInputOnGoing = true;
            bottomFiledDigitIsNegative = false;
            currentOperation = 'n';

            // СМЕНА ЗНАКА ВВОДИМОГО ЧИСЛА
        } else if (lastPressedButton == '&') {
            if (digitInputOnGoing) {
                if ((bottomField.length() == 2) & (bottomField.charAt(1) != '0')) {
                    if (!bottomFiledDigitIsNegative) {
                        bottomField.setCharAt(0, '-');
                        bottomFiledDigitIsNegative = true;
                    } else {
                        bottomField.setCharAt(0, ' ');
                        bottomFiledDigitIsNegative = false;
                    }
                } else if (bottomField.length() >= 3) {
                    if (!bottomFiledDigitIsNegative) {
                        bottomField.setCharAt(0, '-');
                        bottomFiledDigitIsNegative = true;
                    } else {
                        bottomField.setCharAt(0, ' ');
                        bottomFiledDigitIsNegative = false;
                    }
                }
            }

            // ОСНОВНЫЕ ОПЕРАЦИИ
        } else if ((lastPressedButton == '+') | (lastPressedButton == '-')
                | (lastPressedButton == '*') | (lastPressedButton == '/')) {

            if (currentOperation == 'n') {
                topField.append(cleanInputBeforeOperation(bottomField));
                firstNumber = new BigDecimal(String.valueOf(topField));
                bottomField = new StringBuilder(topField);
                topField.append(lastPressedButton);
                secondNumber = firstNumber;
                digitInputOnGoingAfterDot = false; // ДОБАВЛЕНО В 17-13 20.06.2022 - ВРОДЕ БЫ РАБОТАЕТ. НАБЛЮДАТЬ.
            }
            if ((currentOperation != 'n') & (!digitInputOnGoing)) {
                topField.deleteCharAt(topField.length() - 1);
                topField.append(lastPressedButton);
            }
            if ((currentOperation != 'n') & (digitInputOnGoing)) {
                if (currentOperation == '+') {
                    bottomField = new StringBuilder(cleanInputBeforeOperation(bottomField));
                    secondNumber = new BigDecimal(String.valueOf(bottomField));

                    firstNumber = firstNumber.add(secondNumber);                // ЗДЕСЬ - УБИРАТЬ ХВОСТ ИЗ НУЛЕЙ ПОСЛЕ ТОЧКИ
                    secondNumber = firstNumber;
                    bottomField = new StringBuilder(secondNumber.toString());
                    topField = new StringBuilder(secondNumber.toString()).append(currentOperation);
                }
                if (currentOperation == '-') {
                    bottomField = new StringBuilder(cleanInputBeforeOperation(bottomField));
                    secondNumber = new BigDecimal(String.valueOf(bottomField));

                    firstNumber = firstNumber.subtract(secondNumber);
                    secondNumber = firstNumber;
                    bottomField = new StringBuilder(secondNumber.toString());
                    topField = new StringBuilder(secondNumber.toString()).append(currentOperation);
                }
                if (currentOperation == '/') {
                    bottomField = new StringBuilder(cleanInputBeforeOperation(bottomField));
                    secondNumber = new BigDecimal(String.valueOf(bottomField));

                    if (secondNumber.compareTo(new BigDecimal(0)) != 0) {
                        firstNumber = firstNumber
                                .divide(secondNumber, 16, RoundingMode.HALF_UP)
                                .stripTrailingZeros();

                        secondNumber = firstNumber;
                        bottomField = new StringBuilder(secondNumber.toString());
                        topField = new StringBuilder(secondNumber.toString()).append(currentOperation);
                    }
                    if (secondNumber.compareTo(new BigDecimal(0)) == 0) {
                        topField = new StringBuilder("Деление на ноль невозможно!");
                        bottomField = new StringBuilder("ОШИБКА");
                        errorStatus = true;
                    }

                }
                if (currentOperation == '*') {
                    bottomField = new StringBuilder(cleanInputBeforeOperation(bottomField));
                    secondNumber = new BigDecimal(String.valueOf(bottomField));
                    firstNumber = firstNumber.multiply(secondNumber);
                    secondNumber = firstNumber;
                    bottomField = new StringBuilder(secondNumber.toString());
                    topField = new StringBuilder(secondNumber.toString()).append(currentOperation);
                }
            }
            digitInputOnGoing = false;
            currentOperation = lastPressedButton;
        }
    }

    // МЕТОД: РАСПОЗНАЕМ ВВЕДЕННЫЕ ЗНАЧЕНИЯ
    private static char receivedRequestProcessing(String input) {
        switch (input) {
            case "СЕ":
                return 'e';            //clear entry
            case "С":
                return 'c';             //clear all
            case "⌫":
                return 'b';            //backspace
            case "√х":
                return 'r';            //square root
            case "%":
                return '%';             //percentage
            case "x²":
                return 's';            //square
            case "÷":
                return '/';             //division
            case "7":
                return '7';             //7
            case "8":
                return '8';             //8
            case "9":
                return '9';             //9
            case "×":
                return '*';             //multiplication
            case "4":
                return '4';             //4
            case "5":
                return '5';             //5
            case "6":
                return '6';             //6
            case "-":
                return '-';             //minus
            case "1":
                return '1';             //1
            case "2":
                return '2';             //2
            case "3":
                return '3';             //3
            case "+":
                return '+';             //plus
            case "±":
                return '&';             //sign change
            case "0":
                return '0';             //0
            case ",":
                return '.';             //dot
            default:
                return '=';             //result
        }
    }

    // МЕТОД: УБИРАЕМ ОШИБКИ ВВОДА (ХВОСТЫ НУЛЕЙ) ПРИ ПЕРЕДАЧЕ ВВЕДЕННОГО ПОЛЬЗОВАТЕЛЕМ ЧИСЛА В ОПЕРАНД
    private static String cleanInputBeforeOperation(StringBuilder input) {
        if (input.charAt(0) == ' ') input.deleteCharAt(0);
        // удаляем хвост из нулей после точки
        if (digitInputOnGoingAfterDot) {
            while ((input.length() > 2) && (input.charAt(input.length() - 1) == '0')) {
                input.setLength(input.length() - 1);
            }
        }
        if (input.charAt(input.length() - 1) == '.') input.deleteCharAt(input.length() - 1);
        if (((input.charAt(0) == '-') & (input.length() == 2)) && (input.charAt(1) == '0')) input.deleteCharAt(0);
        return input.toString();
    }

    // РЕЖИМ ОТЛАДКИ
    public static String debugReportPrint() {
        return "<p class=\"debug\"><br> calculatorFirstLaunched: " + calculatorFirstLaunched +
                "<br> errorStatus: " + errorStatus +
                "<br> digitInputOnGoing: " + digitInputOnGoing +
                "<br> digitInputOnGoingAfterDot: " + digitInputOnGoingAfterDot +
                "<br> bottomFiledDigitIsNegative: " + bottomFiledDigitIsNegative +
                "<br> bottomFiledDigitIsNegative: " + bottomFiledDigitIsNegative +
                "<br> topField: \"" + topField + "\"" +
                "<br> bottomField: \"" + bottomField + "\"" +
                "<br> firstNumber: \"" + firstNumber + "\"" +
                "<br> secondNumber: \"" + secondNumber + "\"" +
                "<br> currentOperation: \"" + currentOperation + "\"" +
                "<br> lastPressedButton: \"" + lastPressedButton + "\"</p>";
    }
}