package pro.ra_tech.giga_ai_agent.domain.impl;

import io.micrometer.core.annotation.Timed;
import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import pro.ra_tech.giga_ai_agent.database.repos.api.DocProcessingTaskRepository;
import pro.ra_tech.giga_ai_agent.database.repos.api.SourceRepository;
import pro.ra_tech.giga_ai_agent.database.repos.impl.Transactional;
import pro.ra_tech.giga_ai_agent.domain.api.EmbeddingService;
import pro.ra_tech.giga_ai_agent.domain.api.PdfService;
import pro.ra_tech.giga_ai_agent.domain.api.TagService;
import pro.ra_tech.giga_ai_agent.domain.model.DocumentData;
import pro.ra_tech.giga_ai_agent.domain.model.EnqueueDocumentInfo;
import pro.ra_tech.giga_ai_agent.domain.model.InputDocumentMetadata;
import pro.ra_tech.giga_ai_agent.domain.model.PdfProcessingInfo;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;
import pro.ra_tech.giga_ai_agent.failure.DocumentProcessingFailure;
import pro.ra_tech.giga_ai_agent.integration.api.HfsService;
import pro.ra_tech.giga_ai_agent.integration.api.KafkaService;
import pro.ra_tech.giga_ai_agent.integration.api.LlmTextProcessorService;
import pro.ra_tech.giga_ai_agent.integration.config.hfs.HfsProps;
import pro.ra_tech.giga_ai_agent.integration.kafka.model.DocumentType;

import java.util.List;

@Service
@Slf4j
public class PdfServiceImpl extends BaseDocumentService implements PdfService {
    public PdfServiceImpl(
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

    private AppFailure toFailure(Throwable cause) {
        return toFailure(DocumentProcessingFailure.Code.PDF_PROCESSING_FAILURE, cause);
    }

    @Override
    @Timed(
            value = "business.process.call",
            extraTags = {"business.process.service", "pdf-service", "business.process.method", "handle-pdf"},
            histogram = true,
            percentiles = {0.90, 0.95, 0.99}
    )
    public Either<AppFailure, PdfProcessingInfo> handlePdf(
            byte[] contents,
            List<String> tags,
            String name,
            String description
    ) {
        return toText(contents)
                .peek(text -> log.info("Got text with length {} from pdf", format.format(text.length())))
                .flatMap(llmService::splitText)
                .peek(chunks -> log.info("Got {} chunks from llm text processor", chunks.size()))
                .map(chunks -> new DocumentData(name, description, tags, chunks))
                .flatMap(embeddingService::createEmbeddings)
                .map(PdfProcessingInfo::new);
    }

    @Override
    @Timed(
            value = "business.process.call",
            extraTags = {"business.process.service", "pdf-service", "business.process.method", "enqueue-pdf"},
            histogram = true,
            percentiles = {0.90, 0.95, 0.99}
    )
    public Either<AppFailure, EnqueueDocumentInfo> enqueue(
            byte[] contents,
            InputDocumentMetadata meta
    ) {
        return enqueue(contents, meta, DocumentType.PDF);
    }

    @Override
    public Either<AppFailure, List<String>> splitToChunks(byte[] contents) {
        return toText(contents).flatMap(llmService::splitText);
    }

    private Either<AppFailure, String> toText(byte[] contents) {
        return Try.withResources(() -> Loader.loadPDF(contents))
                .of(document -> {
                    val stripper = new PDFTextStripper();
                    return stripper.getText(document);
                })
                .toEither()
                .mapLeft(this::toFailure);
    }
}
