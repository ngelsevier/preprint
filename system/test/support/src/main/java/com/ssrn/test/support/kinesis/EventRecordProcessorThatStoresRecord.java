package com.ssrn.test.support.kinesis;

import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessor;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessorCheckpointer;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.ShutdownReason;
import com.amazonaws.services.kinesis.model.Record;
import com.jayway.jsonpath.JsonPath;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class EventRecordProcessorThatStoresRecord implements IRecordProcessor {
    private final List<Record> kinesisRecords;

    public EventRecordProcessorThatStoresRecord() {
        this.kinesisRecords = new CopyOnWriteArrayList<>();
    }

    public List<Record> receivedRecords() {
        return kinesisRecords;
    }

    @Override
    public void initialize(String shardId) {

    }

    @Override
    public void processRecords(List<Record> records, IRecordProcessorCheckpointer checkpointer) {
        kinesisRecords.addAll(records);
    }

    @Override
    public void shutdown(IRecordProcessorCheckpointer checkpointer, ShutdownReason reason) {

    }

    public void forgetRecords() {
        kinesisRecords.clear();
    }

}
