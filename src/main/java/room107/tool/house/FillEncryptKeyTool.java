/**
 * 
 */
package room107.tool.house;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Setter;
import lombok.extern.apachecommons.CommonsLog;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import room107.datamodel.ExternalHouse;
import room107.datamodel.House;
import room107.util.EncryptUtils;

/**
 * @author yanghao
 *
 */
@CommonsLog
public class FillEncryptKeyTool {
    
    @Setter
    private SessionFactory sessionFactory;
    
    @SuppressWarnings("unchecked")
    public void run() {
        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        List<ExternalHouse> externalHouses = session.createCriteria(ExternalHouse.class).list();
        Map<Long, ExternalHouse> id2House = new HashMap<Long, ExternalHouse>();
        for (ExternalHouse eh : externalHouses) {
            if (eh.getHouseId() != null) {
                id2House.put(eh.getHouseId(), eh);
            }
        }
        List<House> houses = session.createCriteria(House.class).add(Restrictions.in("id", id2House.keySet())).list();
        
        for (House house : houses) {
            if (house.getUsername().startsWith("Anonymous_")) {
                try {
                    ExternalHouse eh = id2House.get(house.getId());
                    eh.setEncryptKey(EncryptUtils.encryptAnomynous(house.getUsername()));
                    session.update(eh);
                    log.info("update a house : " + house.getId());
                } catch (Exception e) {
                    log.error(e, e);
                }
            }
        }
        
        tx.commit();
        session.close();
    }

    public static void main(String[] args) throws IOException {
        @SuppressWarnings("resource")
        ApplicationContext context = new ClassPathXmlApplicationContext(
                "tool-context.xml");
        FillEncryptKeyTool tool = (FillEncryptKeyTool) context
                .getBean("fillEncryptKeyTool");
        tool.run();
        System.exit(0);
    }

}
