package com.logstream.backend.search;

import org.apache.lucene.analysis.Analyzer;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.stereotype.Service;
import java.util.TreeMap;
import org.apache.lucene.search.BooleanQuery; 
import org.apache.lucene.search.BooleanClause;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

@Slf4j
@Service
public class LogSearchService {

    private static final String INDEX_DIR = "lucene-index";

    // Fields that must match EXACTLY (case-sensitive, no tokenizing) — level, service, log_id
    // Everything else (like "message") falls back to StandardAnalyzer for full-text search
    private Analyzer buildQueryAnalyzer() {
        Map<String, Analyzer> perFieldAnalyzers = new HashMap<>();
        perFieldAnalyzers.put("level", new KeywordAnalyzer());
        perFieldAnalyzers.put("service", new KeywordAnalyzer());
        perFieldAnalyzers.put("log_id", new KeywordAnalyzer());
        return new PerFieldAnalyzerWrapper(new StandardAnalyzer(), perFieldAnalyzers);
    }

    public List<Map<String, String>> search(String queryString, int maxResults) throws Exception {
        List<Map<String, String>> results = new ArrayList<>();

        try (Directory directory = FSDirectory.open(Path.of(INDEX_DIR));
             DirectoryReader reader = DirectoryReader.open(directory)) {

            IndexSearcher searcher = new IndexSearcher(reader);

            LogQueryParser parser = new LogQueryParser("message", buildQueryAnalyzer());
            Query query = parser.parse(queryString);

            TopDocs topDocs = searcher.search(query, maxResults);
            log.info("Query '{}' matched {} documents", queryString, topDocs.totalHits.value);

            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                Document doc = searcher.storedFields().document(scoreDoc.doc);
                Map<String, String> result = new HashMap<>();
                result.put("log_id", doc.get("log_id"));
                result.put("level", doc.get("level"));
                result.put("service", doc.get("service"));
                result.put("message", doc.get("message"));
                result.put("timestamp", doc.get("timestamp"));
                result.put("response_time_ms", doc.get("response_time_ms"));
                results.add(result);
            }
        }

        return results;
    }

    public Map<Long, Long> getLogCountsByMinute(long fromEpochMillis, long toEpochMillis) throws IOException {
        Map<Long, Long> buckets = new TreeMap<>(); // TreeMap keeps buckets sorted by time

        try (Directory directory = FSDirectory.open(Path.of(INDEX_DIR));
             DirectoryReader reader = DirectoryReader.open(directory)) {

            IndexSearcher searcher = new IndexSearcher(reader);
            Query rangeQuery = LongPoint.newRangeQuery("timestamp", fromEpochMillis, toEpochMillis);

            TopDocs topDocs = searcher.search(rangeQuery, Integer.MAX_VALUE);
            log.info("Aggregation query matched {} documents", topDocs.totalHits.value);

            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                Document doc = searcher.storedFields().document(scoreDoc.doc);
                long timestamp = Long.parseLong(doc.get("timestamp"));

                // Round down to the nearest minute (60,000 ms) to create the bucket key
                long minuteBucket = (timestamp / 60000) * 60000;

                buckets.merge(minuteBucket, 1L, Long::sum);
            }
        }

        return buckets;
    }
    public long countMatchingInWindow(String queryString, long windowMillis) throws Exception {
    try (Directory directory = FSDirectory.open(Path.of(INDEX_DIR));
         DirectoryReader reader = DirectoryReader.open(directory)) {

        IndexSearcher searcher = new IndexSearcher(reader);
        LogQueryParser parser = new LogQueryParser("message", buildQueryAnalyzer());
        Query userQuery = parser.parse(queryString);

        long now = System.currentTimeMillis();
        Query timeFilter = LongPoint.newRangeQuery("timestamp", now - windowMillis, now);

        BooleanQuery combined = new BooleanQuery.Builder()
                .add(userQuery, BooleanClause.Occur.MUST)
                .add(timeFilter, BooleanClause.Occur.MUST)
                .build();

        TopDocs topDocs = searcher.search(combined, Integer.MAX_VALUE);
        return topDocs.totalHits.value;
    }
}
}