package com.example.SpringAIStarter.controllers;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/prompt")
public class PromptController {

    private final ChatClient chatClient;

    public PromptController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }
    

    @GetMapping("/zero-shot")
    public String zeroShot(@RequestParam String message) {
        String result = chatClient.prompt()
        .user(u -> u.text("""
                Classify the sentiment of the following text as exactly one of: positive, negative, or neutral.
                Only responsd with the sentiment label, nothing else. 

                Text: {message}
                """).param("message", message))
                .call()
                .content();
        return result;

        
    }

    @GetMapping("/few-shot")
    public String fewShot(@RequestParam String message) {
        String result = chatClient.prompt()
        .user(u -> u.text("""
                Tell me the time complexity of the algorithm based on the algorithm name given. 
                Only respond with the worst case time complexity of the algorithm, nothing else. 

                Examples:
                Text: Merge Sort
                Time Complexity of the algorithm is: O(n log n)
                Text: Quick Sort
                Time Complexity of the algorithm is: O(n log n)
                Text: Bubble Sort
                Time Complexity of the algorithm is: O(n^2)
                Text: Selection Sort
                Time Complexity of the algorithm is: O(n^2)
                Text: Insertion Sort
                Time Complexity of the algorithm is: O(n^2)

                Text: {algorithmName}

                
                """).param("algorithmName", message))
                .call()
                .content();
        return result;

        
    }


    @GetMapping("/one-shot")
    public String oneShot(@RequestParam String message) {
        String result = chatClient.prompt()
        .user(u -> u.text("""
                Tell me a single add on product that can be recommended to a user based on the user's current cart items. 
                Only respond with the product name, nothing else. Assume we are working on a food commerce website. 

                Example:
                Cart Items: "pizza", "burger", "coke"
                Recommended Add On Product: "chips"

                Cart Items: {cartItems}
                
                """).param("cartItems", message))
                .call()
                .content();
        return result;

        
    }
}
