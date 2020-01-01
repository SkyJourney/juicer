package com.braggart.juicer.util;

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
        String[] headers = getHeaders(CSVReader);
        return getRecords(CSVReader, headers);
    }

    public static String[] getHeaders(Reader CSVReader) throws Exception {
        BufferedReader bufferedReader = new BufferedReader(CSVReader);
        return bufferedReader.readLine().split(",");
    }

    private static List<Map<String,Object>> getRecords(Reader CSVReader, String[] headers) throws Exception {
        CSVFormat format = CSVFormat.DEFAULT.withHeader(headers);
        CSVParser parser = new CSVParser(CSVReader, format);
        List<CSVRecord> records = parser.getRecords();
        //noinspection rawtypes
        List list = records.parallelStream().map(CSVRecord::toMap).collect(Collectors.toList());
        //noinspection unchecked
        return (List<Map<String,Object>>)list;
    }
}
