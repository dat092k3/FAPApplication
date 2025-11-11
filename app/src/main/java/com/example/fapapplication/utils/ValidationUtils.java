package com.example.fapapplication.utils;

import android.text.TextUtils;
import android.util.Patterns;

import java.util.regex.Pattern;

/**
 * ValidationUtils - Utility class cho validation các input fields
 *
 * Cung cấp các phương thức để kiểm tra tính hợp lệ của:
 * - Email format
 * - Password strength
 * - Required fields
 * - Student ID format
 */
public class ValidationUtils {

    // Password phải có ít nhất 6 ký tự
    private static final int MIN_PASSWORD_LENGTH = 6;

    // Student ID format: chữ cái + số (ví dụ: SE123456, HE141234)
    private static final Pattern STUDENT_ID_PATTERN = Pattern.compile("^[A-Z]{2}\\d{6}$");

    /**
     * Validate email format
     *
     * @param email Email cần kiểm tra
     * @return true nếu email hợp lệ, false nếu không
     */
    public static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * Validate password strength
     *
     * @param password Password cần kiểm tra
     * @return true nếu password hợp lệ (>= 6 ký tự), false nếu không
     */
    public static boolean isValidPassword(String password) {
        return !TextUtils.isEmpty(password) && password.length() >= MIN_PASSWORD_LENGTH;
    }

    /**
     * Validate required field (không được để trống)
     *
     * @param text Text cần kiểm tra
     * @return true nếu text không rỗng, false nếu rỗng
     */
    public static boolean isNotEmpty(String text) {
        return !TextUtils.isEmpty(text) && !text.trim().isEmpty();
    }

    /**
     * Validate student ID format (2 chữ cái + 6 số)
     * Ví dụ: SE123456, HE141234
     *
     * @param studentId Student ID cần kiểm tra
     * @return true nếu format hợp lệ, false nếu không
     */
    public static boolean isValidStudentId(String studentId) {
        return !TextUtils.isEmpty(studentId) && STUDENT_ID_PATTERN.matcher(studentId.toUpperCase()).matches();
    }

    /**
     * Validate xem 2 password có khớp nhau không
     *
     * @param password Password gốc
     * @param confirmPassword Password xác nhận
     * @return true nếu 2 password giống nhau, false nếu khác nhau
     */
    public static boolean passwordsMatch(String password, String confirmPassword) {
        return !TextUtils.isEmpty(password) &&
                !TextUtils.isEmpty(confirmPassword) &&
                password.equals(confirmPassword);
    }

    /**
     * Validate full name (phải có ít nhất 2 từ)
     *
     * @param fullName Full name cần kiểm tra
     * @return true nếu full name hợp lệ, false nếu không
     */
    public static boolean isValidFullName(String fullName) {
        if (TextUtils.isEmpty(fullName)) {
            return false;
        }

        String trimmed = fullName.trim();
        // Phải có ít nhất 2 từ
        return trimmed.contains(" ") && trimmed.split("\\s+").length >= 2;
    }

    /**
     * Validate birthdate format (dd/MM/yyyy)
     *
     * @param birthdate Birthdate string cần kiểm tra
     * @return true nếu format hợp lệ, false nếu không
     */
    public static boolean isValidBirthdate(String birthdate) {
        if (TextUtils.isEmpty(birthdate)) {
            return false;
        }

        // Kiểm tra format cơ bản dd/MM/yyyy
        Pattern datePattern = Pattern.compile("^\\d{2}/\\d{2}/\\d{4}$");
        return datePattern.matcher(birthdate).matches();
    }

    // ============ ERROR MESSAGES ============

    /**
     * Get email error message
     */
    public static String getEmailErrorMessage() {
        return "Email không hợp lệ. Vui lòng nhập đúng định dạng email.";
    }

    /**
     * Get password error message
     */
    public static String getPasswordErrorMessage() {
        return "Mật khẩu phải có ít nhất " + MIN_PASSWORD_LENGTH + " ký tự.";
    }

    /**
     * Get password mismatch error message
     */
    public static String getPasswordMismatchErrorMessage() {
        return "Mật khẩu xác nhận không khớp.";
    }

    /**
     * Get required field error message
     */
    public static String getRequiredFieldErrorMessage() {
        return "Trường này không được để trống.";
    }

    /**
     * Get student ID error message
     */
    public static String getStudentIdErrorMessage() {
        return "Student ID không hợp lệ. Định dạng: 2 chữ cái + 6 số (VD: SE123456)";
    }

    /**
     * Get full name error message
     */
    public static String getFullNameErrorMessage() {
        return "Họ tên phải có ít nhất 2 từ (Họ và Tên).";
    }

    /**
     * Get birthdate error message
     */
    public static String getBirthdateErrorMessage() {
        return "Vui lòng chọn ngày sinh.";
    }
}