package pro.ra_tech.giga_ai_agent.domain.impl;

import io.micrometer.core.annotation.Timed;
import io.vavr.control.Either;
import org.springframework.stereotype.Service;
import pro.ra_tech.giga_ai_agent.database.repos.api.DocProcessingTaskRepository;
import pro.ra_tech.giga_ai_agent.database.repos.api.SourceRepository;
import pro.ra_tech.giga_ai_agent.database.repos.impl.Transactional;
import pro.ra_tech.giga_ai_agent.domain.api.EmbeddingService;
import pro.ra_tech.giga_ai_agent.domain.api.TagService;
import pro.ra_tech.giga_ai_agent.domain.api.TxtService;
import pro.ra_tech.giga_ai_agent.domain.model.EnqueueDocumentInfo;
import pro.ra_tech.giga_ai_agent.domain.model.InputDocumentMetadata;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;
import pro.ra_tech.giga_ai_agent.integration.api.HfsService;
import pro.ra_tech.giga_ai_agent.integration.api.KafkaService;
import pro.ra_tech.giga_ai_agent.integration.api.LlmTextProcessorService;
import pro.ra_tech.giga_ai_agent.integration.config.hfs.HfsProps;
import pro.ra_tech.giga_ai_agent.integration.kafka.model.DocumentType;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class TxtServiceImpl extends BaseDocumentService implements TxtService {
    public TxtServiceImpl(
            LlmTextProcessorService llmService,
            EmbeddingService embeddingService,
            HfsService hfsService,
            KafkaService kafkaService,
            HfsProps hfsProps,
            DocProcessingTaskRepository taskRepo,
            SourceRepository sourceRepo,
            TagService tagsService,
            Transactional trx
    ) {
        super(llmService, embeddingService, hfsService, kafkaService, hfsProps, taskRepo, sourceRepo, tagsService, trx);
    }

    @Override
    @Timed(
            value = "business.process.call",
            extraTags = {"business.process.service", "txt-service", "business.process.method", "enqueue-txt"},
            histogram = true,
            percentiles = {0.90, 0.95, 0.99}
    )
    public Either<AppFailure, EnqueueDocumentInfo> enqueue(byte[] contents, InputDocumentMetadata meta) {
        return enqueue(contents, meta, DocumentType.TXT);
    }

    @Override
    public Either<AppFailure, List<String>> splitToChunks(byte[] contents) {
        return llmService.splitText(new String(contents, StandardCharsets.UTF_8));
    }
}
