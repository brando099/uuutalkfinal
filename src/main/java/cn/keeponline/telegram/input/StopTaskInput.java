package cn.keeponline.telegram.input;

import lombok.Data;

@Data
public class StopTaskInput {
    private String uid;

    private Integer cvsType;
}
