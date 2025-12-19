package cn.keeponline.telegram.service;


import cn.keeponline.telegram.entity.UserTask;
import cn.keeponline.telegram.input.*;

public interface TaskService {
    void asyncAddTask(AddTaskInput addTaskInput) throws Exception;
    void addBatch(AddBatchTaskInput addBatchTaskInput) throws Exception;
    void asyncRestartTask(UserTask userTask) throws Exception;
    void stopTask(StopTaskInput stopTaskInput) throws Exception;
    void stopAllTask(StopAllTaskInput stopAllTaskInput) throws Exception;
    void updateFrequency(UpdateFrequencyInput updateFrequencyInput) throws Exception;
}