package com.logstream.backend.search;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;

import java.util.Set;

/**
 * Extends the classic QueryParser so numeric fields (timestamp, response_time_ms)
 * use proper Point-based range queries instead of being treated as text.
 */
public class LogQueryParser extends QueryParser {

    private static final Set<String> INT_FIELDS = Set.of("response_time_ms");
    private static final Set<String> LONG_FIELDS = Set.of("timestamp");

    public LogQueryParser(String defaultField, Analyzer analyzer) {
        super(defaultField, analyzer);
    }

    @Override
    protected Query getRangeQuery(String field, String part1, String part2,
                                  boolean startInclusive, boolean endInclusive) throws ParseException {
        if (INT_FIELDS.contains(field)) {
            int min = (part1 == null || part1.equals("*")) ? Integer.MIN_VALUE : Integer.parseInt(part1);
            int max = (part2 == null || part2.equals("*")) ? Integer.MAX_VALUE : Integer.parseInt(part2);
            if (!startInclusive && min != Integer.MIN_VALUE) min++;
            if (!endInclusive && max != Integer.MAX_VALUE) max--;
            return IntPoint.newRangeQuery(field, min, max);
        }
        if (LONG_FIELDS.contains(field)) {
            long min = (part1 == null || part1.equals("*")) ? Long.MIN_VALUE : Long.parseLong(part1);
            long max = (part2 == null || part2.equals("*")) ? Long.MAX_VALUE : Long.parseLong(part2);
            if (!startInclusive && min != Long.MIN_VALUE) min++;
            if (!endInclusive && max != Long.MAX_VALUE) max--;
            return LongPoint.newRangeQuery(field, min, max);
        }
        return super.getRangeQuery(field, part1, part2, startInclusive, endInclusive);
    }
}