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

    // ВЫЗЫВАЕМЫЙ ИЗ КЛАССА-СЕРВЛЕТА МЕТОД ПОСЛЕДОВАТЕЛЬНОСТИ ВЫЧИСЛЕНИЙ
    protected static void processing(HttpServletRequest req) {
        // 1 ШАГ (НЕ ОБРАБАТЫВАЕТСЯ ПРИ ПЕРВОМ ЗАПУСКЕ КАЛЬКУЛЯТОРА ДО НАЖАТИЯ КНОПОК ИНТЕРФЕЙСА)
        if (!calculatorFirstLaunchedMode) checkingForInput(req);

        // 2 ШАГ ОБРАБОТКА РЕЖИМА "ОШИБКА КАЛЬКУЛЯТОРА"
        if (errorStatusMode) errorModeSwitchOff();

        // 3 ШАГ ВЫЧИСЛЕНИЯ. ИГНОРИРУЕТСЯ В РЕЖИМЕ "ПЕРВЫЙ ЗАПУСК" И "ОШИБКА КАЛЬКУЛЯТОРА"
        if (!calculatorFirstLaunchedMode) actionsAndCalculations();

        // СБРОС ФЛАЖКА ПРОВЕРКИ РЕЖИМА "ПЕРВЫЙ ЗАПУСК"
        calculatorFirstLaunchedMode = false;
    }

    // ВЫЗЫВАЕМЫЙ ИЗ КЛАССА-СЕРВЛЕТА МЕТОД ВЫВОДА ВЕБ-СТРАНИЦЫ В ФОРМАТЕ STRING
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
        else calculatorFirstLaunchedMode = true;
    }

    // МЕТОД: ОБРАБОТКА НАЖАТИЯ КНОПОК И ВЫЧИСЛЕНИЯ
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
            case '=':
                actionsOnClickResultCommon();
                // ВЫПОЛНЕНИЕ АРИФМЕТИЧЕСКИХ ОПЕРАЦИЙ ПОСЛЕ ВЫБОРА ОПЕРАТОРА
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
            // ОТРАБОТКА ФУНКЦИИ КВАДРАТНОГО КОРНЯ ПРИ НАЖАТИИ КНОПКИ "x²"
            case 's':
                System.out.println("ПОКА НЕ ДОПИСАНО!");
                break;
        }
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////

    private static void actionsOnClickDigits() {
        // ПРОВЕРКА НАЧАЛА НОВОЙ ОПЕРАЦИИ ПОСЛЕ ЗАВЕРШЕНИЯ ПРЕДЫДУЩЕГО ВЫЧИСЛЕНИЯ
        if ((topFieldDisplayed.length() != 0) && (topFieldDisplayed.charAt(topFieldDisplayed.length() - 1) == '=')) {
            resetDefaultsWhenStartingNewOperation();
        }

        // ЕСЛИ ВВОД ЧИСЛА НЕ НАЧАТ И ТЕКУЩАЯ ЦИФРА - ПЕРВАЯ
        if (!digitInputOnGoingMode) {
            bottomFieldDisplayed = new StringBuilder(" ");
            digitInputCount = 0;
        }
        // ЗАЩИТА ОТ ОШИБКИ ВВОДА ЛИШНЕГО НУЛЯ В НАЧАЛЕ (" 0" > "09" > " 0")
        else if ((bottomFieldDisplayed.charAt(1) == '0') & (bottomFieldDisplayed.length() == 2)) {
            bottomFieldDisplayed = new StringBuilder(" ");
            digitInputCount = 0;
        }
        // ВВОД ЦИФР С ЗАЩИТОЙ (МАКСИМУМ 16 ЦИФР)
        if (digitInputCount < 16) {
            bottomFieldDisplayed.append(lastPressedButton);
            digitInputOnGoingMode = true;
            digitInputCount = digitInputCount + 1;
        }
    }

    private static void actionsOnClickZero() {
        // ЕСЛИ ВВОД ЧИСЛА НЕ НАЧАТ
        if (!digitInputOnGoingMode) {
            resetDefaultsCurrent();
            // ЕСЛИ ВВОД ЧИСЛА НАЧАТ И ПРОДОЛЖАЕТСЯ (ОГРАНИЧЕНИЕ - 16 ЦИФР)
        } else if (digitInputCount < 16) {
            // ЗАЩИТА ОТ ОШИБКИ ВВОДА НУЛЯ В НАЧАЛЕ (" 0" > "00" > " 0")
            if ((bottomFieldDisplayed.length() == 2) & (bottomFieldDisplayed.charAt(1) == '0')) {
                digitInputOnGoingMode = true;
                // ВВОД НУЛЯ В НАЧАЛЕ (" 1" > " 10")                          // НУЖНА ОПТИМИЗАЦИЯ!
            } else if ((bottomFieldDisplayed.length() == 2) & (bottomFieldDisplayed.charAt(1) != '0')) {
                bottomFieldDisplayed.append("0");
                digitInputOnGoingMode = true;
                digitInputCount = digitInputCount + 1;
                // ВВОД НУЛЯ В ЛЮБОМ ДРУГОМ МЕСТЕ ("-999" > "-9990")
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
        // ВЫБОР ОПЕРАТОРА ПОСЛЕ ВВОДА ПЕРВОГО ЧИСЛА
        if (currentOperation == 'n') {
            // ОБНОВЛЯЕМ ЗНАЧЕНИЕ ВЕРХНЕГО ПОЛЯ ПЕРВЫМ ВВЕДЕННЫМ ЧИСЛОМ С ПРОВЕРКОЙ КОРРЕКТНОСТИ ВВОДА
            // (НАПРИМЕР "0.450000" > "0.45")
            topFieldDisplayed = new StringBuilder(cleanInputBeforeOperation(bottomFieldDisplayed));
            // ФИКСИРУЕМ ПЕРВОЕ ЧИСЛО В ПАМЯТЬ ("0" > "45")
            firstNumber = new BigDecimal(String.valueOf(topFieldDisplayed));
            // В НИЖНЕЕ ПОЛЕ ДУБЛИРУЕМ ЗНАЧЕНИЕ ВЕРХНЕГО
            // (ДЛЯ АВТОВВОДА ПРИ ПОСЛЕДУЮЩЕМ НАЖАТИИ "=" БЕЗ ВВОДА ВТОРОГО ЧИСЛА ДЛЯ ОПЕРАЦИИ ЧИСЛА С САМИМ СОБОЙ)
            bottomFieldDisplayed = new StringBuilder(topFieldDisplayed);
            // ТО ЖЕ САМОЕ ДЕЛАЕМ ДЛЯ ЧИСЕЛ
            secondNumber = firstNumber;
            // ОБНОВЛЯЕМ ЗНАЧЕНИЕ ВЕРХНЕГО ПОЛЯ ВВЕДЕННЫМ ОПЕРАНДОМ (НАПРИМЕР "0.45" > "0.45+")
            topFieldDisplayed.append(lastPressedButton);
        }
        // ИЗМЕНЕНИЕ УЖЕ ВЫБРАННОГО ОПЕРАТОРА ДО ВВОДА ВТОРОГО ЧИСЛА
        if ((currentOperation != 'n') & (!digitInputOnGoingMode)) {
            topFieldDisplayed = new StringBuilder(bottomFieldDisplayed).append(lastPressedButton);
        }
    }

    private static void actionsOnClickSumSpecific() {
        bottomFieldDisplayed = new StringBuilder(cleanInputBeforeOperation(bottomFieldDisplayed));
        secondNumber = new BigDecimal(String.valueOf(bottomFieldDisplayed));
        firstNumber = firstNumber.add(secondNumber).stripTrailingZeros();
        // ПРОВЕРЯЕМ НА ВЫХОД ЗА ПРЕДЕЛЫ СЧЕТА (10^16)
        outOfBoundsCheck(firstNumber);
        if (!errorStatusMode) {
            secondNumber = firstNumber;
            bottomFieldDisplayed = new StringBuilder(secondNumber.toPlainString());
            // ОБРЕЗАЕМ ВЫВОДИМЫЙ РЕЗУЛЬТАТ ДО 16 ЗНАКОВ (10^16)
            stripTrailingDigitsAfterDotForBottomField();
            topFieldDisplayed = new StringBuilder(secondNumber.toPlainString()).append(lastPressedButton);
        }
    }

    private static void actionsOnClickSubtractionSpecific() {
        bottomFieldDisplayed = new StringBuilder(cleanInputBeforeOperation(bottomFieldDisplayed));
        secondNumber = new BigDecimal(String.valueOf(bottomFieldDisplayed));
        firstNumber = firstNumber.subtract(secondNumber).stripTrailingZeros();
        // ПРОВЕРЯЕМ НА ВЫХОД ЗА ПРЕДЕЛЫ СЧЕТА (10^16)
        outOfBoundsCheck(firstNumber);
        if (!errorStatusMode) {
            secondNumber = firstNumber;
            bottomFieldDisplayed = new StringBuilder(secondNumber.toPlainString());
            // ОБРЕЗАЕМ ВЫВОДИМЫЙ РЕЗУЛЬТАТ ДО 16 ЗНАКОВ (10^16)
            stripTrailingDigitsAfterDotForBottomField();
            topFieldDisplayed = new StringBuilder(secondNumber.toPlainString()).append(lastPressedButton);
        }
    }

    private static void actionsOnClickDivisionSpecific() {
        bottomFieldDisplayed = new StringBuilder(cleanInputBeforeOperation(bottomFieldDisplayed));
        secondNumber = new BigDecimal(String.valueOf(bottomFieldDisplayed));
        // ЗАЩИТА ОТ ДЕЛЕНИЯ НА НОЛЬ - С ВЫЗОВОМ ОШИБКИ
        if (secondNumber.compareTo(new BigDecimal(0)) == 0) {
            actionOnDivisionByZero();
        } else {
            // ДЕЛИМ И СРЕЗАЕМ ХВОСТ ЗА 16 ЗНАКОМ ПОСЛЕ ТОЧКИ, А ТАКЖЕ ХВОСТ НУЛЕЙ
            firstNumber = firstNumber.divide(secondNumber, 16, RoundingMode.HALF_UP)
                    .stripTrailingZeros();
            // ПРОВЕРЯЕМ НА ВЫХОД ЗА ПРЕДЕЛЫ СЧЕТА (10^16)
            outOfBoundsCheck(firstNumber);
            if (!errorStatusMode) {
                secondNumber = firstNumber;
                bottomFieldDisplayed = new StringBuilder(secondNumber.toPlainString());
                // ОБРЕЗАЕМ ВЫВОДИМЫЙ РЕЗУЛЬТАТ ДО 16 ЗНАКОВ (10^16)
                stripTrailingDigitsAfterDotForBottomField();
                topFieldDisplayed = new StringBuilder(secondNumber.toPlainString()).append(lastPressedButton);
            }
        }
    }

    private static void actionsOnClickMultiplicationSpecific() {
        bottomFieldDisplayed = new StringBuilder(cleanInputBeforeOperation(bottomFieldDisplayed));
        secondNumber = new BigDecimal(String.valueOf(bottomFieldDisplayed));
        firstNumber = firstNumber.multiply(secondNumber).stripTrailingZeros();
        // ПРОВЕРЯЕМ НА ВЫХОД ЗА ПРЕДЕЛЫ СЧЕТА (10^16)
        outOfBoundsCheck(firstNumber);
        if (!errorStatusMode) {
            secondNumber = firstNumber;
            bottomFieldDisplayed = new StringBuilder(secondNumber.toPlainString());
            // ОБРЕЗАЕМ ВЫВОДИМЫЙ РЕЗУЛЬТАТ ДО 16 ЗНАКОВ (10^16)
            stripTrailingDigitsAfterDotForBottomField();
            topFieldDisplayed = new StringBuilder(secondNumber.toPlainString()).append(lastPressedButton);
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
        // ПРИ НАЖАТИИ "=" ДО НАЖАТИЯ ОПЕРАТОРА (ПРИ ВВЕДЕННОМ ПЕРВОМ ЧИСЛЕ ЛИБО СО ЗНАЧЕНИЕМ "0")
        if (currentOperation == 'n') {
            // ОБНОВЛЯЕМ ЗНАЧЕНИЕ ВЕРХНЕГО ПОЛЯ ПЕРВЫМ ВВЕДЕННЫМ ЧИСЛОМ С ПРОВЕРКОЙ КОРРЕКТНОСТИ ВВОДА
            // (НАПРИМЕР "0.450000" > "0.45")
            topFieldDisplayed = new StringBuilder(cleanInputBeforeOperation(bottomFieldDisplayed));
            // ФИКСИРУЕМ ПЕРВОЕ ЧИСЛО В ПАМЯТЬ ("0" > "45")
            firstNumber = new BigDecimal(String.valueOf(topFieldDisplayed));
            // В НИЖНЕЕ ПОЛЕ ДУБЛИРУЕМ ЗНАЧЕНИЕ ВЕРХНЕГО
            // (ДЛЯ АВТОВВОДА ПРИ ПОСЛЕДУЮЩЕМ НАЖАТИИ "=" БЕЗ ВВОДА ВТОРОГО ЧИСЛА ДЛЯ ОПЕРАЦИИ ЧИСЛА С САМИМ СОБОЙ)
            bottomFieldDisplayed = new StringBuilder(topFieldDisplayed);
            // ТО ЖЕ САМОЕ ДЕЛАЕМ ДЛЯ ЧИСЕЛ
            secondNumber = firstNumber;
            // ОБНОВЛЯЕМ ЗНАЧЕНИЕ ВЕРХНЕГО ПОЛЯ ВВЕДЕННЫМ ОПЕРАНДОМ (НАПРИМЕР "0.45" > "0.45+")
            topFieldDisplayed.append(lastPressedButton);
        }
    }

    private static void actionsOnClickResultSum() {
        // ОТРИСОВЫВАЕМ ВЕРХНЕЕ ПОЛЕ: ВАРИАНТ, КОГДА БЫЛ НАЧАТ ВВОД ВТОРОГО ЧИСЛА
        if (digitInputOnGoingMode) {
            bottomFieldDisplayed = new StringBuilder(cleanInputBeforeOperation(bottomFieldDisplayed));
            topFieldDisplayed.append(bottomFieldDisplayed).append(lastPressedButton);
            secondNumber = new BigDecimal(String.valueOf(bottomFieldDisplayed));
        }
        // ОТРИСОВЫВАЕМ ВЕРХНЕЕ ПОЛЕ: ВАРИАНТ, КОГДА ОТРАБАТЫВАЕТ ПОВТОР (НАЖАТО "=" ПОДРЯД)
        else {
            topFieldDisplayed = new StringBuilder(bottomFieldDisplayed)
                    .append(currentOperation)
                    .append(secondNumber)
                    .append(lastPressedButton);
        }
        // ПРОВЕРЯЕМ НА ВЫХОД ЗА ПРЕДЕЛЫ СЧЕТА (10^16)
        firstNumber = firstNumber.add(secondNumber).stripTrailingZeros();
        outOfBoundsCheck(firstNumber);
        // ВЫВОДИМ РЕЗУЛЬТАТ В НИЖНЕЕ ПОЛЕ
        if (!errorStatusMode) {
            bottomFieldDisplayed = new StringBuilder(firstNumber.toPlainString());
            // ОБРЕЗАЕМ ВЫВОДИМЫЙ РЕЗУЛЬТАТ ДО 16 ЗНАКОВ (10^16)
            stripTrailingDigitsAfterDotForBottomField();
        }
    }

    private static void actionsOnClickResultSubtraction() {
        // ОТРИСОВЫВАЕМ ВЕРХНЕЕ ПОЛЕ: ВАРИАНТ, КОГДА БЫЛ НАЧАТ ВВОД ВТОРОГО ЧИСЛА
        if (digitInputOnGoingMode) {
            bottomFieldDisplayed = new StringBuilder(cleanInputBeforeOperation(bottomFieldDisplayed));
            topFieldDisplayed.append(bottomFieldDisplayed).append(lastPressedButton);
            secondNumber = new BigDecimal(String.valueOf(bottomFieldDisplayed));
        }
        // ОТРИСОВЫВАЕМ ВЕРХНЕЕ ПОЛЕ: ВАРИАНТ, КОГДА ОТРАБАТЫВАЕТ ПОВТОР (НАЖАТО "=" ПОДРЯД)
        else {
            topFieldDisplayed = new StringBuilder(bottomFieldDisplayed)
                    .append(currentOperation)
                    .append(secondNumber)
                    .append(lastPressedButton);
        }
        // ПРОВЕРЯЕМ НА ВЫХОД ЗА ПРЕДЕЛЫ СЧЕТА (10^16)
        firstNumber = firstNumber.subtract(secondNumber).stripTrailingZeros();
        outOfBoundsCheck(firstNumber);
        // ВЫВОДИМ РЕЗУЛЬТАТ В НИЖНЕЕ ПОЛЕ
        if (!errorStatusMode) {
            bottomFieldDisplayed = new StringBuilder(firstNumber.toPlainString());
            // ОБРЕЗАЕМ ВЫВОДИМЫЙ РЕЗУЛЬТАТ ДО 16 ЗНАКОВ (10^16)
            stripTrailingDigitsAfterDotForBottomField();
        }
    }

    private static void actionsOnClickResultDivision() {
        // ОТРИСОВЫВАЕМ ВЕРХНЕЕ ПОЛЕ: ВАРИАНТ, КОГДА БЫЛ НАЧАТ ВВОД ВТОРОГО ЧИСЛА
        if (digitInputOnGoingMode) {
            bottomFieldDisplayed = new StringBuilder(cleanInputBeforeOperation(bottomFieldDisplayed));
            topFieldDisplayed.append(bottomFieldDisplayed).append(lastPressedButton);
            secondNumber = new BigDecimal(String.valueOf(bottomFieldDisplayed));
        }
        // ОТРИСОВЫВАЕМ ВЕРХНЕЕ ПОЛЕ: ВАРИАНТ, КОГДА ОТРАБАТЫВАЕТ ПОВТОР (НАЖАТО "=" ПОДРЯД)
        else {
            topFieldDisplayed = new StringBuilder(bottomFieldDisplayed)
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
            if (!errorStatusMode) {
                bottomFieldDisplayed = new StringBuilder(firstNumber.toPlainString());
                // ОБРЕЗАЕМ ВЫВОДИМЫЙ РЕЗУЛЬТАТ ДО 16 ЗНАКОВ (10^16)
                stripTrailingDigitsAfterDotForBottomField();
            }
        }
    }

    private static void actionsOnClickResultMultiplication() {
        // ОТРИСОВЫВАЕМ ВЕРХНЕЕ ПОЛЕ: ВАРИАНТ, КОГДА БЫЛ НАЧАТ ВВОД ВТОРОГО ЧИСЛА
        if (digitInputOnGoingMode) {
            bottomFieldDisplayed = new StringBuilder(cleanInputBeforeOperation(bottomFieldDisplayed));
            topFieldDisplayed.append(bottomFieldDisplayed).append(lastPressedButton);
            secondNumber = new BigDecimal(String.valueOf(bottomFieldDisplayed));
        }
        // ОТРИСОВЫВАЕМ ВЕРХНЕЕ ПОЛЕ: ВАРИАНТ, КОГДА ОТРАБАТЫВАЕТ ПОВТОР (НАЖАТО "=" ПОДРЯД)
        else {
            topFieldDisplayed = new StringBuilder(bottomFieldDisplayed)
                    .append(currentOperation)
                    .append(secondNumber)
                    .append(lastPressedButton);
        }
        // ПРОВЕРЯЕМ НА ВЫХОД ЗА ПРЕДЕЛЫ СЧЕТА (10^16)
        firstNumber = firstNumber.multiply(secondNumber).stripTrailingZeros();
        outOfBoundsCheck(firstNumber);
        // ВЫВОДИМ РЕЗУЛЬТАТ В НИЖНЕЕ ПОЛЕ
        if (!errorStatusMode) {
            bottomFieldDisplayed = new StringBuilder(firstNumber.toPlainString());
            // ОБРЕЗАЕМ ВЫВОДИМЫЙ РЕЗУЛЬТАТ ДО 16 ЗНАКОВ (10^16)
            stripTrailingDigitsAfterDotForBottomField();
        }
    }

    private static void actionsOnClickResultCommonFinal() {
        // ПОСЛЕ НАЖАТИЯ КНОПКИ ОПЕРАЦИЙ ОТКЛЮЧАЕМ РЕЖИМ ВВОДА ЧИСЛА
        digitInputOnGoingMode = false;
        digitInputOnGoingAfterDotMode = false;
        bottomFiledDigitIsNegativeMode = false;
        digitInputCount = 0;
    }

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
        if (digitInputOnGoingAfterDotMode) {
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
            topFieldDisplayed = new StringBuilder("Ошибка. Выход за пределы счета");
            bottomFieldDisplayed = new StringBuilder("9.999999999999999Е");
            errorStatusMode = true;
        }
    }

    // ВЫЗОВ И ОБРАБОТКА ОШИБКИ ПРИ ДЕЛЕНИИ НА НОЛЬ
    private static void actionOnDivisionByZero() {
        topFieldDisplayed = new StringBuilder("Деление на ноль невозможно!");
        bottomFieldDisplayed = new StringBuilder("ОШИБКА");
        errorStatusMode = true;
    }

    // СРЕЗАНИЕ ЛИШНИХ ЦИФР ПОСЛЕ АРИФМЕТИЧЕСКИХ ОПЕРАЦИЙ. ВСЕ РАБОТАЕТ >>>> РУКАМИ НЕ ТРОГАТЬ!
    private static void stripTrailingDigitsAfterDotForBottomField() {
        if (bottomFieldDisplayed.length() >= 18) {
            if (bottomFieldDisplayed.charAt(0) == '-') {
                bottomFieldDisplayed.setLength(18);
            } else {
                bottomFieldDisplayed.setLength(17);
            }
        }
        // ИЩЕМ И СРЕЗАЕМ ТОЧКУ НА КОНЦЕ
        if (bottomFieldDisplayed.charAt(bottomFieldDisplayed.length() - 1) == '.') {
            bottomFieldDisplayed.setLength(bottomFieldDisplayed.length() - 1);
        }
    }

    // РЕЖИМ ОТЛАДКИ
    private static String debugReportPrint() {
        return "<p class=\"debug\"><br> calculatorFirstLaunched: " + calculatorFirstLaunchedMode +
                "<br> errorStatus: " + errorStatusMode +
                "<br> digitInputOnGoing: " + digitInputOnGoingMode +
                "<br> digitInputOnGoingAfterDot: " + digitInputOnGoingAfterDotMode +
                "<br> bottomFiledDigitIsNegative: " + bottomFiledDigitIsNegativeMode +
                "<br> topField: \"" + topFieldDisplayed + "\"" +
                "<br> bottomField: \"" + bottomFieldDisplayed + "\"" +
                "<br> firstNumber: \"" + firstNumber + "\"" +
                "<br> secondNumber: \"" + secondNumber + "\"" +
                "<br> currentOperation: \"" + currentOperation + "\"" +
                "<br> lastPressedButton: \"" + lastPressedButton + "\"" +
                "<br> digitInputCount: \"" + digitInputCount + "\"</p>";
    }
}