package room107.dao;

import java.util.List;

import room107.datamodel.VerifyEmailRecord;
import room107.datamodel.VerifyEmailStatus;

/**
 * Note: record is unique for a pair of <username, email>
 * 
 * @author WangXiao
 */
public interface IVerifyEmailRecordDao extends IDao {

    /**
     * @param email
     *            null when no limit
     * @param status
     *            null when no limit
     */
    List<VerifyEmailRecord> getRecords(String email, VerifyEmailStatus status);

    /**
     * @return null when not found
     */
    VerifyEmailRecord getRecord(String username, String email);

    List<VerifyEmailRecord> updateRecords(String emailDomain,
            VerifyEmailStatus status);

}
