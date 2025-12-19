package cn.keeponline.telegram.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

@Data
public class SystemConfigs {
    @TableId(value = "id", type = IdType.UUID)
    private String id;

    private String key;

    private String value;

    private Date createTime;

    private Date modifyTime;

}
