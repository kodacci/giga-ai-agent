package pro.ra_tech.giga_ai_agent.domain.impl;

import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.val;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import pro.ra_tech.giga_ai_agent.domain.api.PdfService;
import pro.ra_tech.giga_ai_agent.domain.model.PdfProcessingInfo;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;
import pro.ra_tech.giga_ai_agent.failure.DocumentProcessingFailure;

@Service
public class PdfServiceImpl implements PdfService {
    private AppFailure toFailure(Throwable cause) {
        return new DocumentProcessingFailure(
                DocumentProcessingFailure.Code.PDF_PROCESSING_FAILURE,
                getClass().getName(),
                cause
        );
    }

    @Override
    public Either<AppFailure, PdfProcessingInfo> handlePdf(byte[] contents) {
        return toText(contents).map(text -> new PdfProcessingInfo(
                text, text.length(), 0
        ));
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
