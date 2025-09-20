package pro.ra_tech.giga_ai_agent.domain.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pro.ra_tech.giga_ai_agent.domain.api.PdfService;
import pro.ra_tech.giga_ai_agent.integration.api.HfsService;
import pro.ra_tech.giga_ai_agent.integration.api.KafkaDocProcessingTaskHandler;
import pro.ra_tech.giga_ai_agent.integration.api.KafkaService;
import pro.ra_tech.giga_ai_agent.integration.kafka.model.DocumentProcessingTask;

@Slf4j
@RequiredArgsConstructor
public class KafkaDocProcessingTaskHandlerImpl implements KafkaDocProcessingTaskHandler {
    private final String baseFolder;
    private final HfsService hfsService;
    private final PdfService pdfService;
    private final KafkaService kafka;

    @Override
    public void onDocumentProcessingTask(DocumentProcessingTask task) {
        log.info("Handling document processing task: {}", task);

        hfsService.downloadFile(baseFolder, task.hfsDocumentId())
                .flatMap(pdfService::splitToChunks);
//                .flatMap(chunks -> kafka.);
    }
}
