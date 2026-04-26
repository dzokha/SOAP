
package vn.dzokha.soap.engine.shared;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.core.io.ResourceLoader; 

import vn.dzokha.soap.config.SOAPProperties;
import vn.dzokha.soap.engine.adapter.AdapterContent;
import vn.dzokha.soap.engine.kmer.KmerContent;
import vn.dzokha.soap.model.sequence.contaminant.ContaminentFinder;

import vn.dzokha.soap.engine.shared.BasicStats;
import vn.dzokha.soap.engine.shared.PerBaseQualityScores;
import vn.dzokha.soap.engine.shared.PerTileQualityScores;
import vn.dzokha.soap.engine.shared.PerSequenceQualityScores;
import vn.dzokha.soap.engine.shared.PerBaseSequenceContent;
import vn.dzokha.soap.engine.shared.PerSequenceGCContent;
import vn.dzokha.soap.engine.shared.NContent;
import vn.dzokha.soap.engine.shared.SequenceLengthDistribution;
import vn.dzokha.soap.engine.shared.OverRepresentedSeqs;


/**
 * ModuleFactory đã được cải tiến thành Spring Component để 
 * tự động tiêm cấu hình (Dependency Injection) vào các Module.
 */
@Component
public class ModuleFactory {

    private final SOAPProperties properties;
    private final ModuleConfig moduleConfig;
    private final ContaminentFinder contaminentFinder;
    private final ResourceLoader resourceLoader;

    // 1. Inject tất cả các phụ thuộc cần thiết vào Factory
    @Autowired
    public ModuleFactory(SOAPProperties properties, 
                         ModuleConfig moduleConfig, 
                         ContaminentFinder contaminentFinder,
                         ResourceLoader resourceLoader) {
        this.properties = properties;
        this.moduleConfig = moduleConfig;
        this.contaminentFinder = contaminentFinder;
        this.resourceLoader = resourceLoader;
    }

    /**
     * Khởi tạo danh sách các module QC tiêu chuẩn.
     * Mỗi module giờ đây nhận cấu hình tập trung để xử lý logic (ngưỡng lỗi, grouping...).
     */
    public QCModule[] getStandardModuleList() {

        OverRepresentedSeqs os = new OverRepresentedSeqs(moduleConfig, properties, contaminentFinder);
        
        QCModule[] module_list = new QCModule[] {
            new BasicStats(), 
            new PerBaseQualityScores(moduleConfig, properties),
            new PerTileQualityScores(moduleConfig, properties),
            new PerSequenceQualityScores(moduleConfig),
            new PerBaseSequenceContent(moduleConfig, properties),
            new PerSequenceGCContent(moduleConfig),
            new NContent(moduleConfig, properties),
            new SequenceLengthDistribution(properties),
        
            os.duplicationLevelModule(),
            os,
            new AdapterContent(properties, moduleConfig, resourceLoader),            
            new KmerContent(properties, moduleConfig)
        };
    
        return module_list;
    }
}

