package room107.mock;

import java.util.Date;

import org.apache.commons.lang.math.RandomUtils;

import room107.datamodel.House;
import room107.datamodel.RentType;

/**
 * @author WangXiao
 */
public class MockHouse extends House {

    private static final long serialVersionUID = -9204227131587063945L;
    
    public static String text = "最新消息：终于要重新招租了，9月份即可入住，爱狗爱音乐的朋友们奔走相告咯！！ \n房子位置在万寿路地铁站向北一个路口，翠微南里。周围绿化很好，都是部委，机关单位的小区。（步行到万寿路地铁站10分钟） \n\n价格：整套是4000元，我的主卧是2200，次卧是1800。杂费均摊，广袤的公共空间共享。 \n\n条件是： \n希望合租人也养狗或是养过狗。本人带一只特别宅的成年金毛。我的狗从来不叫，平时就喜欢睡觉或是玩它的益智玩具。本人不定期出差，希望能与室友相互帮忙照顾狗狗，我出差回来后定盛排筵宴表示感谢。 \n\n福利是： \n入住这套房子就可以享受极尽奢华的私家花园，嗯。 \n\n本人自带各种乐器与录音设备（业余爱好，非职业，不扰民）。如果相处融洽，可以用业余时间教室友乐器，包教包会包分配，学不会下期免费再学，或者免费录音这样子。 \n\n本人在机关单位工作，工作地点在小马厂（西站往东一站地）。本人产地河北，单身，直男，不抽烟，每晚会在锻炼后喝点红酒帮助睡眠，心地善良，仗义疏财，助人为乐。\n";

    public MockHouse() {
        this(0, 0);
        setId((long) RandomUtils.nextInt(30));
        setRank(RandomUtils.nextInt(100));
    }

    public MockHouse(long id, float rank) {
        setId(id);
        setRank(rank);
        setRentType(RandomUtils.nextInt(RentType.values().length) + 1);
        setPrice(1070);
        setProvince("北京");
        setCity("海淀");
        setPosition("万柳北京大学公寓");
        setUsername("中文room1234");
        setName("二室一厅");
        setDescription(text);
        setFloor(3);
        setFacilities("空调|电视|洗衣机|独卫");
        setRoomNumber(3);
        setHallNumber(1);
        setKitchenNumber(1);
        setToiletNumber(10);
        setArea(107);
        setTelephone("+86 15120003000");
        setImageId(RandomUtils.nextInt(5) + ".jpg");
        setAuditStatus(RandomUtils.nextInt(3));
        double x = 116.403875, y = 39.915168;
        setLocationX(x + randomLocationOffset());
        setLocationY(y + randomLocationOffset());
        setCreatedTime(new Date());
        setModifiedTime(new Date());
    }

    private double randomLocationOffset() {
        double result = RandomUtils.nextInt(100) / 1000.0;
        if (RandomUtils.nextBoolean()) {
            result = -result;
        }
        return result;
    }

}
