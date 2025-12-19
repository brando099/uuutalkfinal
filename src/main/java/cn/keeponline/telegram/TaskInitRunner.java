package cn.keeponline.telegram;

import cn.keeponline.telegram.entity.UserTask;
import cn.keeponline.telegram.mapper.UserTaskMapper;
import cn.keeponline.telegram.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class TaskInitRunner implements ApplicationRunner {

    @Autowired
    private UserTaskMapper userTaskMapper;

    @Autowired
    private TaskService taskService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<UserTask> userTasks = userTaskMapper.getByStatus(1);
        log.info("需要添加的任务数量: {}", userTasks.size());
        for (UserTask userTask : userTasks) {
            taskService.asyncRestartTask(userTask);
        }
    }
}