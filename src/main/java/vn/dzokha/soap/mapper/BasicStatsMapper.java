package vn.dzokha.soap.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import vn.dzokha.soap.engine.shared.BasicStats;
import vn.dzokha.soap.dto.BasicStatsDTO;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BasicStatsMapper {
    // Để MapStruct tự tìm các trường trùng tên (nếu có). 
    // Nếu không trùng, nó sẽ để null/default nhờ ReportingPolicy.IGNORE
    BasicStatsDTO toDTO(BasicStats basicStats);
}