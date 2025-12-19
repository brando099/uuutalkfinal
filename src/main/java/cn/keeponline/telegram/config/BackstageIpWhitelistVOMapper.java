package cn.keeponline.telegram.config;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author TianYang.Pu
 * @version 1.0  ,2019.11.14
 */
@Mapper
public interface BackstageIpWhitelistVOMapper {

    BackstageIpWhitelistVOMapper INSTANCE = Mappers.getMapper(BackstageIpWhitelistVOMapper.class);

    @Mappings({
            @Mapping(source = "ipAddress", target = "ipAddress"),
            @Mapping(source = "detail", target = "detail"),
            @Mapping(source = "createDate",target = "addTime"),
            @Mapping(source = "status",target = "status"),
            @Mapping(source = "id", target = "id"),
            @Mapping(source = "modifyDate", target = "modifyDate"),
            @Mapping(source = "no", target = "no")
    })
    BackstageIpWhitelistVO domain2VO(BackstageIpWhitelistOutputDTO backstageIpWhitelistOutputDTO);

    List<BackstageIpWhitelistVO> domain2VO(List<BackstageIpWhitelistOutputDTO> backstageIpWhitelistOutputDTOList);

}
