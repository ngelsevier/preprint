package com.ssrn.test.support.kinesis;

import com.amazonaws.services.kinesis.model.Record;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

public class KinesisUtils {

    public static String getKinesisRecordContent(Record record) {
        return new String(record.getData().array());
    }
}
