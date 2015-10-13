package room107.service.user;

import java.util.List;

import room107.datamodel.Contact;

/**
 * @author WangXiao
 */
public interface IContactService {

    List<Contact> getAll();

    void submit(Contact contact);

}
