package room107.service.user;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.mail.SimpleEmail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import room107.dao.IContactDao;
import room107.datamodel.Contact;
import room107.util.EmailUtils;
import room107.util.EmailUtils.EmailAccount;

/**
 * @author WangXiao
 */
@Service
public class ContactServiceImpl implements IContactService {
    
    private static final List<String> toes;
    
    static {
        toes = new ArrayList<String>();
        toes.add("weiwei@107room.com");
        toes.add("zhaoshuhong@107room.com");
        toes.add("jinyu@107room.com");
        toes.add("yanghao@107room.com");
    }

    @Autowired
    private IContactDao contactDao;

    @Override
    public List<Contact> getAll() {
        return contactDao.getAll(Contact.class);
    }

    @Override
    public void submit(Contact contact) {
        contactDao.save(contact);
        EmailUtils.sendMail(toes, "New contact",
                "username=" + contact.getUsername() + ", message="
                        + contact.getMessage() + ", contact="
                        + contact.getContact1() + " " + contact.getContact2(),
                SimpleEmail.class, EmailAccount.ADMIN);
    }
}
