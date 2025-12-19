package cn.keeponline.telegram.service.impl;

import cn.keeponline.telegram.dto.MessageDTO;
import cn.keeponline.telegram.dto.MessageDetailPageDTO;
import cn.keeponline.telegram.entity.Message;
import cn.keeponline.telegram.mapper.MessageMapper;
import cn.keeponline.telegram.service.MessageService;
import cn.keeponline.telegram.vo.MessageVO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageMapper messageMapper;
    @Override
    public MessageVO getMessageList(Long userId, Integer pageNumber, Integer pageSize) {
        List<Message> list = messageMapper.listByUserId(userId);
        Map<Long, Message> map = new LinkedHashMap<>();
        for (Message message : list) {
            Long otherId = message.getOtherId();
            if (!map.containsKey(otherId)) {
                map.put(otherId, message);
            }
        }
        int total = map.size();
        List<Message> groupedList = new ArrayList<>();
        for (Long otherId : map.keySet()) {
            Message message = map.get(otherId);
            groupedList.add(message);
        }
        int start = (pageNumber - 1) * pageSize;
        List<Message> pagedList = groupedList.stream().skip(start).limit(pageSize).collect(Collectors.toList());

        List<MessageDTO> dtoList = new ArrayList<>();

        if (pageNumber == 1) {
            Message message = messageMapper.listByUserIdAndOtherIdLimit1(userId, userId);
            if (message != null) {
                MessageDTO messageDTO = MessageDTO.builder()
                        .otherId(userId)
                        .sendTime(message.getSendTime())
                        .lastMessage(message.getMessageContent())
                        .build();
                dtoList.add(messageDTO);
            }
        }

        for (Message message : pagedList) {
            MessageDTO messageDTO = MessageDTO.builder()
                    .otherId(message.getOtherId())
                    .sendTime(message.getSendTime())
                    .lastMessage(message.getMessageContent())
                    .build();
            dtoList.add(messageDTO);
        }
        MessageVO messageVO = new MessageVO();
        messageVO.setList(dtoList);
        messageVO.setTotal(total);

        return messageVO;
    }

    @Override
    public PageInfo<MessageDetailPageDTO> listMessageDetail(Long userId, Long otherId, Integer pageNumber, Integer pageSize) {
        PageHelper.startPage(pageNumber, pageSize);
        List<Message> list = messageMapper.listByUserIdAndOtherId(userId, otherId);

        List<MessageDetailPageDTO> dtoList = new ArrayList<>();
        for (Message message : list) {
            MessageDetailPageDTO messageDetailPageDTO = MessageDetailPageDTO.builder()
                    .messageContent(message.getMessageContent())
                    .sendTime(message.getSendTime())
                    .fromId(message.getFromId())
                    .build();
            dtoList.add(messageDetailPageDTO);
        }

        PageInfo pageInfo = new PageInfo(list);
        pageInfo.setList(dtoList);
        return pageInfo;
    }
}
