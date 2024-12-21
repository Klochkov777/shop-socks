package by.klochkov.shop_socks.mapper;

import by.klochkov.shop_socks.dto.SockCsvDto;
import by.klochkov.shop_socks.dto.SockDto;
import by.klochkov.shop_socks.dto.UpdateSockDto;
import by.klochkov.shop_socks.model.Sock;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SockMapper {
    Sock toEntity(SockDto sockDto);
    SockDto toDto(Sock sock);
    Sock toEntity(SockCsvDto sockDto);
    List<Sock> toListSocks(List<SockCsvDto> socks);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateSockFromDto(UpdateSockDto updateSockDto, @MappingTarget Sock sock);
}
