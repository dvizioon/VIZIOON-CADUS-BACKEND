package br.com.backend.cadus.common;

public final class Cpf {

  private Cpf() {}

  public static String digits(String value) {
    if (value == null) {
      return "";
    }
    return value.replaceAll("\\D", "");
  }

  public static boolean isValid(String value) {
    String digits = digits(value);
    if (digits.length() != 11 || digits.chars().distinct().count() == 1) {
      return false;
    }
    return checkDigit(digits, 9) == digitAt(digits, 9)
        && checkDigit(digits, 10) == digitAt(digits, 10);
  }

  private static int checkDigit(String digits, int length) {
    int sum = 0;
    for (int index = 0; index < length; index += 1) {
      sum += digitAt(digits, index) * (length + 1 - index);
    }
    int rest = (sum * 10) % 11;
    return rest == 10 ? 0 : rest;
  }

  private static int digitAt(String digits, int index) {
    return digits.charAt(index) - '0';
  }
}
