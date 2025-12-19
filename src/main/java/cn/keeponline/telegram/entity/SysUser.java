package cn.keeponline.telegram.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
public class SysUser  {
    @TableId(type = IdType.UUID)
    private String id;
    /**
     * 业务id
     */
    @ApiModelProperty(notes = "编号")
    private String outId;
    /**
     * 用户名
     */
    @ApiModelProperty(notes = "用户名")
    private String username;
    /**
     * 密码
     */
    @JsonIgnore
    private String password;
    /**
     * 状态
     */
    @ApiModelProperty(notes = "状态")
    private Integer status;

    /**
     * 谷歌令牌
     */
    @ApiModelProperty(notes = "谷歌令牌")
    private String secretKey;

    /**
     * 谷歌令牌是否启用
     */
    @ApiModelProperty(notes = "谷歌令牌是否启用")
    private Boolean googleAuthEnabled;

    private Date creatTime;

    private Date modifyTime;

}
