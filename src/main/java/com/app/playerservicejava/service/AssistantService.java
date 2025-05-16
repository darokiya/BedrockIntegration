package com.app.playerservicejava.service;


import com.app.playerservicejava.model.Prompt;
import com.app.playerservicejava.model.QueryType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.bedrock.BedrockAsyncClient;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeAsyncClient;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelWithResponseStreamRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelWithResponseStreamResponseHandler;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class AssistantService {

    private static final String CLAUDE = "anthropic.claude-v2:1";
    //private static final String DEEPSEEK = "amazon.nova-lite-v1:0";
    private static final String LLAMA4 = "us.meta.llama4-maverick-17b-instruct-v1:0";
    private static final String DEEPSEEK = "us.deepseek.r1-v1:0";
    private static final String NOVA = "amazon.nova-lite-v1:0";//"us.amazon.nova-pro-v1:0";
    // Inference profile ID us.meta.llama4-maverick-17b-instruct-v1:0
    //Inference profile ARN arn:aws:bedrock:us-east-1:281846016812:inference-profile/us.meta.llama4-maverick-17b-instruct-v1:0

    @Autowired
    private BedrockRuntimeClient bedrockClient;

    @Autowired
    private BedrockRuntimeAsyncClient bedrockAsyncClient;

    public String askAssistant(Prompt prompt) throws JsonProcessingException {
        // Claude requires you to enclose the prompt as follows:
        String enclosedPrompt = "Human: " + prompt.getQuestion() + "\n\nAssistant:";
        String response = "";
        if (prompt.getQueryType() == QueryType.SYNC) {
            response = syncResponse(enclosedPrompt);
        } else if (prompt.getQueryType() == QueryType.ASYNC) {
            response = asyncResponse(enclosedPrompt);
        }
        return response;
    }

    public String askAssistantLlama(Prompt prompt) throws JsonProcessingException {
        return syncResponseIllama(prompt.getQuestion());
    }

    public String askAssistantDeepSeek(Prompt prompt) throws JsonProcessingException {
        return syncResponseDeepSeek(prompt.getQuestion());
    }

    public String askAssistantNova(Prompt prompt) throws JsonProcessingException {
        return syncResponseNova(prompt.getQuestion());
    }


    /*
     * * Synchronous call to AI for text response
     */
    private String syncResponse(String enclosedPrompt) throws JsonProcessingException {

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode rootNode = mapper.createObjectNode();
        rootNode.put("prompt", enclosedPrompt);
        rootNode.put("max_tokens_to_sample", 200);
        rootNode.put("temperature", 0.5);
        rootNode.putArray("stop_sequences").add("\n\nHuman:");
        String payload = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);

        InvokeModelRequest request = InvokeModelRequest.builder().body(SdkBytes.fromUtf8String(payload))
                .modelId(CLAUDE)
                .contentType("application/json")
                .accept("application/json").build();

        InvokeModelResponse response = bedrockClient.invokeModel(request);

        mapper = new ObjectMapper();

        JsonNode jsonNode = mapper.readTree(response.body().asUtf8String());
        String generatedText = jsonNode.get("completion").asText();

        System.out.println("Generated text: " + generatedText);

        return generatedText;
    }

    private String syncResponseIllama(String enclosedPrompt) throws JsonProcessingException {

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode rootNode = mapper.createObjectNode();
        rootNode.put("prompt", enclosedPrompt);
        rootNode.put("max_gen_len", 512);
        rootNode.put("temperature", 0.5);
        rootNode.put("top_p", 0.9);
        String payload = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);

        // Prepare the JSON payload as a String
        String jsonBody = "{\"max_tokens\":256, \"messages\":[{\"role\":\"user\", \"content\":\"Hello, world\"}]}";

        InvokeModelRequest request = InvokeModelRequest.builder().body(SdkBytes.fromUtf8String(payload))
                .modelId(LLAMA4)
                .contentType("application/json")
                .accept("application/json")
                //.body(SdkBytes.fromUtf8String(jsonBody))
                .build();

        InvokeModelResponse response = bedrockClient.invokeModel(request);
        String generatedText = response.body().asUtf8String();

        System.out.println("Generated text: " + generatedText);

        return generatedText;
    }


    private String syncResponseDeepSeek(String enclosedPrompt) throws JsonProcessingException {

        // Create the root ObjectNode
        // Create an ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode rootNode = objectMapper.createObjectNode();

        // Create the "inferenceConfig" ObjectNode
        ObjectNode inferenceConfigNode = objectMapper.createObjectNode();
        inferenceConfigNode.put("max_tokens", 512);

        // Create the "messages" ArrayNode
        ArrayNode messagesNode = objectMapper.createArrayNode();
        ObjectNode messageNode = objectMapper.createObjectNode();
        messageNode.put("role", "user");
        messageNode.put("content", enclosedPrompt);
        messagesNode.add(messageNode);

        // Add the "inferenceConfig" and "messages" nodes to the root node
        rootNode.set("inferenceConfig", inferenceConfigNode);
        rootNode.set("messages", messagesNode);
        String payload = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);
        System.out.println("Payload>>>>>>: " + payload);

        InvokeModelRequest request = InvokeModelRequest.builder().body(SdkBytes.fromUtf8String(payload))
                .modelId(DEEPSEEK)
                .contentType("application/json")
                .accept("application/json").build();

        InvokeModelResponse response = bedrockClient.invokeModel(request);

        objectMapper = new ObjectMapper();

        JsonNode jsonNode = objectMapper.readTree(response.body().asUtf8String());
        String generatedText = jsonNode.get("completion").asText();

        System.out.println("Generated text: " + generatedText);

        return generatedText;
    }

    private String syncResponseNova(String enclosedPrompt) throws JsonProcessingException {

        // Create the root ObjectNode
        // Create an ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode rootNode = objectMapper.createObjectNode();

        // Create the "inferenceConfig" ObjectNode
        ObjectNode inferenceConfigNode = objectMapper.createObjectNode();
        inferenceConfigNode.put("max_new_tokens", 1000);

        // Create the "messages" ArrayNode
        ArrayNode messagesNode = objectMapper.createArrayNode();
        ObjectNode messageNode = objectMapper.createObjectNode();

        ArrayNode contentNode = objectMapper.createArrayNode();
        ObjectNode textNode = objectMapper.createObjectNode();
        textNode.put("text", enclosedPrompt);
        contentNode.add(textNode);

        messageNode.put("role", "user");
        messageNode.put("content", contentNode);
        messagesNode.add(messageNode);

        // Add the "inferenceConfig" and "messages" nodes to the root node
        rootNode.set("inferenceConfig", inferenceConfigNode);
        rootNode.set("messages", messagesNode);
        String payload = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);
        System.out.println("Payload>>>>>>: " + payload);

        InvokeModelRequest request = InvokeModelRequest.builder().body(SdkBytes.fromUtf8String(payload))
                .modelId(NOVA)
                .contentType("application/json")
                .accept("application/json").build();

        InvokeModelResponse response = bedrockClient.invokeModel(request);

        objectMapper = new ObjectMapper();


        JsonNode jsonNode = objectMapper.readTree(response.body().asUtf8String());
        // Print the entire response for debugging
        System.out.println("Response JSON: " + jsonNode.toPrettyString());

        JsonNode textNode1 = jsonNode.path("output").path("message").path("content").get(0).path("text");

        // Extract the text
        String generatedText = textNode1.asText();

        System.out.println("Generated text: " + generatedText);

        return generatedText;
    }


    /*
     * * Streaming call to AI for text response
     */
    private String asyncResponse(String enclosedPrompt) {
        var finalCompletion = new AtomicReference<>("");
        var silent = false;

        var payload = new JSONObject().put("prompt", enclosedPrompt).put("temperature", 0.8)
                .put("max_tokens_to_sample", 300).toString();

        var request = InvokeModelWithResponseStreamRequest.builder().body(SdkBytes.fromUtf8String(payload))
                .modelId(CLAUDE).contentType("application/json").accept("application/json").build();

        var visitor = InvokeModelWithResponseStreamResponseHandler.Visitor.builder().onChunk(chunk -> {
            var json = new JSONObject(chunk.bytes().asUtf8String());
            var completion = json.getString("completion");
            finalCompletion.set(finalCompletion.get() + completion);
            if (!silent) {
                System.out.print(completion);
            }
        }).build();

        var handler = InvokeModelWithResponseStreamResponseHandler.builder()
                .onEventStream(stream -> stream.subscribe(event -> event.accept(visitor))).onComplete(() -> {
                }).onError(e -> System.out.println("\n\nError: " + e.getMessage())).build();

        bedrockAsyncClient.invokeModelWithResponseStream(request, handler).join();

        return finalCompletion.get();
    }
}
