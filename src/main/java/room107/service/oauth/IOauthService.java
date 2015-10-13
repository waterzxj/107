package room107.service.oauth;

import java.util.Map;

import room107.datamodel.User;
import room107.service.oauth.IOauthPlatform.ReleaseStatus;

public interface IOauthService {	
	Map<String, String> getLoginUrls();
	
	void confirmOauthStatus(String key, User user);

	ReleaseStatus waitOauthStatus(String key);	
	
	User getOauthUser(String platform, String code);
}
