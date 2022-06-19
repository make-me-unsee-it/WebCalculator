package com.step.hryshkin.servlet5;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(urlPatterns = {"/calculator"})
public class CalculatorPage extends HttpServlet {
    public static StringBuilder topField = new StringBuilder("");
    public static double topFieldDouble = 0;
    public static StringBuilder bottomField = new StringBuilder(" 0");
    public static double bottomFieldDouble = 0;
    public static boolean calculatorFirstLaunched = true;
    public static boolean digitInputOnGoing = true;
    public static boolean digitInputOnGoingAfterDot = false;
    public static boolean bottomFiledDigitIsNegative = false;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html");
        resp.setCharacterEncoding("UTF-8");

        //распознать, какая кнопка нажата
        char lastPressedButton = '0';

        if (!calculatorFirstLaunched) {         // ВСЯ ЛОГИКА В ЭТОМ БЛОКЕ/////////////////////////
            lastPressedButton = inputRecognition(req.getParameter("answer")); //нажата кнопка

            if (lastPressedButton == '1') {
                if (!digitInputOnGoing) bottomField = new StringBuilder(" ");
                if ((bottomField.charAt(1) == '0') & (bottomField.length() == 2)) bottomField = new StringBuilder(" ");
                    bottomField.append("1");
                digitInputOnGoing = true;

            } else if (lastPressedButton == '2') {
                if (!digitInputOnGoing) bottomField = new StringBuilder(" ");
                if ((bottomField.charAt(1) == '0') & (bottomField.length() == 2)) bottomField = new StringBuilder(" ");
                bottomField.append("2");
                digitInputOnGoing = true;

            } else if (lastPressedButton == '3') {
                if (!digitInputOnGoing) bottomField = new StringBuilder(" ");
                if ((bottomField.charAt(1) == '0') & (bottomField.length() == 2)) bottomField = new StringBuilder(" ");
                bottomField.append("3");
                digitInputOnGoing = true;

            } else if (lastPressedButton == '4') {
                if (!digitInputOnGoing) bottomField = new StringBuilder(" ");
                if ((bottomField.charAt(1) == '0') & (bottomField.length() == 2)) bottomField = new StringBuilder(" ");
                bottomField.append("4");
                digitInputOnGoing = true;

            } else if (lastPressedButton == '5') {
                if (!digitInputOnGoing) bottomField = new StringBuilder(" ");
                if ((bottomField.charAt(1) == '0') & (bottomField.length() == 2)) bottomField = new StringBuilder(" ");
                bottomField.append("5");
                digitInputOnGoing = true;

            } else if (lastPressedButton == '6') {
                if (!digitInputOnGoing) bottomField = new StringBuilder(" ");
                if ((bottomField.charAt(1) == '0') & (bottomField.length() == 2)) bottomField = new StringBuilder(" ");
                bottomField.append("6");
                digitInputOnGoing = true;

            } else if (lastPressedButton == '7') {
                if (!digitInputOnGoing) bottomField = new StringBuilder(" ");
                if ((bottomField.charAt(1) == '0') & (bottomField.length() == 2)) bottomField = new StringBuilder(" ");
                bottomField.append("7");
                digitInputOnGoing = true;

            } else if (lastPressedButton == '8') {
                if (!digitInputOnGoing) bottomField = new StringBuilder(" ");
                if ((bottomField.charAt(1) == '0') & (bottomField.length() == 2)) bottomField = new StringBuilder(" ");
                bottomField.append("8");
                digitInputOnGoing = true;

            } else if (lastPressedButton == '9') {
                if (!digitInputOnGoing) bottomField = new StringBuilder(" ");
                if ((bottomField.charAt(1) == '0') & (bottomField.length() == 2)) bottomField = new StringBuilder(" ");
                bottomField.append("9");
                digitInputOnGoing = true;

            } else if (lastPressedButton == '0') {
                if (!digitInputOnGoing) bottomField = new StringBuilder(" 0");
                else {
                    if ((bottomField.length() != 2) & (bottomField.charAt(1) != '0')) {
                        bottomField.append("0");
                        digitInputOnGoing = true;
                    }
                }

            } else if (lastPressedButton == '.') {
                if (!digitInputOnGoingAfterDot) {
                    bottomField.append(".");
                    digitInputOnGoing = true;
                    digitInputOnGoingAfterDot = true;
                }

            } else if (lastPressedButton == 'b') {
                if (digitInputOnGoing) {
                    int currentBottomFieldLength = bottomField.length();

                    if (currentBottomFieldLength == 2) {
                        bottomField = new StringBuilder(" 0");
                        bottomFiledDigitIsNegative = false;
                    } else if (bottomField.charAt(currentBottomFieldLength - 1) == '.') {
                        bottomField.setLength(currentBottomFieldLength - 1);
                        digitInputOnGoingAfterDot = false;
                    } else bottomField.setLength(currentBottomFieldLength - 1);
                }

            } else if (lastPressedButton == 'e') {
                bottomField = new StringBuilder(" 0");
                bottomFieldDouble = 0;
                digitInputOnGoingAfterDot = false;
                digitInputOnGoing = true;
                bottomFiledDigitIsNegative = false;

            } else if (lastPressedButton == 'c') {
                bottomField = new StringBuilder(" 0");
                bottomFieldDouble = 0;
                topField = new StringBuilder("");
                topFieldDouble = 0;
                digitInputOnGoingAfterDot = false;
                digitInputOnGoing = true;
                bottomFiledDigitIsNegative = false;
            }


        } // ВСЯ ЛОГИКА В ЭТОМ БЛОКЕ///////////////////////////////////////////////////////////////////////////

