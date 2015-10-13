package room107.service.oauth;

public abstract class OauthPlatformImpl implements IOauthPlatform{
	public abstract String getPlatform();

	public abstract String getLoginUrl();
	
	public abstract Auth getAuth(String code);
	
	public abstract AuthUserInfo getUserInfo(Auth auth);
}
