package room107.dao;

import java.util.List;

import room107.datamodel.Contact;

/**
 * @author WangXiao
 */
public interface IContactDao extends IDao {

    List<Contact> getLatest(int maxCount);

}
