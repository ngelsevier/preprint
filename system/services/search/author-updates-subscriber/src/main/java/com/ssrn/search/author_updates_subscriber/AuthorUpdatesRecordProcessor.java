package com.ssrn.search.author_updates_subscriber;

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
import com.ssrn.search.domain.AuthorUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class AuthorUpdatesRecordProcessor implements IRecordProcessor, IShutdownNotificationAware {
    private final static Logger LOGGER = LoggerFactory.getLogger(AuthorUpdatesRecordProcessor.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    private Consumer<List<AuthorUpdate>> authorUpdatesConsumer;
    private boolean logIndividualAuthorUpdates;

    AuthorUpdatesRecordProcessor(Consumer<List<AuthorUpdate>> authorUpdatesConsumer, boolean logIndividualAuthorUpdates) {
        this.authorUpdatesConsumer = authorUpdatesConsumer;
        this.logIndividualAuthorUpdates = logIndividualAuthorUpdates;
    }

    @Override
    public void initialize(InitializationInput initializationInput) {
    }

    @Override
    public void processRecords(ProcessRecordsInput processRecordsInput) {
        try {
            List<AuthorUpdate> authorUpdates = processRecordsInput.getRecords().stream()
                    .map(this::extractData)
                    .map(this::deserializeToAuthorUpdate)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            authorUpdatesConsumer.accept(authorUpdates);

            checkpoint(processRecordsInput.getCheckpointer());

        } catch (Throwable e) {
            LOGGER.error("Exception thrown whilst processing Kinesis records", e);
            throw e;
        }
    }

    @Override
    public void shutdown(ShutdownInput shutdownInput) {
        if (shutdownInput.getShutdownReason().equals(ShutdownReason.TERMINATE)) {
            checkpoint(shutdownInput.getCheckpointer());
        }
        LOGGER.info(String.format("Record processor shutting down with shutdown reason: %s", shutdownInput.getShutdownReason()));
    }

    @Override
    public void shutdownRequested(IRecordProcessorCheckpointer checkpointer) {
        LOGGER.info("Record processor requested to shut down");
    }

    private byte[] extractData(Record record) {
        byte[] data = record.getData().array();

        if (this.logIndividualAuthorUpdates) {
            LOGGER.info(String.format("Extracted Kinesis record data - %s", new String(data).replace(":", "-")));
        }
        return data;
    }

    private AuthorUpdate deserializeToAuthorUpdate(byte[] recordContent) {
        try {
            return objectMapper.readValue(recordContent, AuthorUpdate.class);
        } catch (Throwable suppressedException) {
            LOGGER.error(String.format("Exception thrown whilst deserializing Kinesis record data to %s. Record data was %s", AuthorUpdate.class, new String(recordContent).replace(":", "-")), suppressedException);
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
