/**
 * 
 */
package room107.tool.user;

import java.io.IOException;
import java.util.List;

import lombok.Setter;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import room107.datamodel.User;
import room107.util.Md5Utils;

/**
 * @author yanghao
 */
public class UserTokenGenerateTool {
    
    @Setter
    private SessionFactory sessionFactory;
    
    public void run() {
        Session session = sessionFactory.openSession();
        @SuppressWarnings("unchecked")
        List<User> users = session.createCriteria(User.class).list();
        for (User user : users) {
            user.setToken(Md5Utils.number2String(Md5Utils.sign32bit(user.getUsername().getBytes())));
            Transaction tx = session.beginTransaction();
            session.update(user);
            tx.commit();
            System.out.println(user.getUsername() + " : " + user.getToken());
        }
        session.close();
        System.out.println("total : " + users.size());
    }

    public static void main(String[] args) throws IOException {
        @SuppressWarnings("resource")
        ApplicationContext context = new ClassPathXmlApplicationContext(
                "tool-context.xml");
        UserTokenGenerateTool tool = (UserTokenGenerateTool) context
                .getBean("userTokenGenerateTool");
        tool.run();
        System.exit(0);
    }

}
