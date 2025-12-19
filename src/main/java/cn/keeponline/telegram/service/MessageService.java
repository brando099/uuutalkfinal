package cn.keeponline.telegram.service;

import cn.keeponline.telegram.dto.MessageDetailPageDTO;
import cn.keeponline.telegram.vo.MessageVO;
import com.github.pagehelper.PageInfo;

/**
 * 运营平台用户(SysUser)表服务接口
 *
 * @author makejava
 * @since 2021-03-30 17:03:58
 */
public interface MessageService {
    /**
     * 通过userId查询用户聊天列表
     */
    MessageVO getMessageList(Long userId, Integer pageNumber, Integer pageSize);

    /**
     * 获取聊天详情页 消息列表
     */
    PageInfo<MessageDetailPageDTO> listMessageDetail(Long userId, Long otherId, Integer pageNumber, Integer pageSize);
}