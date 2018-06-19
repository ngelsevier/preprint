package com.ssrn.search.papers_consumer;

import com.amazonaws.services.kinesis.clientlibrary.exceptions.InvalidStateException;
import com.amazonaws.services.kinesis.clientlibrary.exceptions.ShutdownException;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessorCheckpointer;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.v2.IRecordProcessor;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.v2.IShutdownNotificationAware;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.ShutdownReason;
import com.amazonaws.services.kinesis.clientlibrary.types.InitializationInput;
import com.amazonaws.services.kinesis.clientlibrary.types.ProcessRecordsInput;
import com.amazonaws.services.kinesis.clientlibrary.types.ShutdownInput;
import com.amazonaws.services.kinesis.model.Record;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssrn.search.domain.Paper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class PaperRecordProcessor implements IRecordProcessor, IShutdownNotificationAware {
    private final static Logger LOGGER = LoggerFactory.getLogger(PaperRecordProcessor.class);

    private final Consumer<List<Paper>> papersConsumer;
    private final boolean logIndividualPapers;
    private final ObjectMapper objectMapper = new ObjectMapper();

    PaperRecordProcessor(Consumer<List<Paper>> papersConsumer, boolean logIndividualPapers) {
        this.papersConsumer = papersConsumer;
        this.logIndividualPapers = logIndividualPapers;
    }

    @Override
    public void initialize(InitializationInput initializationInput) {
    }

    @Override
    public void processRecords(ProcessRecordsInput processRecordsInput) {
        try {
            List<Paper> papers = processRecordsInput.getRecords().stream()
                    .map(this::extractData)
                    .map(this::deserializeToPaper)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            papersConsumer.accept(papers);

            checkpoint(processRecordsInput.getCheckpointer());
        } catch (Throwable e) {
            LOGGER.error("Exception thrown whilst processing Kinesis records", e);
            throw e;
        }
    }

    @Override
    public void shutdownRequested(IRecordProcessorCheckpointer checkpointer) {
        LOGGER.info("Record processor requested to shut down");
    }

    @Override
    public void shutdown(ShutdownInput shutdownInput) {
        if (shutdownInput.getShutdownReason().equals(ShutdownReason.TERMINATE)) {
            checkpoint(shutdownInput.getCheckpointer());
        }

        LOGGER.info(String.format("Record processor shutting down with shutdown reason: %s", shutdownInput.getShutdownReason()));
    }

    private byte[] extractData(Record record) {
        byte[] data = record.getData().array();

        if (this.logIndividualPapers) {
            LOGGER.info(String.format("Extracted Kinesis record data - %s", new String(data).replace(":", "-")));
        }

        return data;
    }

    private Paper deserializeToPaper(byte[] recordContent) {
        try {
            return objectMapper.readValue(recordContent, Paper.class);
        } catch (Throwable suppressedException) {
            LOGGER.error(String.format("Exception thrown whilst deserializing Kinesis record data to %s. Record data was %s", Paper.class, new String(recordContent).replace(":", "-")), suppressedException);
            return null;
        }
    }

    private static void checkpoint(IRecordProcessorCheckpointer checkpointer) {
        try {
            checkpointer.checkpoint();
        } catch (InvalidStateException | ShutdownException e) {
            throw new RuntimeException(e);
        }
    }

}
