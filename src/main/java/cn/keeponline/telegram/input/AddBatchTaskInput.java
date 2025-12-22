package cn.keeponline.telegram.input;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.util.List;


@Data
public class AddBatchTaskInput {

    private List<String> uids;

    @NotNull(message = "发送间隔不能为空")
    private Integer sendInterval;

    private String messageContent;

    private MultipartFile file;

    private Integer cvsType;
}
