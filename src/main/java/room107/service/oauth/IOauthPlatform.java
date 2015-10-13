package room107.service.oauth;

import room107.datamodel.GenderType;
import room107.datamodel.RegRole;
import room107.datamodel.User;
import room107.datamodel.UserType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public interface IOauthPlatform {
    public String getPlatform();

    public String getLoginUrl();

    public Auth getAuth(String code);

    public AuthUserInfo getUserInfo(Auth auth);

    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthUserInfo {
        @Getter
        @Setter
        private String name;

        @Getter
        @Setter
        private String username;

        @Getter
        @Setter
        private int gender;

        @Getter
        @Setter
        private int status;

        @Getter
        @Setter
        private String faviconUrl;

        public void setGenderWithString(String gender) {
            int val = 0;
            if (gender == null) {
                val = GenderType.UNKNOWN.ordinal();
            } else if (gender.equals("男") || gender.equals("m")) {
                val = GenderType.MALE.ordinal();
            } else if (gender.equals("女") || gender.equals("f")) {
                val = GenderType.FEMALE.ordinal();
            } else {
                val = GenderType.UNKNOWN.ordinal();
            }
            this.setGender(val);
        }

        public User getUser() {
            User user = new User();
            user.setName(this.name);
            user.setUsername(this.username);
            user.setGender(this.gender);
            user.setPassword(String.format(this.username,
                    "" + (int) (Math.random() * 1000000)));
            user.setType(UserType.TENANT.ordinal());
            user.setRegRole(RegRole.PC_TENANT);
            user.setStatus(this.status);
            user.setFaviconUrl(this.faviconUrl);
            return user;
        }
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class Auth {
        @Getter
        @Setter
        private String accessToken;

        @Getter
        @Setter
        private String uid;

        @Getter
        @Setter
        private String username;

        public void setUsernameWithUid(String uid, String platform) {
            this.setUsername(platform.toUpperCase() + "_" + uid);
        }
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReleaseStatus {
        @Getter
        @Setter
        private boolean isReturned = false;

        @Getter
        @Setter
        private User user = null;

        @Getter
        @Setter
        private long createdTimestamp;
    }
}
