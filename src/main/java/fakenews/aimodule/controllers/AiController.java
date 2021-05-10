package fakenews.aimodule.controllers;

import fakenews.aimodule.entities.AiEntity;
import fakenews.aimodule.servicies.AiService;
import fakenews.aimodule.utilities.ScoreResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("ai-module")
public class AiController {

    private final AiService aiService;

    @Autowired
    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/")
    public ScoreResult getResult(@RequestBody AiEntity aiEntity) throws IOException {
        return aiService.getResult(aiEntity);
    }
}
