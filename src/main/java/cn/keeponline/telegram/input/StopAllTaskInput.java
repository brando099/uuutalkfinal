package cn.keeponline.telegram.input;

import lombok.Data;

import java.util.List;

@Data
public class StopAllTaskInput {
    private List<String> uids;
}
