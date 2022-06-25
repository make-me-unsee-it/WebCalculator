package com.step.hryshkin.servlet5;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class CalculatorCore {
    private CalculatorCore() {
    }

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
    private static BigDecimal secondNumber = BigDecimal.valueOf(0);      // ЗНАЧЕНИЕ ВТОРОГО ОПЕРАНДА
    private static char currentOperation = 'n';                          // ЗНАЧЕНИЕ ОПЕРАТОРА
    private static char lastPressedButton = '\u0000';                    // ПОСЛЕДНЯЯ НАЖАТАЯ КНОПКА
    private static int digitInputCount = 1;                              // СЧЕТЧИК ЗНАКОВ ВВОДИМОГО ЧИСЛА

    // СБРОС ПАРАМЕТРОВ КАЛЬКУЛЯТОРА ПО УМОЛЧАНИЮ
    private static void resetDefaultsAll() {
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
        digitInputCount = 1;
    }

    // СБРОС ПАРАМЕТРОВ ДЛЯ ТЕКУЩЕГО ВВОДА
    private static void resetDefaultsCurrent() {
        bottomField = new StringBuilder(" 0");
        bottomFiledDigitIsNegative = false;
        digitInputOnGoingAfterDot = false;
        digitInputOnGoing = true;
        digitInputCount = 1;
    }

    // ВЫЗЫВАЕМЫЙ ИЗ КЛАССА-СЕРВЛЕТА МЕТОД ПОСЛЕДОВАТЕЛЬНОСТИ ВЫЧИСЛЕНИЙ
    protected static void processing(HttpServletRequest req) {
        // 1 ШАГ (НЕ ОБРАБАТЫВАЕТСЯ ПРИ ПЕРВОМ ЗАПУСКЕ КАЛЬКУЛЯТОРА ДО НАЖАТИЯ КНОПОК ИНТЕРФЕЙСА)
        if (!calculatorFirstLaunched) checkingForInput(req);

        // 2 ШАГ ОБРАБОТКА РЕЖИМА "ОШИБКА КАЛЬКУЛЯТОРА"
        if (errorStatus) errorModeSwitchOff();

        // 3 ШАГ ВЫЧИСЛЕНИЯ. ИГНОРИРУЕТСЯ В РЕЖИМЕ "ПЕРВЫЙ ЗАПУСК" И "ОШИБКА КАЛЬКУЛЯТОРА"
        if (!calculatorFirstLaunched) actionsAndCalculations();

        // СБРОС ФЛАЖКА ПРОВЕРКИ РЕЖИМА "ПЕРВЫЙ ЗАПУСК"
        calculatorFirstLaunched = false;
    }

    // ВЫЗЫВАЕМЫЙ ИЗ КЛАССА-СЕРВЛЕТА МЕТОД ВЫВОДА ВЕБ-СТРАНИЦЫ В ФОРМАТЕ STRING
    protected static String contextPageToString() {
        String displayedPage = "";
        String resourcePath = "calc_page_source.html";
        if (errorStatus) resourcePath = "calc_page_error_source.html";
        try {
            displayedPage = String.format(ResourceReader
                    .webPageContentToString(resourcePath), topField, bottomField, debugReportPrint());
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
        else resetDefaultsAll();
    }

    // МЕТОД: ВЫХОДА ИЗ РЕЖИМА "ОШИБКА КАЛЬКУЛЯТОРА" ПО НАЖАТИЮ КЛАВИШИ CLEAR
    private static void errorModeSwitchOff() {
        // ПРИ НАЖАТИИ КНОПКИ "CLEAR" - ВСЕ ПАРАМЕТРЫ ОБНУЛЯЮТСЯ
        if (lastPressedButton == 'c') resetDefaultsAll();
            // ПРИ НАЖАТИИ ЛЮБЫХ ДРУГИХ КНОПОК -
            // ВКЛЮЧАЕТСЯ РЕЖИМ "ПЕРВЫЙ ЗАПУСК", ЧТОБЫ ПРОПУСТИТЬ НИЖЕИДУЩИЙ БЛОК ВЫЧИСЛЕНИЙ
        else calculatorFirstLaunched = true;
    }

    // МЕТОД: ОБРАБОТКА НАЖАТИЯ КНОПОК И ВЫЧИСЛЕНИЯ
    private static void actionsAndCalculations() {
        if ((lastPressedButton == '1') | (lastPressedButton == '2') | (lastPressedButton == '3') |
                (lastPressedButton == '4') | (lastPressedButton == '5') | (lastPressedButton == '6') |
                (lastPressedButton == '7') | (lastPressedButton == '8') | (lastPressedButton == '9')) {
            actionsOnClickDigits();

        } else if (lastPressedButton == '0') {
            actionsOnClickZero();

        } else if (lastPressedButton == '.') {
            actionsOnClickDot();

        } else if (lastPressedButton == 'b') {
            actionsOnClickBackspace();

        } else if (lastPressedButton == 'e') {
            resetDefaultsCurrent();

        } else if (lastPressedButton == 'c') {
            resetDefaultsAll();

        } else if (lastPressedButton == '&') {
            if (digitInputOnGoing) actionsOnClickSignChangeButton();

        } else if (lastPressedButton == '+') {
            actionsOnClickSumSubDivMulCommon();
            if ((currentOperation != 'n') & (digitInputOnGoing)) actionsOnClickSumSpecific();
            actionsOnClickSumSubDivMulCommonFinal();

        } else if (lastPressedButton == '-') {
            actionsOnClickSumSubDivMulCommon();
            if ((currentOperation != 'n') & (digitInputOnGoing)) actionsOnClickSubtractionSpecific();
            actionsOnClickSumSubDivMulCommonFinal();

        } else if (lastPressedButton == '/') {
            actionsOnClickSumSubDivMulCommon();
            if ((currentOperation != 'n') & (digitInputOnGoing)) actionsOnClickDivisionSpecific();
            actionsOnClickSumSubDivMulCommonFinal();

        } else if (lastPressedButton == '*') {
            actionsOnClickSumSubDivMulCommon();
            if ((currentOperation != 'n') & (digitInputOnGoing)) actionsOnClickMultiplicationSpecific();
            actionsOnClickSumSubDivMulCommonFinal();

            // ВЫПОЛНЕНИЕ ОПЕРАЦИЙ ПРИ НАЖАТИИ КНОПКИ "="
        } else if (lastPressedButton == '=') {
            actionsOnClickResultCommon();
            // ВЫПОЛНЕНИЕ АРИФМЕТИЧЕСКИХ ОПЕРАЦИЙ ПОСЛЕ ВЫБОРА ОПЕРАТОРА
            if (currentOperation != 'n') {
                if (currentOperation == '+') {
                    actionsOnClickResultSum();
                    actionsOnClickResultCommonFinal();

                } else if (currentOperation == '-') {
                    actionsOnClickResultSubtraction();
                    actionsOnClickResultCommonFinal();
                } else if (currentOperation == '/') {
                    actionsOnClickResultDivision();
                    actionsOnClickResultCommonFinal();
                } else if (currentOperation == '*') {
                    actionsOnClickResultMultiplication();
                    actionsOnClickResultCommonFinal();
                }
            }

            // ОТРАБОТКА ФУНКЦИИ КВАДРАТНОГО КОРНЯ ПРИ НАЖАТИИ КНОПКИ "x²"
        } else if (lastPressedButton == 's') {
            System.out.println("ПОКА НЕ ДОПИСАНО!");
        }
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////

    private static void actionsOnClickDigits() {
        // ЕСЛИ ВВОД ЧИСЛА НЕ НАЧАТ И ТЕКУЩАЯ ЦИФРА - ПЕРВАЯ
        if (!digitInputOnGoing) {
            bottomField = new StringBuilder(" ");
            digitInputCount = 0;
        }
        // ЗАЩИТА ОТ ОШИБКИ ВВОДА ЛИШНЕГО НУЛЯ В НАЧАЛЕ (" 0" > "09" > " 0")
        else if ((bottomField.charAt(1) == '0') & (bottomField.length() == 2)) {
            bottomField = new StringBuilder(" ");
            digitInputCount = 0;
        }
        // ВВОД ЦИФР С ЗАЩИТОЙ (МАКСИМУМ 16 ЦИФР)
        if (digitInputCount < 16) {
            bottomField.append(lastPressedButton);
            digitInputOnGoing = true;
            digitInputCount = digitInputCount + 1;
        }
    }

    private static void actionsOnClickZero() {
        // ЕСЛИ ВВОД ЧИСЛА НЕ НАЧАТ
        if (!digitInputOnGoing) {
            resetDefaultsCurrent();
            // ЕСЛИ ВВОД ЧИСЛА НАЧАТ И ПРОДОЛЖАЕТСЯ (ОГРАНИЧЕНИЕ - 16 ЦИФР)
        } else if (digitInputCount < 16) {
            // ЗАЩИТА ОТ ОШИБКИ ВВОДА НУЛЯ В НАЧАЛЕ (" 0" > "00" > " 0")
            if ((bottomField.length() == 2) & (bottomField.charAt(1) == '0')) {
                digitInputOnGoing = true;
                // ВВОД НУЛЯ В НАЧАЛЕ (" 1" > " 10")                          // НУЖНА ОПТИМИЗАЦИЯ!
            } else if ((bottomField.length() == 2) & (bottomField.charAt(1) != '0')) {
                bottomField.append("0");
                digitInputOnGoing = true;
                digitInputCount = digitInputCount + 1;
                // ВВОД НУЛЯ В ЛЮБОМ ДРУГОМ МЕСТЕ ("-999" > "-9990")
            } else if (bottomField.length() > 2) {
                bottomField.append("0");
                digitInputOnGoing = true;
                digitInputCount = digitInputCount + 1;
            }
        }
    }

    private static void actionsOnClickDot() {
        if (!digitInputOnGoingAfterDot) {
            bottomField.append(".");
            digitInputOnGoing = true;
            digitInputOnGoingAfterDot = true;
        }
    }

    private static void actionsOnClickBackspace() {
        if (digitInputOnGoing) {
            int currentBottomFieldLength = bottomField.length();
            if (currentBottomFieldLength == 2) {
                resetDefaultsCurrent();
            } else if ((currentBottomFieldLength == 4) & (bottomField.charAt(1) == '0') & digitInputOnGoingAfterDot) {
                resetDefaultsCurrent();
            } else if ((currentBottomFieldLength == 3) & (bottomField.charAt(1) == '0') & digitInputOnGoingAfterDot) {
                resetDefaultsCurrent();
            } else if (bottomField.charAt(currentBottomFieldLength - 1) == '.') {
                bottomField.setLength(currentBottomFieldLength - 1);
                digitInputOnGoingAfterDot = false;
            } else {
                bottomField.setLength(currentBottomFieldLength - 1);
                digitInputCount = digitInputCount - 1;
            }
        }
    }

    private static void actionsOnClickSignChangeButton() {
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

    private static void actionsOnClickSumSubDivMulCommon() {
        // ВЫБОР ОПЕРАТОРА ПОСЛЕ ВВОДА ПЕРВОГО ЧИСЛА
        if (currentOperation == 'n') {
            // ОБНОВЛЯЕМ ЗНАЧЕНИЕ ВЕРХНЕГО ПОЛЯ ПЕРВЫМ ВВЕДЕННЫМ ЧИСЛОМ С ПРОВЕРКОЙ КОРРЕКТНОСТИ ВВОДА
            // (НАПРИМЕР "0.450000" > "0.45")
            topField = new StringBuilder(cleanInputBeforeOperation(bottomField));
            // ФИКСИРУЕМ ПЕРВОЕ ЧИСЛО В ПАМЯТЬ ("0" > "45")
            firstNumber = new BigDecimal(String.valueOf(topField));
            // В НИЖНЕЕ ПОЛЕ ДУБЛИРУЕМ ЗНАЧЕНИЕ ВЕРХНЕГО
            // (ДЛЯ АВТОВВОДА ПРИ ПОСЛЕДУЮЩЕМ НАЖАТИИ "=" БЕЗ ВВОДА ВТОРОГО ЧИСЛА ДЛЯ ОПЕРАЦИИ ЧИСЛА С САМИМ СОБОЙ)
            bottomField = new StringBuilder(topField);
            // ТО ЖЕ САМОЕ ДЕЛАЕМ ДЛЯ ЧИСЕЛ
            secondNumber = firstNumber;
            // ОБНОВЛЯЕМ ЗНАЧЕНИЕ ВЕРХНЕГО ПОЛЯ ВВЕДЕННЫМ ОПЕРАНДОМ (НАПРИМЕР "0.45" > "0.45+")
            topField.append(lastPressedButton);
        }
        // ИЗМЕНЕНИЕ УЖЕ ВЫБРАННОГО ОПЕРАТОРА ДО ВВОДА ВТОРОГО ЧИСЛА
        if ((currentOperation != 'n') & (!digitInputOnGoing)) {
            topField = new StringBuilder(bottomField).append(lastPressedButton);
        }
    }

    private static void actionsOnClickSumSpecific() {
        bottomField = new StringBuilder(cleanInputBeforeOperation(bottomField));
        secondNumber = new BigDecimal(String.valueOf(bottomField));
        firstNumber = firstNumber.add(secondNumber).stripTrailingZeros();
        // ПРОВЕРЯЕМ НА ВЫХОД ЗА ПРЕДЕЛЫ СЧЕТА (10^16)
        outOfBoundsCheck(firstNumber);
        if (!errorStatus) {
            secondNumber = firstNumber;
            bottomField = new StringBuilder(secondNumber.toPlainString());
            // ОБРЕЗАЕМ ВЫВОДИМЫЙ РЕЗУЛЬТАТ ДО 16 ЗНАКОВ (10^16)
            stripTrailingDigitsAfterDotForBottomField();
            topField = new StringBuilder(secondNumber.toPlainString()).append(lastPressedButton);
        }
    }

    private static void actionsOnClickSubtractionSpecific() {
        bottomField = new StringBuilder(cleanInputBeforeOperation(bottomField));
        secondNumber = new BigDecimal(String.valueOf(bottomField));
        firstNumber = firstNumber.subtract(secondNumber).stripTrailingZeros();
        // ПРОВЕРЯЕМ НА ВЫХОД ЗА ПРЕДЕЛЫ СЧЕТА (10^16)
        outOfBoundsCheck(firstNumber);
        if (!errorStatus) {
            secondNumber = firstNumber;
            bottomField = new StringBuilder(secondNumber.toPlainString());
            // ОБРЕЗАЕМ ВЫВОДИМЫЙ РЕЗУЛЬТАТ ДО 16 ЗНАКОВ (10^16)
            stripTrailingDigitsAfterDotForBottomField();
            topField = new StringBuilder(secondNumber.toPlainString()).append(lastPressedButton);
        }
    }

    private static void actionsOnClickDivisionSpecific() {
        bottomField = new StringBuilder(cleanInputBeforeOperation(bottomField));
        secondNumber = new BigDecimal(String.valueOf(bottomField));
        // ЗАЩИТА ОТ ДЕЛЕНИЯ НА НОЛЬ - С ВЫЗОВОМ ОШИБКИ
        if (secondNumber.compareTo(new BigDecimal(0)) == 0) {
            actionOnDivisionByZero();
        } else {
            // ДЕЛИМ И СРЕЗАЕМ ХВОСТ ЗА 16 ЗНАКОМ ПОСЛЕ ТОЧКИ, А ТАКЖЕ ХВОСТ НУЛЕЙ
            firstNumber = firstNumber.divide(secondNumber, 16, RoundingMode.HALF_UP)
                    .stripTrailingZeros();
            // ПРОВЕРЯЕМ НА ВЫХОД ЗА ПРЕДЕЛЫ СЧЕТА (10^16)
            outOfBoundsCheck(firstNumber);
            if (!errorStatus) {
                secondNumber = firstNumber;
                bottomField = new StringBuilder(secondNumber.toPlainString());
                // ОБРЕЗАЕМ ВЫВОДИМЫЙ РЕЗУЛЬТАТ ДО 16 ЗНАКОВ (10^16)
                stripTrailingDigitsAfterDotForBottomField();
                topField = new StringBuilder(secondNumber.toPlainString()).append(lastPressedButton);
            }
        }
    }

    private static void actionsOnClickMultiplicationSpecific() {
        bottomField = new StringBuilder(cleanInputBeforeOperation(bottomField));
        secondNumber = new BigDecimal(String.valueOf(bottomField));
        firstNumber = firstNumber.multiply(secondNumber).stripTrailingZeros();
        // ПРОВЕРЯЕМ НА ВЫХОД ЗА ПРЕДЕЛЫ СЧЕТА (10^16)
        outOfBoundsCheck(firstNumber);
        if (!errorStatus) {
            secondNumber = firstNumber;
            bottomField = new StringBuilder(secondNumber.toPlainString());
            // ОБРЕЗАЕМ ВЫВОДИМЫЙ РЕЗУЛЬТАТ ДО 16 ЗНАКОВ (10^16)
            stripTrailingDigitsAfterDotForBottomField();
            topField = new StringBuilder(secondNumber.toPlainString()).append(lastPressedButton);
        }
    }

    private static void actionsOnClickSumSubDivMulCommonFinal() {
        digitInputOnGoing = false;
        digitInputOnGoingAfterDot = false;
        bottomFiledDigitIsNegative = false;
        digitInputCount = 0;
        currentOperation = lastPressedButton;
    }

    private static void actionsOnClickResultCommon() {
        // ПРИ НАЖАТИИ "=" ДО НАЖАТИЯ ОПЕРАТОРА (ПРИ ВВЕДЕННОМ ПЕРВОМ ЧИСЛЕ ЛИБО СО ЗНАЧЕНИЕМ "0")
        if (currentOperation == 'n') {
            // ОБНОВЛЯЕМ ЗНАЧЕНИЕ ВЕРХНЕГО ПОЛЯ ПЕРВЫМ ВВЕДЕННЫМ ЧИСЛОМ С ПРОВЕРКОЙ КОРРЕКТНОСТИ ВВОДА
            // (НАПРИМЕР "0.450000" > "0.45")
            topField = new StringBuilder(cleanInputBeforeOperation(bottomField));
            // ФИКСИРУЕМ ПЕРВОЕ ЧИСЛО В ПАМЯТЬ ("0" > "45")
            firstNumber = new BigDecimal(String.valueOf(topField));
            // В НИЖНЕЕ ПОЛЕ ДУБЛИРУЕМ ЗНАЧЕНИЕ ВЕРХНЕГО
            // (ДЛЯ АВТОВВОДА ПРИ ПОСЛЕДУЮЩЕМ НАЖАТИИ "=" БЕЗ ВВОДА ВТОРОГО ЧИСЛА ДЛЯ ОПЕРАЦИИ ЧИСЛА С САМИМ СОБОЙ)
            bottomField = new StringBuilder(topField);
            // ТО ЖЕ САМОЕ ДЕЛАЕМ ДЛЯ ЧИСЕЛ
            secondNumber = firstNumber;
            // ОБНОВЛЯЕМ ЗНАЧЕНИЕ ВЕРХНЕГО ПОЛЯ ВВЕДЕННЫМ ОПЕРАНДОМ (НАПРИМЕР "0.45" > "0.45+")
            topField.append(lastPressedButton);
        }
    }

    private static void actionsOnClickResultSum() {
        // ОТРИСОВЫВАЕМ ВЕРХНЕЕ ПОЛЕ: ВАРИАНТ, КОГДА БЫЛ НАЧАТ ВВОД ВТОРОГО ЧИСЛА
        if (digitInputOnGoing) {
            bottomField = new StringBuilder(cleanInputBeforeOperation(bottomField));
            topField.append(bottomField).append(lastPressedButton);
            secondNumber = new BigDecimal(String.valueOf(bottomField));
        }
        // ОТРИСОВЫВАЕМ ВЕРХНЕЕ ПОЛЕ: ВАРИАНТ, КОГДА ОТРАБАТЫВАЕТ ПОВТОР (НАЖАТО "=" ПОДРЯД)
        else {
            topField = new StringBuilder(bottomField)
                    .append(currentOperation)
                    .append(secondNumber)
                    .append(lastPressedButton);
        }
        // ПРОВЕРЯЕМ НА ВЫХОД ЗА ПРЕДЕЛЫ СЧЕТА (10^16)
        firstNumber = firstNumber.add(secondNumber).stripTrailingZeros();
        outOfBoundsCheck(firstNumber);
        // ВЫВОДИМ РЕЗУЛЬТАТ В НИЖНЕЕ ПОЛЕ
        if (!errorStatus) {
            bottomField = new StringBuilder(firstNumber.toPlainString());
            // ОБРЕЗАЕМ ВЫВОДИМЫЙ РЕЗУЛЬТАТ ДО 16 ЗНАКОВ (10^16)
            stripTrailingDigitsAfterDotForBottomField();
        }
    }

    private static void actionsOnClickResultSubtraction() {
        // ОТРИСОВЫВАЕМ ВЕРХНЕЕ ПОЛЕ: ВАРИАНТ, КОГДА БЫЛ НАЧАТ ВВОД ВТОРОГО ЧИСЛА
        if (digitInputOnGoing) {
            bottomField = new StringBuilder(cleanInputBeforeOperation(bottomField));
            topField.append(bottomField).append(lastPressedButton);
            secondNumber = new BigDecimal(String.valueOf(bottomField));
        }
        // ОТРИСОВЫВАЕМ ВЕРХНЕЕ ПОЛЕ: ВАРИАНТ, КОГДА ОТРАБАТЫВАЕТ ПОВТОР (НАЖАТО "=" ПОДРЯД)
        else {
            topField = new StringBuilder(bottomField)
                    .append(currentOperation)
                    .append(secondNumber)
                    .append(lastPressedButton);
        }
        // ПРОВЕРЯЕМ НА ВЫХОД ЗА ПРЕДЕЛЫ СЧЕТА (10^16)
        firstNumber = firstNumber.subtract(secondNumber).stripTrailingZeros();
        outOfBoundsCheck(firstNumber);
        // ВЫВОДИМ РЕЗУЛЬТАТ В НИЖНЕЕ ПОЛЕ
        if (!errorStatus) {
            bottomField = new StringBuilder(firstNumber.toPlainString());
            // ОБРЕЗАЕМ ВЫВОДИМЫЙ РЕЗУЛЬТАТ ДО 16 ЗНАКОВ (10^16)
            stripTrailingDigitsAfterDotForBottomField();
        }
    }

    private static void actionsOnClickResultDivision() {
        // ОТРИСОВЫВАЕМ ВЕРХНЕЕ ПОЛЕ: ВАРИАНТ, КОГДА БЫЛ НАЧАТ ВВОД ВТОРОГО ЧИСЛА
        if (digitInputOnGoing) {
            bottomField = new StringBuilder(cleanInputBeforeOperation(bottomField));
            topField.append(bottomField).append(lastPressedButton);
            secondNumber = new BigDecimal(String.valueOf(bottomField));
        }
        // ОТРИСОВЫВАЕМ ВЕРХНЕЕ ПОЛЕ: ВАРИАНТ, КОГДА ОТРАБАТЫВАЕТ ПОВТОР (НАЖАТО "=" ПОДРЯД)
        else {
            topField = new StringBuilder(bottomField)
                    .append(currentOperation)
                    .append(secondNumber)
                    .append(lastPressedButton);
        }
        // ЗАЩИТА ОТ ДЕЛЕНИЯ НА НОЛЬ - ВЫЗОВ РЕЖИМА ОШИБКИ
        if (secondNumber.compareTo(new BigDecimal(0)) == 0) {
            actionOnDivisionByZero();
        } else {
            // ДЕЛИМ И СРЕЗАЕМ ХВОСТ ЗА 16 ЗНАКОМ ПОСЛЕ ТОЧКИ, А ТАКЖЕ ХВОСТ НУЛЕЙ
            firstNumber = firstNumber.divide(secondNumber, 16, RoundingMode.HALF_UP)
                    .stripTrailingZeros();
            // ПРОВЕРЯЕМ РЕЗУЛЬТАТ НА ВЫХОД ЗА ПРЕДЕЛЫ СЧЕТА (10^16)
            outOfBoundsCheck(firstNumber);
            // ВЫВОДИМ РЕЗУЛЬТАТ В НИЖНЕЕ ПОЛЕ
            if (!errorStatus) {
                bottomField = new StringBuilder(firstNumber.toPlainString());
                // ОБРЕЗАЕМ ВЫВОДИМЫЙ РЕЗУЛЬТАТ ДО 16 ЗНАКОВ (10^16)
                stripTrailingDigitsAfterDotForBottomField();
            }
        }
    }

    private static void actionsOnClickResultMultiplication() {
        // ОТРИСОВЫВАЕМ ВЕРХНЕЕ ПОЛЕ: ВАРИАНТ, КОГДА БЫЛ НАЧАТ ВВОД ВТОРОГО ЧИСЛА
        if (digitInputOnGoing) {
            bottomField = new StringBuilder(cleanInputBeforeOperation(bottomField));
            topField.append(bottomField).append(lastPressedButton);
            secondNumber = new BigDecimal(String.valueOf(bottomField));
        }
        // ОТРИСОВЫВАЕМ ВЕРХНЕЕ ПОЛЕ: ВАРИАНТ, КОГДА ОТРАБАТЫВАЕТ ПОВТОР (НАЖАТО "=" ПОДРЯД)
        else {
            topField = new StringBuilder(bottomField)
                    .append(currentOperation)
                    .append(secondNumber)
                    .append(lastPressedButton);
        }
        // ПРОВЕРЯЕМ НА ВЫХОД ЗА ПРЕДЕЛЫ СЧЕТА (10^16)
        firstNumber = firstNumber.multiply(secondNumber).stripTrailingZeros();
        outOfBoundsCheck(firstNumber);
        // ВЫВОДИМ РЕЗУЛЬТАТ В НИЖНЕЕ ПОЛЕ
        if (!errorStatus) {
            bottomField = new StringBuilder(firstNumber.toPlainString());
            // ОБРЕЗАЕМ ВЫВОДИМЫЙ РЕЗУЛЬТАТ ДО 16 ЗНАКОВ (10^16)
            stripTrailingDigitsAfterDotForBottomField();
        }
    }

    private static void actionsOnClickResultCommonFinal() {
        // ПОСЛЕ НАЖАТИЯ КНОПКИ ОПЕРАЦИЙ ОТКЛЮЧАЕМ РЕЖИМ ВВОДА ЧИСЛА
        digitInputOnGoing = false;
        digitInputOnGoingAfterDot = false;
        bottomFiledDigitIsNegative = false;
        digitInputCount = 0;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////
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

    // СРЕЗАНИЕ ХВОСТА НУЛЕЙ ПОСЛЕ ТОЧКИ ПРИ ПЕРЕДАЧЕ ВВЕДЕННОГО ПОЛЬЗОВАТЕЛЕМ ЧИСЛА В ОПЕРАНД
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

    // ПРОВЕРКА ВЫХОДА ЗА ПРЕДЕЛЫ ВЫЧИСЛЕНИЙ (10^16) И ВЫЗОВ РЕЖИМА "ОШИБКА КАЛЬКУЛЯТОРА"
    private static void outOfBoundsCheck(BigDecimal number) {
        // БЕРЕМ МОДУЛЬ ПРОВЕРЯЕМОГО ЧИСЛА
        if (number.compareTo(new BigDecimal("0")) < 0) number = number.multiply(new BigDecimal(-1));
        // ПРОВЕРКА и ВКЛЮЧЕНИЕ РЕЖИМА ОШИБКИ
        if (number.compareTo(new BigDecimal("9999999999999999")) > 0) {
            topField = new StringBuilder("Ошибка. Выход за пределы счета");
            bottomField = new StringBuilder("9.999999999999999Е");
            errorStatus = true;
        }
    }

    // ВЫЗОВ И ОБРАБОТКА ОШИБКИ ПРИ ДЕЛЕНИИ НА НОЛЬ
    private static void actionOnDivisionByZero() {
        topField = new StringBuilder("Деление на ноль невозможно!");
        bottomField = new StringBuilder("ОШИБКА");
        errorStatus = true;
    }

    // СРЕЗАНИЕ ЛИШНИХ ЦИФР ПОСЛЕ АРИФМЕТИЧЕСКИХ ОПЕРАЦИЙ. ВСЕ РАБОТАЕТ >>>> РУКАМИ НЕ ТРОГАТЬ!
    private static void stripTrailingDigitsAfterDotForBottomField() {
        if (bottomField.length() >= 18) {
            if (bottomField.charAt(0) == '-') {
                bottomField.setLength(18);
            } else {
                bottomField.setLength(17);
            }
        }
        // ИЩЕМ И СРЕЗАЕМ ТОЧКУ НА КОНЦЕ
        if (bottomField.charAt(bottomField.length() - 1) == '.') {
            bottomField.setLength(bottomField.length() - 1);
        }
    }

    // РЕЖИМ ОТЛАДКИ
    private static String debugReportPrint() {
        return "<p class=\"debug\"><br> calculatorFirstLaunched: " + calculatorFirstLaunched +
                "<br> errorStatus: " + errorStatus +
                "<br> digitInputOnGoing: " + digitInputOnGoing +
                "<br> digitInputOnGoingAfterDot: " + digitInputOnGoingAfterDot +
                "<br> bottomFiledDigitIsNegative: " + bottomFiledDigitIsNegative +
                "<br> topField: \"" + topField + "\"" +
                "<br> bottomField: \"" + bottomField + "\"" +
                "<br> firstNumber: \"" + firstNumber + "\"" +
                "<br> secondNumber: \"" + secondNumber + "\"" +
                "<br> currentOperation: \"" + currentOperation + "\"" +
                "<br> lastPressedButton: \"" + lastPressedButton + "\"" +
                "<br> digitInputCount: \"" + digitInputCount + "\"</p>";
    }
}