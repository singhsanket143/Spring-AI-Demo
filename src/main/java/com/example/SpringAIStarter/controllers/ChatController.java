package com.example.SpringAIStarter.controllers;

import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.dtos.MovieRecommendation;


@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatClient chatClient;

    public ChatController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    } 


    @GetMapping("/simple")
    public String simpleChat(@RequestParam String message) {
        String response = chatClient.prompt()
            .user(message)
            .call()
            .content();
        return response;
    }

    @GetMapping("/movies")
    public List<MovieRecommendation> getMovieRecommendations(@RequestParam String genre, @RequestParam int count) {
        return chatClient.prompt()
        .user(u -> u.text("""
                Recommend exactly {count} movies in the {genre} genre. For each movie, provide the accurate title, description, genre, rating, year, and actors.
                The rating should be a number between 0 and 10, and the year should be the year of the movie released. The data should be absolutely accurate.
                Everytime even the genre is same, the movies should be different.
                """).param("count", String.valueOf(count)).param("genre", genre))
        .call()
        .entity(new ParameterizedTypeReference<List<MovieRecommendation>>() {});
    }
    
}
