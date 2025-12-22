package cn.keeponline.telegram.input;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;


@Data
public class AddTaskInput {
    @NotEmpty(message = "uid不能为空")
    private String uid;

    @NotNull(message = "发送间隔不能为空")
    private Integer sendInterval;

    private String messageContent;

    private MultipartFile file;

    @NotNull(message = "cvsType不能为空")
    private Integer cvsType;
}
