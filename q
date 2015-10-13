[1mdiff --git a/src/main/java/room107/service/house/HouseServiceImpl.java b/src/main/java/room107/service/house/HouseServiceImpl.java[m
[1mindex 2360512..b8815a9 100644[m
[1m--- a/src/main/java/room107/service/house/HouseServiceImpl.java[m
[1m+++ b/src/main/java/room107/service/house/HouseServiceImpl.java[m
[36m@@ -63,7 +63,7 @@[m [mpublic class HouseServiceImpl implements IHouseService {[m
 [m
     @Autowired[m
     private IHouseDao houseDao;[m
[31m-    [m
[32m+[m[41m[m
     @Autowired[m
     private IExternalHouseDao externalHouseDao;[m
 [m
[36m@@ -195,14 +195,15 @@[m [mpublic class HouseServiceImpl implements IHouseService {[m
         Validate.notNull(house);[m
         Validate.notEmpty(rooms);[m
         String username = user.getUsername();[m
[31m-        [m
[31m-        //validate house by username[m
[32m+[m[41m[m
[32m+[m[32m        // validate house by username[m[41m[m
         List<House> oldHouse = houseDao.getHouses(username);[m
         if (CollectionUtils.isNotEmpty(oldHouse)) {[m
[31m-            log.error("add a duplicated house for username " + username + ", discard.");[m
[32m+[m[32m            log.error("add a duplicated house for username " + username[m[41m[m
[32m+[m[32m                    + ", discard.");[m[41m[m
             return oldHouse.get(0).getId();[m
         }[m
[31m-        [m
[32m+[m[41m[m
         // house[m
         house.setUsername(username);[m
         house.setName(HouseUtils.formatStruct(house.getRoomNumber(),[m
[36m@@ -277,6 +278,14 @@[m [mpublic class HouseServiceImpl implements IHouseService {[m
                 house.getPosition());[m
         oldHouse.setArea(house.getArea());[m
         oldHouse.setCity(house.getCity());[m
[32m+[m[32m        boolean descriptionChanged = !StringUtils.equals([m[41m[m
[32m+[m[32m                oldHouse.getDescription(), house.getDescription());[m[41m[m
[32m+[m[32m        if (descriptionChanged) {[m[41m[m
[32m+[m[32m            EmailUtils.sendAdminMail([m[41m[m
[32m+[m[32m                    "Update house:the description information is changed",[m[41m[m
[32m+[m[32m                    "houseId=" + house.getId() + ", username=" + username[m[41m[m
[32m+[m[32m                            + ", description=" + house.getDescription());[m[41m[m
[32m+[m[32m        }[m[41m[m
         oldHouse.setDescription(house.getDescription());[m
         oldHouse.setFacilities(house.getFacilities());[m
         oldHouse.setFloor(house.getFloor());[m
[36m@@ -298,9 +307,6 @@[m [mpublic class HouseServiceImpl implements IHouseService {[m
         oldHouse.setModifiedTime(new Date());[m
         oldHouse.setRequiredGender(house.getRequiredGender());[m
         houseDao.updateHouse(oldHouse, positionChanged);[m
[31m-        EmailUtils.sendAdminMail("Update house",[m
[31m-                "houseId=" + house.getId() + ", username=" + username[m
[31m-                        + ", description=" + house.getDescription());[m
     }[m
 [m
     @Override[m
[36m@@ -312,7 +318,7 @@[m [mpublic class HouseServiceImpl implements IHouseService {[m
     public void update(Room room) {[m
         houseDao.update(room);[m
     }[m
[31m-    [m
[32m+[m[41m[m
     @Override[m
     public void updateRoomReportUsers(long houseId, long roomId, User user) {[m
         Room room = houseDao.getRoom(roomId);[m
[36m@@ -323,13 +329,15 @@[m [mpublic class HouseServiceImpl implements IHouseService {[m
         UserBehaviorLog.HOUSE_UPDATE.info("Update room reportUsers: , roomId="[m
                 + roomId + ", reportUsers=" + newUsers);[m
         houseDao.updateRoom(room);[m
[31m-        EmailUtils.sendAdminMail("举报房子已经出租", "该房子已经出租，分租或不限类型，houseID=" + houseId[m
[31m-                + "，roomID=" + roomId + "，userName=" + user.getUsername());[m
[32m+[m[32m        EmailUtils.sendAdminMail("举报房子已经出租",[m[41m[m
[32m+[m[32m                "该房子已经出租，分租或不限类型，houseID=" + houseId + "，roomID=" + roomId[m[41m[m
[32m+[m[32m                        + "，userName=" + user.getUsername());[m[41m[m
     }[m
[31m-    [m
[31m-    private String doNewusers (String oldUsers, String userId) {[m
[32m+[m[41m[m
[32m+[m[32m    private String doNewusers(String oldUsers, String userId) {[m[41m[m
         String newUsers = "";[m
[31m-        if (oldUsers == null) oldUsers = "";[m
[32m+[m[32m        if (oldUsers == null)[m[41m[m
[32m+[m[32m            oldUsers = "";[m[41m[m
         if (oldUsers.indexOf(userId) == -1) {[m
             long timestamp = System.currentTimeMillis();[m
             if ("".equals(oldUsers)) {[m
[36m@@ -421,6 +429,14 @@[m [mpublic class HouseServiceImpl implements IHouseService {[m
                         + username + ", roomId=" + oldRoom.getId());[m
                 houseDao.deleteRoom(oldRoom);[m
             } else { // update[m
[32m+[m[32m                boolean imageChanged = !StringUtils.equals(room.getImageIds(),[m[41m[m
[32m+[m[32m                        oldRoom.getImageIds());[m[41m[m
[32m+[m[32m                if (imageChanged) {[m[41m[m
[32m+[m[32m                    EmailUtils.sendAdminMail("the roomImage changed!",[m[41m[m
[32m+[m[32m                            "houseId: " + room.getHouseId() + " "[m[41m[m
[32m+[m[32m                                    + "username: " + username + " "[m[41m[m
[32m+[m[32m                                    + "roomImage changed!!");[m[41m[m
[32m+[m[32m                }[m[41m[m
                 oldRoom.setArea(room.getArea());[m
                 oldRoom.setImageIds(room.getImageIds());[m
                 oldRoom.setName(HouseUtils.getRoomType(room.getType()));[m
[36m@@ -436,7 +452,7 @@[m [mpublic class HouseServiceImpl implements IHouseService {[m
             return oldRoom;[m
         }[m
     }[m
[31m-    [m
[32m+[m[41m[m
     @Override[m
     public void updateHouseReportUsers(long houseId, User user) {[m
         House house = houseDao.getHouse(houseId);[m
[36m@@ -444,8 +460,9 @@[m [mpublic class HouseServiceImpl implements IHouseService {[m
         String oldUsers = house.getReportUsers();[m
         String newUsers = doNewusers(oldUsers, user.getId().toString());[m
         house.setReportUsers(newUsers);[m
[31m-        UserBehaviorLog.HOUSE_UPDATE.info("Update house reportUsers: , houseId="[m
[31m-                + houseId + ", reportUsers=" + newUsers);[m
[32m+[m[32m        UserBehaviorLog.HOUSE_UPDATE[m[41m[m
[32m+[m[32m                .info("Update house reportUsers: , houseId=" + houseId[m[41m[m
[32m+[m[32m                        + ", reportUsers=" + newUsers);[m[41m[m
         houseDao.updateHouse(house, false);[m
         EmailUtils.sendAdminMail("举报房子已经出租", "该房子已经出租，整租类型，houseID=" + houseId[m
                 + "，userName=" + user.getUsername());[m
[36m@@ -509,7 +526,7 @@[m [mpublic class HouseServiceImpl implements IHouseService {[m
         house.setImageId(imageId);[m
         houseDao.updateHouse(house, false);[m
     }[m
[31m-    [m
[32m+[m[41m[m
     private List<HouseAndUser> fillHouseAndUser(List<House> houses) {[m
         Map<String, House> name2House = new HashMap<String, House>();[m
         for (House house : houses) {[m
[36m@@ -524,15 +541,16 @@[m [mpublic class HouseServiceImpl implements IHouseService {[m
             h.setUser(user);[m
             id2Result.put(house.getId(), h);[m
         }[m
[31m-        List<ExternalHouse> ehs = externalHouseDao.getExternalHouseByHouseId(id2Result.keySet());[m
[32m+[m[32m        List<ExternalHouse> ehs = externalHouseDao[m[41m[m
[32m+[m[32m                .getExternalHouseByHouseId(id2Result.keySet());[m[41m[m
         for (ExternalHouse eh : ehs) {[m
             HouseAndUser h = id2Result.get(eh.getHouseId());[m
             h.setExternalHouse(eh);[m
         }[m
[31m-        [m
[32m+[m[41m[m
         List<HouseAndUser> result = new ArrayList<HouseAndUser>();[m
         result.addAll(id2Result.values());[m
[31m-        [m
[32m+[m[41m[m
         Collections.sort(result, new Comparator<HouseAndUser>() {[m
             @Override[m
             public int compare(HouseAndUser h1, HouseAndUser h2) {[m
[36m@@ -544,7 +562,7 @@[m [mpublic class HouseServiceImpl implements IHouseService {[m
                 return 0;[m
             }[m
         });[m
[31m-        [m
[32m+[m[41m[m
         return result;[m
     }[m
 [m
[36m@@ -558,35 +576,38 @@[m [mpublic class HouseServiceImpl implements IHouseService {[m
             return Collections.emptyList();[m
         }[m
     }[m
[31m-    [m
[32m+[m[41m[m
     @Override[m
     public List<HouseAndUser> getHouseAndUsers(String searchKey) {[m
         List<HouseAndUser> result = new ArrayList<HouseAndUser>();[m
[31m-        if (StringUtils.isEmpty(searchKey)) return result;[m
[32m+[m[32m        if (StringUtils.isEmpty(searchKey))[m[41m[m
[32m+[m[32m            return result;[m[41m[m
         try {[m
             List<House> houses = new ArrayList<House>();[m
[31m-            //by houseId[m
[32m+[m[32m            // by houseId[m[41m[m
             try {[m
                 Long id = Long.valueOf(searchKey);[m
                 House house = houseDao.getHouse(id);[m
[31m-                if (house != null) houses.add(house);[m
[31m-            } catch(Exception e) {}[m
[31m-            //by contact[m
[32m+[m[32m                if (house != null)[m[41m[m
[32m+[m[32m                    houses.add(house);[m[41m[m
[32m+[m[32m            } catch (Exception e) {[m[41m[m
[32m+[m[32m            }[m[41m[m
[32m+[m[32m            // by contact[m[41m[m
             houses.addAll(getHousesByContact(searchKey, searchKey, searchKey));[m
[31m-            //by userKey[m
[32m+[m[32m            // by userKey[m[41m[m
             List<User> users = userDao.getUserByKey(searchKey);[m
             if (CollectionUtils.isNotEmpty(users)) {[m
                 for (User user : users) {[m
                     houses.addAll(houseDao.getHouses(user.getUsername()));[m
                 }[m
             }[m
[31m-            //by encryptKey[m
[32m+[m[32m            // by encryptKey[m[41m[m
             String start = "http://107room.com/user/k";[m
             List<ExternalHouse> ehs = null;[m
             if (searchKey.startsWith(start)) {[m
                 String key = searchKey.substring(start.length());[m
                 ehs = externalHouseDao.getExternalHouseByEncryptKey(key);[m
[31m-                [m
[32m+[m[41m[m
             } else if (searchKey.startsWith("k")) {[m
                 String key = searchKey.substring(1);[m
                 ehs = externalHouseDao.getExternalHouseByEncryptKey(key);[m
[36m@@ -600,7 +621,7 @@[m [mpublic class HouseServiceImpl implements IHouseService {[m
                 }[m
             }[m
             result = fillHouseAndUser(houses);[m
[31m-        } catch(Exception e) {[m
[32m+[m[32m        } catch (Exception e) {[m[41m[m
             log.error(e, e);[m
         }[m
         return result;[m
[36m@@ -608,7 +629,8 @@[m [mpublic class HouseServiceImpl implements IHouseService {[m
 [m
     @Override[m
     public int getRejectedHouseCount() {[m
[31m-        return houseDao.getHouseCount(AuditStatus.REJECTED) + externalHouseDao.getHouseCount(AuditStatus.REJECTED);[m
[32m+[m[32m        return houseDao.getHouseCount(AuditStatus.REJECTED)[m[41m[m
[32m+[m[32m                + externalHouseDao.getHouseCount(AuditStatus.REJECTED);[m[41m[m
     }[m
 [m
     @Override[m
[36m@@ -662,9 +684,9 @@[m [mpublic class HouseServiceImpl implements IHouseService {[m
         }[m
         fillUser(result);[m
         searchInfo.setHouseResults(result);[m
[31m-        return searchInfo;[m
[32m+[m[32m        return searchInfo;[m[41m[m
     }[m
[31m-    [m
[32m+[m[41m[m
     private void fillUser(List<HouseResult> result) {[m
         List<String> usernames = new ArrayList<String>();[m
         for (HouseResult houseResult : result) {[m
[36m@@ -688,7 +710,7 @@[m [mpublic class HouseServiceImpl implements IHouseService {[m
         time = DateUtils.addWeeks(new Date(), -1);[m
         houseDao.close(UserStatus.ANONYMOUS, time);[m
     }[m
[31m-    [m
[32m+[m[41m[m
     @Override[m
     public void remindDeprecated(int week) {[m
         Date from = new Date();[m
[36m@@ -720,13 +742,15 @@[m [mpublic class HouseServiceImpl implements IHouseService {[m
                                             + week[m
                                             + "周。<br/><br/>"[m
                                             + "<span style=\"color:red;font-weight:bold;\">如已成功出租，请到理房页面关闭出租。</span><br/><br/>"[m
[31m-                                            + "为保证时效性，系统将在" + (3 - week)[m
[32m+[m[32m                                            + "为保证时效性，系统将在"[m[41m[m
[32m+[m[32m                                            + (3 - week)[m[41m[m
                                             + "周后自动关闭该房间。<br/><br/>"[m
[31m-                                            + "107间：http://107room.com<br/><br/>" + "如"[m
[32m+[m[32m                                            + "107间：http://107room.com<br/><br/>"[m[41m[m
[32m+[m[32m                                            + "如"[m[41m[m
                                             + (3 - week)[m
                                             + "周后尚未成功出租，也可在网站理房页面重新开放出租<br/><br/>"[m
[31m-                                            + "感谢你对107间的支持，让租房真实而简单！", HtmlEmail.class,[m
[31m-                                    EmailAccount.ADMIN);[m
[32m+[m[32m                                            + "感谢你对107间的支持，让租房真实而简单！",[m[41m[m
[32m+[m[32m                                    HtmlEmail.class, EmailAccount.ADMIN);[m[41m[m
                 }[m
             }[m
         } else if (week == 3) {[m
[36m@@ -742,10 +766,11 @@[m [mpublic class HouseServiceImpl implements IHouseService {[m
                                             + week[m
                                             + "周。<br/><br/>"[m
                                             + "<span style=\"color:red;font-weight:bold;\">为保证时效性，系统将自动关闭该房间。</span><br/><br/>"[m
[31m-                                            + "107间：http://107room.com<br/><br/>" + "如尚未成功出租，也可在网站理房页面重新开放出租。<br/><br/>"[m
[32m+[m[32m                                            + "107间：http://107room.com<br/><br/>"[m[41m[m
[32m+[m[32m                                            + "如尚未成功出租，也可在网站理房页面重新开放出租。<br/><br/>"[m[41m[m
                                             + "未来成功出租后也请到理房页面关闭出租，方便租客浏览有效房间。<br/><br/>"[m
[31m-                                            + "感谢你对107间的支持，让租房真实而简单！", HtmlEmail.class,[m
[31m-                                    EmailAccount.ADMIN);[m
[32m+[m[32m                                            + "感谢你对107间的支持，让租房真实而简单！",[m[41m[m
[32m+[m[32m                                    HtmlEmail.class, EmailAccount.ADMIN);[m[41m[m
                 }[m
             }[m
         }[m
[36m@@ -762,7 +787,8 @@[m [mpublic class HouseServiceImpl implements IHouseService {[m
     }[m
 