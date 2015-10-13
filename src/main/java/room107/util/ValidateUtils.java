package room107.util;

import java.util.regex.Pattern;

import lombok.Getter;
import lombok.extern.apachecommons.CommonsLog;
import room107.datamodel.GenderType;

/**
 * @author WangXiao
 */
@CommonsLog
public class ValidateUtils {

    @Getter
    public static final int USERNAME_MIN_CHARS = 1, USERNAME_MAX_CHARS = 30;

    @Getter
    public static final int PASSWORD_MIN_CHARS = 5, PASSWORD_MAX_CHARS = 30;

    @Getter
    public static final String EMAIL_PATTERN = "^[\\w-+]+(\\.[\\w-+]+)*@([\\w-]+\\.)+[a-zA-Z]+$";

    public static boolean validateUsername(String username) {
        if (username == null) {
            return false;
        }
        for (int i = 0; i < username.length(); i++) {
            if (Character.isWhitespace(username.charAt(i))) {
                log.debug("Has white space: " + username);
                return false;
            }
        }
        return username.length() >= USERNAME_MIN_CHARS
                && username.length() <= USERNAME_MAX_CHARS;
    }

    public static boolean validatePassword(String password) {
        if (password == null) {
            return false;
        }
        return password.length() >= PASSWORD_MIN_CHARS
                && password.length() <= PASSWORD_MAX_CHARS;
    }

    public static boolean validateEmail(String email) {
        return email == null ? false : Pattern.matches(EMAIL_PATTERN, email);
    }

    public static boolean validateGender(String gender) {
        try {
            int i = Integer.parseInt(gender);
            GenderType.values()[i].toString(); // test bound
            return true;
        } catch (Exception e) {
            log.warn("Invalid gender: " + gender);
            return false;
        }
    }
}
