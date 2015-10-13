package room107.service.user;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;

import org.apache.commons.lang.Validate;

import room107.dao.IUserDao;
import room107.datamodel.User;
import room107.datamodel.VerifyEmailRecord;
import room107.datamodel.VerifyEmailStatus;
import room107.datamodel.VerifyEmailType;
import room107.util.EncryptUtils;
import room107.util.JsonUtils;

import com.google.gson.Gson;

/**
 * See {@link IUserDao}.
 * 
 * @author WangXiao
 */
public interface IVerifyService {

    /**
     * @return non-null
     */
    VerifyEmailStatus verifyEmail(ConfirmData confirmData);

    /**
     * @return whether confirm successfully, e.g. confirmed by others
     */
    boolean confirmEmail(ConfirmData data);

    void confirmCredential(String username, String verifyName, String verifyId);
    
    List<User> getUsersByVerifyName(String verifyName);

    // ----------- for admin ---------- //

    List<VerifyEmailRecord> getVerifyEmailRecords(VerifyEmailStatus status);

    void setEmailType(String email, VerifyEmailType type);

    /**
     * Denote where confirm request was sent.
     * 
     * @author WangXiao
     */
    enum ConfirmSource {
        PC, MOBILE
    }

    @RequiredArgsConstructor
    @NoArgsConstructor
    @Data
    @CommonsLog
    class ConfirmData {

        @NonNull
        private String username, email;

        @NonNull
        private ConfirmSource source = ConfirmSource.PC;

        private long time = System.currentTimeMillis();

        public ConfirmData(String username, String email, ConfirmSource source) {
            this.username = username;
            this.email = email;
            this.source = source;
            validate();
        }

        /**
         * @return null when failed
         */
        public String getConfirmUrl() {
            String code = encrypt();
            return code == null ? null : "http://107room.com/verify/" + code;
        }

        /**
         * @return null when failed
         */
        public String encrypt() {
            String json = JsonUtils.toJson(this);
            try {
                return EncryptUtils.encrypt(json);
            } catch (Exception e) {
                return null;
            }
        }

        /**
         * 
         * @param code
         *            encrypted code
         */
        public static ConfirmData decrypt(String code) {
            if (code == null) {
                return null;
            }
            try {
                String json = EncryptUtils.decrypt(code);
                return new Gson().fromJson(json, ConfirmData.class);
            } catch (Exception e) {
                log.error("Decrypt failed: code=" + code, e);
                return null;
            }
        }

        public void validate() {
            Validate.notNull(username);
            Validate.notNull(email);
            Validate.notNull(source);
        }

    }

}
