package com.example.SpringAIStarter.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/rag")
public class RagController {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    public RagController(ChatClient.Builder builder, VectorStore store) {
        this.vectorStore = store;
        this.chatClient = builder
        .defaultSystem("""
            You are a financial analyst assistant. You answer questions about company
            quarterly earnings reports using only the provided context. Provide
            comprehensive, detailed answers covering all relevant information from the
            context — including revenue, profit, margins, growth, guidance, key highlights,
            risks, and any other details present. If the context does not contain enough
            information, say so clearly. Always cite which company and quarter the data
            comes from.
        """)
        .build();
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> uploadDocuments(@RequestParam("files") List<MultipartFile> files) throws IOException {
        int totalChunks = 0;

        List<String> processed = new ArrayList<>();

        TokenTextSplitter splitter = TokenTextSplitter.builder()
        .withChunkSize(512)
        .withMinChunkSizeChars(100)
        .withMinChunkLengthToEmbed(50)
        .withMaxNumChunks(10000)
        .build();

        for(MultipartFile file : files) {
            String fileName = file.getOriginalFilename();
            TikaDocumentReader reader = new TikaDocumentReader(new InputStreamResource(file.getInputStream()));

            List<Document> rawDocs = reader.read();

            rawDocs.forEach(doc -> doc.getMetadata().put("source", fileName));

            List<Document> chunks = splitter.split(rawDocs);
            vectorStore.add(chunks);

            totalChunks += chunks.size();
            processed.add(fileName);

        }

        return Map.of("totalChunks", totalChunks, "processed", processed);
    }

    @GetMapping("/ask")
    public String ask(@RequestParam String question) {
        QuestionAnswerAdvisor advisor = QuestionAnswerAdvisor.builder(vectorStore)
        .searchRequest(SearchRequest.builder().similarityThreshold(0.4).topK(15).build())
        .build();
        
        String response = chatClient.prompt()
        .advisors(advisor)
        .user(question)
        .call()
        .content();

        return response;
    }
}
