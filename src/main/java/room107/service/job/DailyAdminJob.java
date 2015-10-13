package room107.service.job;

import java.io.File;
import java.net.URL;
import java.util.List;

import lombok.extern.apachecommons.CommonsLog;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import room107.Constants;
import room107.datamodel.Room;
import room107.service.house.IHouseService;

/**
 * Do daily admin works.
 * 
 * @author WangXiao
 */
@CommonsLog
@Component
public class DailyAdminJob extends SimpleJob {

    @Autowired
    private IHouseService houseService;

    @Override
    public void run() {
        remindDeprecated();
        closeDeprecated();
        backupImages();
    }

    /**
     * Close deprecated houses and rooms.
     */
    private void closeDeprecated() {
        try {
            houseService.closeDeprecated();
        } catch (Exception e) {
            log.error("closeDeprecated failed", e);
        }
    }
    
    /**
     * send email to the users where houses and rooms conditioned.
     */
    private void remindDeprecated() {
        try {
            houseService.remindDeprecated(1);
            houseService.remindDeprecated(2);
            houseService.remindDeprecated(3);
        } catch (Exception e) {
            log.error("remindDeprecated failed", e);
        }
    }

    public void backupImages() {
        List<Room> rooms = houseService.getImageUnbackupedRooms();
        for (Room room : rooms) {
            String[] imageIds = StringUtils.split(room.getImageIds(),
                    Constants.MULTI_VALUE_SEPARATOR);
            log.info("Backup room: roomId=" + room.getId());
            if (imageIds != null) {
                boolean success = true;
                for (String id : imageIds) {
                    try {
                        // from url
                        URL url = new URL(id);
                        // to file
                        File file = new File(Constants.PATH_HOUSE_PHOTO_DIR,
                                url.getPath());
                        log.info("imageId=" + url + ", file="
                                + file.getAbsoluteFile());
                        FileUtils.copyURLToFile(url, file, 5000, 5000);
                    } catch (Exception e) {
                        log.error("Backup image failed: imageId=" + id
                                + ", file=", e);
                        success = false;
                        break;
                    }
                }
                if (success) {
                    room.setImageBackuped(true);
                    houseService.update(room);
                }
            }
        }
    }

}
