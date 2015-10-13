package room107.web;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.apachecommons.CommonsLog;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import room107.datamodel.User;
import room107.util.JsonUtils;
import room107.web.session.SessionKeys;
import room107.web.session.SessionManager;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Controller for uploading files.
 * 
 * @author WangXiao
 */
@Controller
@CommonsLog
public class UploadController {

    public final int uploadMaxCount = 6, uploadMaxSize = 512 * 1024;

    private final String uploadDir = "/107room/static/user/",
            debugUploadDir = "C:/Users/Brainshawn/git/107room/src/main/webapp/static/user/";

    private final Set<String> uploadFileTypes = new HashSet<String>(
            Arrays.asList(new String[] { "jpg", "jpeg", "gif", "png" }));
    
    @RequestMapping(value = "/upload/{dir}", method = RequestMethod.POST)
    public ResponseEntity<String> upload(@PathVariable String dir,
            MultipartHttpServletRequest request, HttpServletResponse response) {
        String iid = request.getParameter("iid");
        User user = SessionManager.getSessionValue(request, response, SessionKeys.USER);
        Validate.notNull(user);
        LinkedList<UploadFile> result = new LinkedList<UploadFile>();
        Iterator<String> itr = request.getFileNames();
        int i = 0;
        while (itr.hasNext()) {
            MultipartFile file = request.getFile(itr.next());
            // validate type
            String fileType = null;
            int j = file.getOriginalFilename().lastIndexOf('.');
            if (j > 0) {
                String postfix = file.getOriginalFilename().substring(j + 1)
                        .toLowerCase();
                if (uploadFileTypes.contains(postfix)) {
                    fileType = postfix;
                }
            }
            final String name = getFileName(file.getOriginalFilename(),
                    user.getUsername(), fileType);
            UploadFile uploadFile = new UploadFile(iid, name, file.getSize());
            result.add(uploadFile);
            if (fileType == null) {
                uploadFile.setStatus(UploadFile.STATUS_ERROR_TYPE);
                log.warn("Invalid type: fileName=" + file.getOriginalFilename());
                continue;
            }
            // validate size
            if (file.getSize() > uploadMaxSize) {
                uploadFile.setStatus(UploadFile.STATUS_ERROR_SIZE);
                log.warn("Too big: size=" + file.getSize());
                continue;
            }
            // read and write
            String ud = uploadDir;
            String url = StringUtils.trimToNull(request.getRequestURL()
                    .toString());
            if (url != null && !url.contains("107room.com")) {
                ud = debugUploadDir;
            }
            try {
                uploadFile.setBytes(file.getBytes());
                File path = new File(ud + dir + File.separator + name);
                FileCopyUtils.copy(file.getBytes(), new FileOutputStream(path));
                log.info("Upload: username=" + user.getUsername() + ", file="
                        + path);
            } catch (IOException e) {
                log.error("Save file failed: path=", e);
                File d = new File(ud + dir);
                if (!d.exists()) { // usually when first-time deploy
                    d.mkdirs();
                }
            }
            if (++i > uploadMaxCount) {
                log.warn("Too many files: uploadMaxCount=" + uploadMaxCount);
                break;
            }
        }
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("Content-Type", "text/html; charset=utf-8");
        return new ResponseEntity<String>(
                JsonUtils.toJson(result), responseHeaders,
                HttpStatus.CREATED);
    }

    private String getFileName(String fileName, String username, String fileType) {
        return new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + "_"
                + username + "_" + Math.abs(fileName.hashCode()) + "."
                + fileType;
    }

}

@RequiredArgsConstructor
@JsonIgnoreProperties({ "bytes" })
@ToString
class UploadFile {

    static final int STATUS_OK = 0, STATUS_ERROR_TYPE = 1,
            STATUS_ERROR_SIZE = 2;

    /**
     * Sent from client.
     */
    @Getter
    @Setter
    @NonNull
    private String iid;

    @Getter
    @Setter
    @NonNull
    private String name;

    @Getter
    @Setter
    @NonNull
    private long size;

    @Getter
    @Setter
    private int status = STATUS_OK;

    @Getter
    @Setter
    private byte[] bytes;

}
