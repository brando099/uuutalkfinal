package cn.keeponline.telegram.component;

import cn.keeponline.telegram.dto.uuudto.UUUFriendDTO;
import cn.keeponline.telegram.dto.uuudto.UUUGroupDTO;
import cn.keeponline.telegram.entity.SendRecord;
import cn.keeponline.telegram.entity.UserInfo;
import cn.keeponline.telegram.entity.UserPackage;
import cn.keeponline.telegram.mapper.SendRecordMapper;
import cn.keeponline.telegram.mapper.UserInfoMapper;
import cn.keeponline.telegram.mapper.UserPackageMapper;
import cn.keeponline.telegram.talktools.services.UuutalkApiClient;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class AsyncComponent {

    private static final String SEND_RECORD_KEY_PREFIX = "sendRecord:";
    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private UserPackageMapper userPackageMapper;

    @Qualifier("sendRecordMapper")
    @Autowired
    private SendRecordMapper sendRecordMapper;

    @Autowired
    @Qualifier("redisTemplate1")
    private RedisTemplate<String, Object> redisTemplate;

    @Async("asyncTaskExecutor")
    public void checkQRCode(String uuid, Long expire, String outId, Long packageId) throws Exception {
        while (true) {
            Thread.sleep(3000);
            if (new Date().getTime() > expire) {
                log.info("长时间未扫码，退出");
                break;
            }
            UuutalkApiClient uuutalkApiClient = new UuutalkApiClient();
            Map<String, String> map = uuutalkApiClient.getLoginStatus(uuid);
            String status = map.get("status");
            log.info("status: {}", status);

            if ("expired".equals(status)) {
                break;
            } else if ("authed".equals(status)) {
                log.info("【uuutalk】login success");
                String authCode = map.get("auth_code");
                Map<String, String> authMap = uuutalkApiClient.loginWithAuthCode(authCode);
                log.info("authInfo: {}", JSON.toJSONString(authMap));
                String token = authMap.get("token");
                String uid = authMap.get("uid");
                String nickname = authMap.get("name");
                String username = authMap.get("username");
                List<UUUGroupDTO> groups = uuutalkApiClient.getGroups(token);
                List<UUUFriendDTO> friends = uuutalkApiClient.getFriends(token);
//                log.info("groups: {}", JSON.toJSONString(groups));
//                log.info("friends: {}", JSON.toJSONString(friends));
                int groupSize = groups.size();
                int friendSize = friends.size() - 2;

                UserInfo user = userInfoMapper.getByUid(uid);
                if (user != null) {
                    log.info("重复扫码，删除旧数据");
                    userInfoMapper.deleteById(user.getId());
                    Long packageIdOld = user.getPackageId();
                    UserPackage userPackage = userPackageMapper.selectById(packageIdOld);
                    userPackage.setStatus(0);
                    userPackageMapper.updateById(userPackage);
                }

                UserInfo userInfo = new UserInfo();
                userInfo.setUid(uid);
                userInfo.setAccountId(outId);
                userInfo.setPackageId(packageId);
                userInfo.setToken(token);
                userInfo.setUuid(uuid);
                userInfo.setNickname(nickname);
                userInfo.setMobile(username);
                userInfo.setGroupSize(groupSize);
                userInfo.setFriendSize(friendSize);
                userInfoMapper.insert(userInfo);
                log.info("添加uuutalk账号成功: {}", JSON.toJSONString(userInfo));

                UserPackage userPackage = userPackageMapper.selectById(packageId);
                userPackage.setUid(uid);
                userPackage.setStatus(1);
                if (userPackageMapper.updateById(userPackage) == 1) {
                    log.info("修改套餐状态成功: {}", JSON.toJSONString(userPackage));
                }
                break;
            }
        }
    }

    Map<String, String> map = new ConcurrentHashMap<>();

    @Async("asyncTaskExecutor")
    public void insert(SendRecord sendRecord) throws Exception {
        sendRecordMapper.insert(sendRecord);
//        log.info("插入成功: {}", sendRecord);
    }

    @Async("asyncTaskExecutor")
    public void insertSendRecord(SendRecord sendRecord) {
        String key = SEND_RECORD_KEY_PREFIX + sendRecord.getUid();
        redisTemplate.opsForHash().put(key, sendRecord.getId(), sendRecord);
    }

    @Async("asyncTaskExecutor")
    public void syncCountInfo(List<UserInfo> users) throws Exception {
        String accountId = users.get(0).getAccountId();
        if (map.get(accountId) != null) {
            return;
        }
        map.put(accountId, accountId);
        try {
            extracted(users);
        } finally {
            map.remove(accountId);
        }
    }

    private void extracted(List<UserInfo> users) throws Exception {
        UuutalkApiClient uuutalkApiClient = new UuutalkApiClient();
        for (UserInfo user : users) {
            if (user.getStatus() == 0) {
                continue;
            }
            String token = user.getToken();
            List<UUUFriendDTO> friends = uuutalkApiClient.getFriends(token);
//            String result = SendMessage.getGroupListCheck(uid, token);
            if (friends == null) {
                log.info("账号异常");
                user.setStatus(0);
                if (userInfoMapper.updateById(user) == 1) {
                    log.info("状态失效，修改用户信息成功: {}", JSON.toJSONString(user));
                }
                continue;
            }

//            List<FriendDTO> friendList = SendMessage.getFriendList(uid, token);
            user.setGroupSize(uuutalkApiClient.getGroups(token).size());
            user.setFriendSize(friends.size() - 2);
            userInfoMapper.updateById(user);
        }
    }
}
