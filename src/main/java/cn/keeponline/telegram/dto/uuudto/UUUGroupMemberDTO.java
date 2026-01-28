package cn.keeponline.telegram.dto.uuudto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class UUUGroupMemberDTO {

    private Long id;
    private String uid;

    @JsonProperty("group_no")
    private String groupNo;

    private String name;
    private String remark;
    private Integer role;
    private Long version;

    @JsonProperty("is_deleted")
    private Integer isDeleted;

    private Integer status;

    @JsonProperty("is_destroy")
    private Integer isDestroy;

    private String vercode;

    @JsonProperty("invite_uid")
    private String inviteUid;

    private Integer robot;

    @JsonProperty("forbidden_expir_time")
    private Long forbiddenExpirTime;

    @JsonProperty("is_special_short_no")
    private Integer isSpecialShortNo;

    @JsonProperty("sign_list")
    private List<Object> signList;

    private Integer vip;

    @JsonProperty("vip_expire")
    private Long vipExpire;

    private String avatar;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("updated_at")
    private String updatedAt;

    @JsonProperty("user_status")
    private Integer userStatus;

    @JsonProperty("setting_blacklist")
    private Integer settingBlacklist;

    private String short_no;

    // 是否已经是好友了
    private Integer follow;
}
