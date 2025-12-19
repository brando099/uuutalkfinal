package cn.keeponline.telegram.dto.ws;

import cn.keeponline.telegram.dto.MsgBodyDTO;
import cn.keeponline.telegram.dto.MsgHeadDTO;
import lombok.Data;

@Data
public class GmsgDTO {
    private MsgHeadDTO msg_head;
    private MsgBodyDTO msg_body;
}
