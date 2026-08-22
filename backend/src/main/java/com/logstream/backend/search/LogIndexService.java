package com.logstream.backend.search;

import com.logstream.backend.grpc.LogMessage;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;

@Slf4j
@Service
public class LogIndexService {

    private static final String INDEX_DIR = "lucene-index";

    private Directory directory;
    private IndexWriter indexWriter;

    @PostConstruct
    public void init() throws IOException {
        directory = FSDirectory.open(Path.of(INDEX_DIR));
        StandardAnalyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        indexWriter = new IndexWriter(directory, config);
        log.info("Lucene index initialized at ./{}", INDEX_DIR);
    }

    public void indexLog(LogMessage logMessage) throws IOException {
        Document doc = new Document();

        doc.add(new StringField("log_id", logMessage.getLogId(), Field.Store.YES));
        doc.add(new StringField("level", logMessage.getLevel(), Field.Store.YES));
        doc.add(new StringField("service", logMessage.getService(), Field.Store.YES));
        doc.add(new TextField("message", logMessage.getMessage(), Field.Store.YES));

        // Numeric fields: Point type for range-query searching, StoredField for retrieval
        doc.add(new LongPoint("timestamp", logMessage.getTimestamp()));
        doc.add(new StoredField("timestamp", logMessage.getTimestamp()));

        doc.add(new IntPoint("response_time_ms", logMessage.getResponseTimeMs()));
        doc.add(new StoredField("response_time_ms", logMessage.getResponseTimeMs()));

        indexWriter.addDocument(doc);
        indexWriter.commit();

        log.info("Indexed log_id={} into Lucene", logMessage.getLogId());
    }

    @PreDestroy
    public void close() throws IOException {
        if (indexWriter != null) indexWriter.close();
        if (directory != null) directory.close();
    }
}