package com.yjh.rememberme.voice.controller;

import com.yjh.rememberme.common.dto.ResponseMessage;
import com.yjh.rememberme.database.entity.Chat;
import com.yjh.rememberme.database.entity.Voice;
import com.yjh.rememberme.voice.dto.VoiceDTO;
import com.yjh.rememberme.voice.service.VoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/voice")
public class VoiceController {
    private final VoiceService voiceService;

    @Autowired
    public VoiceController(VoiceService voiceService) {
        this.voiceService = voiceService;
    }
    //
    @PostMapping("/{username}")
    public ResponseEntity<?> postVoiceChatBot(@PathVariable String username , @RequestBody VoiceDTO voiceDTO) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", Charset.forName("UTF-8")));
        Map<String, Object> responseMap = new HashMap<>();

        voiceService.postVoiceLog(username, voiceDTO.getWeId());

        RestTemplate restTemplate = new RestTemplate();

        HttpEntity<?> entity = new HttpEntity<>(voiceDTO, headers);
        String url = "https://ae78-119-194-163-123.jp.ngrok.io/voice_chat_bot_inference";

        UriComponents uri = UriComponentsBuilder.fromHttpUrl(url).build();
        ResponseEntity<?> resultMap = restTemplate.exchange(uri.toString(), HttpMethod.POST, entity, Map.class);
        System.out.println("resultMap = " + resultMap);

        if(resultMap.getBody() == null){
            return ResponseEntity
                    .badRequest()
                    .headers(headers)
                    .body(new ResponseMessage(400, "postVoiceChatBot failed", responseMap));
        }

        responseMap.put("voice",resultMap);


        return ResponseEntity
                .created(URI.create("/"+username))
                .headers(headers)
                .body(new ResponseMessage(201,"postVoiceChatBot succeed",responseMap));
    }
    //음성채팅 업로드
    @PostMapping("/postvoice/{username}")
    public ResponseEntity<?> postVoice(@PathVariable String username , @RequestBody VoiceDTO voiceDTO) throws UnsupportedAudioFileException, IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", Charset.forName("UTF-8")));
        Map<String, Object> responseMap = new HashMap<>();
        Voice voice = null;
        voice = voiceService.postVoice(username, voiceDTO);

        responseMap.put("voiceName",voice.getVoiceName());

        return ResponseEntity
                .created(URI.create("/" + username))
                .headers(headers)
                .body(new ResponseMessage(201, "postVoice succeed", responseMap));
    }
}