        calculatorFirstLaunched = false;

        resp.getWriter().println("<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <title>Калькултор на сервлете</title>\n" +
                "    <link rel='stylesheet' href='https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css'>\n" +
                "    <style type=\"text/css\">\n" +
                "\n" +
                "body { background: gray; }\n" +
                "\n" +
                "table.top { background: white; margin-left: auto; margin-right: auto;}\n" +
                "\n" +
                "table.bottom { background: #DCDCDC; margin-left: auto; margin-right: auto; }\n" +
                "\n" +
                "td.header {\n" +
                "font-family: Monospace; family-name: Andele Mono; font-size: small;\n" +
                "background: gray;\n" +
                "color: white;\n" +
                "text-align: left;\n" +
                "}\n" +
                "\n" +
                "td.top {\n" +
                "font-family: Sans-serif; family-name: Helvetica; font-size: small;\n" +
                "background: white; color: #2F4F4F;\n" +
                "width: 312px; height: 25px;\n" +
                "text-align: right;\n" +
                "}\n" +
                "\n" +
                "td.bottom {\n" +
                "font-family: Sans-serif; family-name: Helvetica; font-size: x-large;\n" +
                "background: white; color: #2F4F4F;\n" +
                "width: 312px; height: 40px;\n" +
                "text-align: right;\n" +
                "}\n" +
                "\n" +
                "td.crutch { background: #DCDCDC; color: #DCDCDC; }\n" +
                "\n" +
                "form {width: 75px; height: 30px; }\n" +
                "\n" +
                "input.button{\n" +
                "font-family: Sans-serif; family-name: Helvetica;\n" +
                "width: 75px; height: 50px;\n" +
                "}\n" +
                "\n" +
                "input.buttonDigit {\n" +
                "font-family: Sans-serif; family-name: Helvetica; font-weight: bold;\n" +
                "width: 75px; height: 50px;\n" +
                "}\n" +
                "\n" +
                "input.buttonFunction {\n" +
                "font-family: Sans-serif; family-name: Helvetica;\n" +
                "width: 75px; height: 50px;\n" +
                "}\n" +
                "\n" +
                "input.gitHubButton {\n" +
                "width: 75px; height: 50px;\n" +
                "color: teal;\n" +
                "}\n" +
                "    </style>\n" +
                "\n" +
                "</head>\n" +
                "<body>\n" +
                "<table class=\"top\">\n" +
                "    <tr>\n" +
                "        <td class=\"header\"><i class='fa fa-github' style='color: #f3da35'></i>calc</td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "        <td class=\"top\">" + topField.toString() + "</td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "        <td class=\"bottom\">" + bottomField.toString() + "</td>\n" +
                "    </tr>\n" +
                "</table>\n" +
                "\n" +
                "<table class=\"bottom\">\n" +
                "    <tr>\n" +
                "        <td>\n" +
                "            <form action=\"/calculator\" method=\"get\">\n" +
                "                <p><input class=\"buttonFunction\" type=\"submit\" name=\"answer\" value=\"СЕ\"></p>\n" +
                "            </form>\n" +
                "        </td>\n" +
                "        <td>\n" +
                "            <form action=\"/calculator\" method=\"get\">\n" +
                "                <p><input class=\"buttonFunction\" type=\"submit\" name=\"answer\" value=\"С\"></p>\n" +
                "            </form>\n" +
                "        </td>\n" +
                "        <td>\n" +
                "            <form action=\"/calculator\" method=\"get\">\n" +
                "                <p><input class=\"buttonFunction\" type=\"submit\" name=\"answer\" value=\"&#9003;\"></p>\n" +
                "            </form>\n" +
                "        </td>\n" +
                "        <td>\n" +
                "            <form action=\"https://github.com/make-me-unsee-it/WebCalculator\" method=\"get\">\n" +
                "                <p><input class=\"gitHubButton\" type=\"submit\" value=\"GitHub\"></p>\n" +
                "            </form>\n" +
                "        </td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "        <td class=\"=usual\">\n" +
                "            <form action=\"/calculator\" method=\"get\">\n" +
                "                <p><input class=\"buttonFunction\" type=\"submit\" name=\"answer\" value=\"&#8730;х\"></p>\n" +
                "            </form>\n" +
                "        </td>\n" +
                "        <td class=\"=usual\">\n" +
                "            <form action=\"/calculator\" method=\"get\">\n" +
                "                <p><input class=\"buttonFunction\" type=\"submit\" name=\"answer\" value=\"%\"></p>\n" +
                "            </form>\n" +
                "        </td>\n" +
                "        <td class=\"=usual\">\n" +
                "            <form action=\"/calculator\" method=\"get\">\n" +
                "                <p><input class=\"buttonFunction\" type=\"submit\" name=\"answer\" value=\"x&#178;\"></p>\n" +
                "            </form>\n" +
                "        </td>\n" +
                "        <td class=\"=usual\">\n" +
                "            <form action=\"/calculator\" method=\"get\">\n" +
                "                <p><input class=\"buttonFunction\" type=\"submit\" name=\"answer\" value=\"&#247;\"></p>\n" +
                "            </form>\n" +
                "        </td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "        <td>\n" +
                "            <form action=\"/calculator\" method=\"get\">\n" +
                "                <p><input class=\"buttonDigit\" type=\"submit\" name=\"answer\" value=\"7\"></p>\n" +
                "            </form>\n" +
                "        </td>\n" +
                "        <td>\n" +
                "            <form action=\"/calculator\" method=\"get\">\n" +
                "                <p><input class=\"buttonDigit\" type=\"submit\" name=\"answer\" value=\"8\"></p>\n" +
                "            </form>\n" +
                "        </td>\n" +
                "        <td>\n" +
                "            <form action=\"/calculator\" method=\"get\">\n" +
                "                <p><input class=\"buttonDigit\" type=\"submit\" name=\"answer\" value=\"9\"></p>\n" +
                "            </form>\n" +
                "        </td>\n" +
                "        <td>\n" +
                "            <form action=\"/calculator\" method=\"get\">\n" +
                "                <p><input class=\"buttonFunction\" type=\"submit\" name=\"answer\" value=\"&#215;\"></p>\n" +
                "            </form>\n" +
                "        </td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "        <td>\n" +
                "            <form action=\"/calculator\" method=\"get\">\n" +
                "                <p><input class=\"buttonDigit\" type=\"submit\" name=\"answer\" value=\"4\"></p>\n" +
                "            </form>\n" +
                "        </td>\n" +
                "        <td>\n" +
                "            <form action=\"/calculator\" method=\"get\">\n" +
                "                <p><input class=\"buttonDigit\" type=\"submit\" name=\"answer\" value=\"5\"></p>\n" +
                "            </form>\n" +
                "        </td>\n" +
                "        <td>\n" +
                "            <form action=\"/calculator\" method=\"get\">\n" +
                "                <p><input class=\"buttonDigit\" type=\"submit\" name=\"answer\" value=\"6\"></p>\n" +
                "            </form>\n" +
                "        </td>\n" +
                "        <td>\n" +
                "            <form action=\"/calculator\" method=\"get\">\n" +
                "                <p><input class=\"buttonFunction\" type=\"submit\" name=\"answer\" value=\"-\"></p>\n" +
                "            </form>\n" +
                "        </td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "        <td>\n" +
                "            <form action=\"/calculator\" method=\"get\">\n" +
                "                <p><input class=\"buttonDigit\" type=\"submit\" name=\"answer\" value=\"1\"></p>\n" +
                "            </form>\n" +
                "        </td>\n" +
                "        <td>\n" +
                "            <form action=\"/calculator\" method=\"get\">\n" +
                "                <p><input class=\"buttonDigit\" type=\"submit\" name=\"answer\" value=\"2\"></p>\n" +
                "            </form>\n" +
                "        </td>\n" +
                "        <td>\n" +
                "            <form action=\"/calculator\" method=\"get\">\n" +
                "                <p><input class=\"buttonDigit\" type=\"submit\" name=\"answer\" value=\"3\"></p>\n" +
                "            </form>\n" +
                "        </td>\n" +
                "        <td>\n" +
                "            <form action=\"/calculator\" method=\"get\">\n" +
                "                <p><input class=\"buttonFunction\" type=\"submit\" name=\"answer\" value=\"+\"></p>\n" +
                "            </form>\n" +
                "        </td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "        <td>\n" +
                "            <form action=\"/calculator\" method=\"get\">\n" +
                "                <p><input class=\"button\" type=\"submit\" name=\"answer\" value=\"&#177;\"></p>\n" +
                "            </form>\n" +
                "        </td>\n" +
                "        <td>\n" +
                "            <form action=\"/calculator\" method=\"get\">\n" +
                "                <p><input class=\"buttonDigit\" type=\"submit\" name=\"answer\" value=\"0\"></p>\n" +
                "            </form>\n" +
                "        </td>\n" +
                "        <td>\n" +
                "            <form action=\"/calculator\" method=\"get\">\n" +
                "                <p><input class=\"button\" type=\"submit\" name=\"answer\" value=\",\"></p>\n" +
                "            </form>\n" +
                "        </td>\n" +
                "        <td>\n" +
                "            <form action=\"/calculator\" method=\"get\">\n" +
                "                <p><input class=\"buttonFunction\" type=\"submit\" name=\"answer\" value=\"=\"></p>\n" +
                "            </form>\n" +
                "        </td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "        <td class=\"crutch\">.</td>\n" +
                "    </tr>\n" +
                "</table>\n" +
                "</body>\n" +
                "</html>");
    }


    public char inputRecognition(String input) {
        if (input.equals("СЕ")) return 'e';                 //clear entry
        else if (input.equals("С")) return 'c';             //clear all
        else if (input.equals("⌫")) return 'b';            //backspace
        else if (input.equals("√х")) return 'r';            //square root
        else if (input.equals("%")) return '%';             //percentage
        else if (input.equals("x²")) return 's';            //square
        else if (input.equals("÷")) return '/';             //division
        else if (input.equals("7")) return '7';             //7
        else if (input.equals("8")) return '8';             //8
        else if (input.equals("9")) return '9';             //9
        else if (input.equals("×")) return '*';             //multiplication
        else if (input.equals("4")) return '4';             //4
        else if (input.equals("5")) return '5';             //5
        else if (input.equals("6")) return '6';             //6
        else if (input.equals("-")) return '-';             //minus
        else if (input.equals("1")) return '1';             //1
        else if (input.equals("2")) return '2';             //2
        else if (input.equals("3")) return '3';             //3
        else if (input.equals("+")) return '+';             //plus
        else if (input.equals("±")) return '&';             //sign change
        else if (input.equals("0")) return '0';             //0
        else if (input.equals(",")) return '.';             //dot
        else return '=';                                    //result
    }
}

