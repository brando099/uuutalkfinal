package cn.keeponline.telegram.dto;

import lombok.Data;
import java.util.List;

@Data
public class GroupInfo {
    private List<GroupDTO> groups;
}