package cn.keeponline.telegram.service;


import cn.keeponline.telegram.dto.uuudto.UUUGroupDTO;
import cn.keeponline.telegram.dto.uuudto.UUUGroupMemberDTO;
import cn.keeponline.telegram.entity.UserTask;
import cn.keeponline.telegram.input.*;

import java.io.IOException;
import java.util.List;

public interface TaskService {
    void addBatch(AddBatchTaskInput addBatchTaskInput) throws Exception;
    void asyncRestartTask(UserTask userTask) throws Exception;
    void stopTask(StopTaskInput stopTaskInput) throws Exception;
    void stopAllTask(StopAllTaskInput stopAllTaskInput) throws Exception;
    void updateFrequency(UpdateFrequencyInput updateFrequencyInput) throws Exception;
    List<UUUGroupDTO> getGroups(String token) throws IOException;
    void addFriends(String groupId, String remark, String uid) throws IOException, InterruptedException;
    void addFriendsExecute(String groupId, String remark, String uid, List<UUUGroupMemberDTO> uuuGroupMemberDTOS, String token, Integer addWait) throws InterruptedException;
}