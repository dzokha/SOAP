package vn.dzokha.FastQC.Mappers;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import vn.dzokha.FastQC.Modules.Shared.BasicStats;
import vn.dzokha.FastQC.Modules.Shared.BasicStatsDTO;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BasicStatsMapper {
    // Để MapStruct tự tìm các trường trùng tên (nếu có). 
    // Nếu không trùng, nó sẽ để null/default nhờ ReportingPolicy.IGNORE
    BasicStatsDTO toDTO(BasicStats basicStats);
}