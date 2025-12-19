package cn.keeponline.telegram.component;

import cn.keeponline.telegram.dto.FriendDTO;
import cn.keeponline.telegram.dto.GroupDTO;
import cn.keeponline.telegram.dto.GroupInfo;
import cn.keeponline.telegram.dto.YResponse;
import cn.keeponline.telegram.entity.SendRecord;
import cn.keeponline.telegram.entity.UserInfo;
import cn.keeponline.telegram.entity.UserPackage;
import cn.keeponline.telegram.mapper.SendRecordMapper;
import cn.keeponline.telegram.mapper.UserInfoMapper;
import cn.keeponline.telegram.mapper.UserPackageMapper;
import cn.keeponline.telegram.test.SendMessage;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class AsyncComponent {
    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private UserPackageMapper userPackageMapper;

    @Qualifier("sendRecordMapper")
    @Autowired
    private SendRecordMapper sendRecordMapper;

    @Async("asyncTaskExecutor")
    public void checkQRCode(String uuid, Long expire, String outId, Long packageId) throws Exception {
        expire = expire * 1000;
        JSONObject poll;
        while (true) {
            Thread.sleep(3000);
            if (new Date().getTime() > expire) {
                log.info("长时间未扫码，退出");
                break;
            }
            String s = SendMessage.pollStatus(uuid);
            poll = JSONObject.parseObject(s);
            JSONObject data = poll.getJSONObject("data");
            if (data.getInteger("status") == 2) {
                log.info("【yuni】login success");
                String token = data.getString("token");
                JSONObject userJSONObject = SendMessage.getUserInfo(token);
                JSONObject userObject = userJSONObject.getJSONObject("data");
                String uid = userObject.getString("uid");
                String nickname = userObject.getString("nickname");
                String mobile = userObject.getString("mobile");

                List<GroupDTO> groupList = SendMessage.getGroupList(uid, token);
//                log.info("群组信息: {}", groupList);
                List<FriendDTO> friendList = SendMessage.getFriendList(uid, token);

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
                userInfo.setMobile(mobile);
                userInfo.setGroupSize(groupList.size());
                userInfo.setFriendSize(friendList.size());
                userInfoMapper.insert(userInfo);
                log.info("添加与你账号成功: {}", JSON.toJSONString(userInfo));

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
        for (UserInfo user : users) {
            if (user.getStatus() == 0) {
                continue;
            }
            String uid = user.getUid();
            String token = user.getToken();
            String result = SendMessage.getGroupListCheck(uid, token);
            JSONObject jsonObject = JSON.parseObject(result);
            if (jsonObject.getInteger("ec") != 200) {
                log.info("账号异常: {}", JSON.toJSONString(jsonObject));
                user.setStatus(0);
                if (userInfoMapper.updateById(user) == 1) {
                    log.info("状态失效，修改用户信息成功: {}", JSON.toJSONString(user));
                }
                continue;
            }
            List<GroupDTO> groups = JSON.parseObject(result, new TypeReference<YResponse<GroupInfo>>() {
            }).getData().getGroups();
            int groupSize = groups.size();
            List<FriendDTO> friendList = SendMessage.getFriendList(uid, token);
            user.setGroupSize(groupSize);
            user.setFriendSize(friendList.size());
            userInfoMapper.updateById(user);
        }
    }
}
