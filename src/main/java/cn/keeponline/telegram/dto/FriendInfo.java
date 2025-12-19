package cn.keeponline.telegram.dto;

import lombok.Data;

import java.util.List;

@Data
public class FriendInfo {
    private List<FriendDTO> friends;
}