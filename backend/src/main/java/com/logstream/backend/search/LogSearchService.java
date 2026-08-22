package com.logstream.backend.search;

import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class LogSearchService {

    private static final String INDEX_DIR = "lucene-index";

    public List<Map<String, String>> search(String queryString, int maxResults) throws Exception {
        List<Map<String, String>> results = new ArrayList<>();

        try (Directory directory = FSDirectory.open(Path.of(INDEX_DIR));
             DirectoryReader reader = DirectoryReader.open(directory)) {

            IndexSearcher searcher = new IndexSearcher(reader);

            // "message" is the default field searched if the user doesn't specify one
            QueryParser parser = new QueryParser("message", new StandardAnalyzer());
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
}