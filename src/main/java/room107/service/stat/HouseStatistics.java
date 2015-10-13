package room107.service.stat;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.PostConstruct;

import lombok.extern.apachecommons.CommonsLog;

import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import room107.dao.ILogStatResultDao;
import room107.datamodel.LogStatResult;
import room107.service.house.IHouseService;

/**
 * @author WangXiao
 */
@CommonsLog
@Component
@Transactional
public class HouseStatistics {
    
    private static final double ratio = 3.1415926 * 3;

    @Autowired
    private IHouseService houseService;
    
    @Autowired
    private ILogStatResultDao logStatResultDao;

    private Date updateTime;

    private int count;
    
    private Map<String, Integer> district2UserCount = null;
    
    private static final long UPDAT_DISTRICT_INTERVAL = 1000L * 60 * 30;

    public synchronized int getRejectedHouseCount() {
        Date date = new Date();
        if (updateTime == null
                || DateUtils.addMinutes(updateTime, 30).compareTo(date) <= 0) {
            count = houseService.getRejectedHouseCount();
            updateTime = date;
        }
        return count;
    }
    
    @PostConstruct
    public void init() {
        reloadDistrict2UserCount();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                reloadDistrict2UserCount();
            }
        }, UPDAT_DISTRICT_INTERVAL, UPDAT_DISTRICT_INTERVAL);
    }
    
    private void reloadDistrict2UserCount() {
        try {
            log.info("start to reload district to user count.");
            List<LogStatResult> results = logStatResultDao.getLatestDistrictStat();
            Map<String, Integer> district2UserCount = new HashMap<String, Integer>();
            for (LogStatResult result : results) {
                String district = result.getName().replaceAll("District_", "");
                district2UserCount.put(district, (int)(result.getUv() * ratio));
            }
            this.district2UserCount = district2UserCount;
            log.info("reload district to user count success, total " + this.district2UserCount.size());
        } catch(Exception e) {
            log.error(e, e);
        }
    }
    
    public Map<String, Integer> getDistrict2UserCount() {
        return district2UserCount;
    }

}
