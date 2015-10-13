package room107.service.storage.qiniu;

import lombok.extern.apachecommons.CommonsLog;

import org.springframework.stereotype.Service;

import room107.util.QiniuUploadUtils;

import com.qiniu.api.auth.digest.Mac;
import com.qiniu.api.config.Config;
import com.qiniu.api.rs.PutPolicy;

/**
 * @author WangXiao
 */
@CommonsLog
@Service
public class QiniuService {
	private static final String UPLOAD_REDIRECT_PATH = "/house/upload/redirect";

    public QiniuService() throws Exception {
        Config.ACCESS_KEY = QiniuUploadUtils.ACCESS_KEY;
        Config.SECRET_KEY = QiniuUploadUtils.SECRET_KEY;
    }

    public String getUptokenHouse() throws Exception {
        return getUptoken("h107");
    }
    
    public String getRedirectUptokenHouse(String redirectDomain) throws Exception {
    	return getRedirectUptoken("h107", redirectDomain);
    }

    public String getUptokenTest() throws Exception {
        return getUptoken("test107");
    }

    private String getUptoken(String bucket) throws Exception {
        PutPolicy policy = new PutPolicy(bucket);
        policy.returnBody = "{\"bucket\":$(bucket),\"key\":$(key),\"iid\":$(x:iid),\"size\":$(fsize),\"w\":$(imageInfo.width),\"h\":$(imageInfo.height)}";
        String result = policy.token(new Mac(Config.ACCESS_KEY,
                Config.SECRET_KEY));
        if (log.isDebugEnabled()) {
            log.debug("bucket=" + bucket + ", uptoken=" + result);
        }
        return result;
    }
    
    private String getRedirectUptoken(String bucket, String redirectDomain) throws Exception {    	
        PutPolicy policy = new PutPolicy(bucket);
        policy.returnBody = "{\"bucket\":$(bucket),\"key\":$(key),\"iid\":$(x:iid),\"size\":$(fsize),\"w\":$(imageInfo.width),\"h\":$(imageInfo.height)}";        
        policy.returnUrl = "http://" + redirectDomain + UPLOAD_REDIRECT_PATH;
        String result = policy.token(new Mac(Config.ACCESS_KEY,
                Config.SECRET_KEY));
        if (log.isDebugEnabled()) {
            log.debug("bucket=" + bucket + ", uptoken=" + result);
        }
        return result;    	
    }
}
