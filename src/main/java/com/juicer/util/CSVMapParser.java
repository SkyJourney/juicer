package com.juicer.util;

import com.juicer.core.JuicerData;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.Reader;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author SkyJourney
 */
public class CSVMapParser {

    public static List<Map<String,Object>> getData(Reader CSVReader) throws Exception {
        BufferedReader bufferedReader = new BufferedReader(CSVReader);
        String[] headers = getHeaders(bufferedReader);
        return getRecords(bufferedReader, headers);
    }

    public static String[] getHeaders(BufferedReader CSVReader) throws Exception {
        return CSVReader.readLine().split(",");
    }

    private static List<Map<String,Object>> getRecords(BufferedReader CSVReader, String[] headers) throws Exception {
        CSVFormat format = CSVFormat.DEFAULT.withHeader(headers);
        CSVParser parser = new CSVParser(CSVReader, format);
        List<CSVRecord> records = parser.getRecords();
        return records.parallelStream()
                .map(record -> {
                    JuicerData juicerData = JuicerData.getInstance();
                    record.toMap().forEach(juicerData::addData);
                    return juicerData;
                })
                .collect(Collectors.toList());
    }
}
