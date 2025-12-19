package cn.keeponline.telegram.input;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;


@Data
public class UpdateFrequencyInput {

    private List<String> uids;

    @NotNull(message = "发送间隔不能为空")
    private Integer sendInterval;



}
