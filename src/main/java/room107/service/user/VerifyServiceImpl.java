package room107.service.user;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.extern.apachecommons.CommonsLog;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.mail.HtmlEmail;
import org.apache.velocity.VelocityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import room107.dao.IUserDao;
import room107.dao.IVerifyEmailDao;
import room107.dao.IVerifyEmailDao.EmailType;
import room107.dao.IVerifyEmailRecordDao;
import room107.datamodel.User;
import room107.datamodel.VerifyEmail;
import room107.datamodel.VerifyEmailRecord;
import room107.datamodel.VerifyEmailStatus;
import room107.datamodel.VerifyEmailType;
import room107.datamodel.VerifyStatus;
import room107.util.EmailUtils;
import room107.util.UserBehaviorLog;
import room107.util.WebUtils;

/**
 * @author WangXiao
 */
@CommonsLog
@Service
@Transactional
public class VerifyServiceImpl implements IVerifyService {

    @Autowired
    private IVerifyEmailDao verifyEmailDao;

    @Autowired
    private IVerifyEmailRecordDao verifyEmailRecordDao;

    @Autowired
    private IUserDao userDao;

    @Override
    public VerifyEmailStatus verifyEmail(ConfirmData confirmData) {
        Validate.notNull(confirmData);
        confirmData.validate();
        String username = confirmData.getUsername();
        String email = confirmData.getEmail();
        email = StringUtils.trimToNull(email);
        Validate.notNull(email);
        UserBehaviorLog.AUTH.info("Verify email: username=" + username
                + ", email=" + email);
        String domain = WebUtils.getDomainFromEmail(email);
        EmailType type = verifyEmailDao.getEmailType(domain);
        /*
         * correct white by domain postfix
         */
        if (type == null && EmailUtils.isEduEmail(email)) {
            type = EmailType.WHITE;
        }
        /*
         * check black
         */
        if (type == EmailType.BLACK) {
            updateVerifyRecord(username, email, VerifyEmailStatus.BLACK);
            return VerifyEmailStatus.BLACK;
        }
        /*
         * check confirmed by other
         */
        if (confirmedByOther(username, email)) {
            updateVerifyRecord(username, email,
                    VerifyEmailStatus.CONFIRMED_BY_OTHER);
            return VerifyEmailStatus.CONFIRMED_BY_OTHER;
        }
        /*
         * send confirm email
         */
        sendEmail(confirmData, "verify/white.vm");
        if (type != EmailType.WHITE) { // gray
            EmailUtils.sendAdminMail("Gray email: " + email, "username: "
                    + username);
            updateVerifyRecord(username, email, VerifyEmailStatus.GRAY);
            return VerifyEmailStatus.GRAY;
        } else { // white
            updateVerifyRecord(username, email, VerifyEmailStatus.TO_CONFIRM);
            return VerifyEmailStatus.TO_CONFIRM;
        }
    }

    /**
     * @param email
     * @return
     */
    private boolean confirmedByOther(String username, String email) {
        VerifyEmailRecord record = getConfirmRecord(email);
        if (record != null && !username.equalsIgnoreCase(record.getUsername())) {
            return true;
        }
        return false;
    }

    @Override
    public boolean confirmEmail(ConfirmData data) {
        Validate.notNull(data);
        data.validate();
        // check whether confirmed by other
        if (confirmedByOther(data.getUsername(), data.getEmail())) {
            return false;
        }
        // update confirm
        VerifyEmailRecord record = verifyEmailRecordDao.getRecord(
                data.getUsername(), data.getEmail());
        if (record != null
                && record.getStatus() != VerifyEmailStatus.GRAY.ordinal()) {
            updateVerifyRecord(data.getUsername(), data.getEmail(),
                    VerifyEmailStatus.CONFIRMED);
        }
        User user = userDao.getUser(data.getUsername());
        user.setVerifyStatus(VerifyStatus.VERIFIED_EMAIL.ordinal());
        user.setVerifyEmail(data.getEmail());
        userDao.update(user);
        return true;
    }

    @Override
    public void confirmCredential(String username, String verifyName, String verifyId) {
        User user = userDao.getUser(username);
        Validate.notNull(user);
        user.setVerifyStatus(VerifyStatus.VERIFIED_CREDENTIAL.ordinal());
        user.setModifiedTime(new Date());
        user.setVerifyName(verifyName);
        user.setVerifyId(verifyId);
        userDao.update(user);
    }

    @Override
    public List<VerifyEmailRecord> getVerifyEmailRecords(
            VerifyEmailStatus status) {
        return verifyEmailRecordDao.getRecords(null, status);
    }

    @Override
    public void setEmailType(String email, VerifyEmailType type) {
        Validate.notNull(email);
        Validate.notNull(type);
        // update email type
        String domain = WebUtils.getDomainFromEmail(email);
        Validate.notNull(domain, "Null domain: email=" + email);
        VerifyEmail verifyEmail = verifyEmailDao.getVerifyEmail(domain);
        if (verifyEmail == null) {
            verifyEmail = new VerifyEmail(domain, type.ordinal(), new Date());
        } else {
            verifyEmail.setType(type.ordinal());
        }
        verifyEmailDao.saveOrUpdate(verifyEmail);
        // update record
        VerifyEmailStatus status = type == VerifyEmailType.WHITE ? VerifyEmailStatus.TO_CONFIRM
                : VerifyEmailStatus.BLACK;
        List<VerifyEmailRecord> records = verifyEmailRecordDao.updateRecords(
                domain, status);
        // black
        if (type == VerifyEmailType.BLACK) {
            Set<String> usernames = new HashSet<String>();
            // send email
            for (VerifyEmailRecord record : records) {
                usernames.add(record.getUsername());
                sendEmail(
                        new ConfirmData(record.getUsername(), record.getEmail()),
                        "verify/black.vm");
            }
            // update verify status
            for (String username : usernames) {
                try {
                    User user = userDao.getUser(username);
                    user.setVerifyStatus(VerifyStatus.UNVERIFIED.ordinal());
                    userDao.update(user);
                } catch (Exception e) {
                    log.error("Black user failed: username=" + username, e);
                }
            }
        }
    }

    private VerifyEmailRecord getConfirmRecord(String email) {
        List<VerifyEmailRecord> records = verifyEmailRecordDao.getRecords(
                email, VerifyEmailStatus.CONFIRMED);
        return records.isEmpty() ? null : records.get(0);
    }

    private void sendEmail(ConfirmData data, String template) {
        String title = "107间邮箱认证";
        VelocityContext context = new VelocityContext();
        context.put("title", title);
        context.put("data", data);
        EmailUtils.sendMailByTemplate(data.getEmail(), title, template,
                context, HtmlEmail.class, EmailUtils.EmailAccount.NO_REPLY);

    }

    private void updateVerifyRecord(String username, String email,
            VerifyEmailStatus status) {
        Date date = new Date();
        VerifyEmailRecord record = verifyEmailRecordDao.getRecord(username,
                email);
        if (record == null) {
            record = new VerifyEmailRecord(username, email, status.ordinal(),
                    date, date);
        } else {
            if (record.getStatus() == VerifyEmailStatus.GRAY.ordinal()) {

            }
            record.setStatus(status.ordinal());
            record.setModifiedTime(date);
        }
        verifyEmailRecordDao.saveOrUpdate(record);
    }

    @Override
    public List<User> getUsersByVerifyName(String verifyName) {
        return userDao.getUserByVerifyName(verifyName);
    }

}
