package room107.service.user;

import java.util.Collections;
import java.util.List;

import lombok.extern.apachecommons.CommonsLog;
import room107.datamodel.Contact;

/**
 * @author WangXiao
 */
@CommonsLog
public class ContactServiceImplMock implements IContactService {

    @Override
    public void submit(Contact contact) {
        log.debug("Submited: " + contact);
    }

    @Override
    public List<Contact> getAll() {
        return Collections.emptyList();
    }

}
