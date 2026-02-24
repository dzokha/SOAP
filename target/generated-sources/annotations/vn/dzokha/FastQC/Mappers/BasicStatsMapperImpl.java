package vn.dzokha.FastQC.Mappers;

import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import vn.dzokha.FastQC.Modules.Shared.BasicStats;
import vn.dzokha.FastQC.Modules.Shared.BasicStatsDTO;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-02-25T02:16:14+0700",
    comments = "version: 1.6.0.Beta1, compiler: javac, environment: Java 25.0.1 (Homebrew)"
)
@Component
public class BasicStatsMapperImpl implements BasicStatsMapper {

    @Override
    public BasicStatsDTO toDTO(BasicStats basicStats) {
        if ( basicStats == null ) {
            return null;
        }

        String filename = null;
        String fileType = null;
        String encoding = null;
        long totalSequences = 0L;
        long sequencesFlagged = 0L;
        double gcContent = 0.0d;

        BasicStatsDTO basicStatsDTO = new BasicStatsDTO( filename, fileType, encoding, totalSequences, sequencesFlagged, gcContent );

        return basicStatsDTO;
    }
}
