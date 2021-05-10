package fakenews.aimodule.servicies;

import fakenews.aimodule.entities.AiEntity;
import fakenews.aimodule.entities.AiResultEntity;
import fakenews.aimodule.repositories.AiRepository;
import fakenews.aimodule.utilities.ScoreResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.Scanner;

@Service
public class AiService {

    private final AiRepository aiRepository;

    @Autowired
    public AiService(AiRepository aiRepository) {
        this.aiRepository = aiRepository;
    }

    /**
     * private method that returns the score of the aiEntity given
     * @param aiEntity the given entity for which the score needs to be calculated
     * @return the score of the parameter aiEntity
     * if the aiEntity given has already been checked, then we just return its score (which was calculated at some point before)
     * if it has not been checked before, then:
     * we execute calculate its score by executing the 2 python scripts with it as a parameter
     * we merge the 2 scores that result from the scripts
     * we insert the aiEntity into our detabase (id, title, content and score)
     * we return the resulted score (a ScoreResult object)
     */
    public ScoreResult getResult(AiEntity aiEntity) throws IOException {
        Optional<AiResultEntity> exists = aiRepository.findById(aiEntity.getId());

        if (exists.isEmpty()) {
            String result = calculateScore(aiEntity);

            AiResultEntity aiResult = new AiResultEntity();
            aiResult.setId(aiEntity.getId());
            aiResult.setTitle(aiEntity.getTitle());
            aiResult.setContent(aiEntity.getContent());
            aiResult.setResult(result);
            aiRepository.save(aiResult);

            return new ScoreResult(result);
        }
        else {
            //return its score
            return new ScoreResult(aiRepository.getOne(aiEntity.getId()).getResult());
        }
    }

    /**
     * Private method that executes the first python script using the given entity.
     * @param aiEntity the given entity for which the score needs to be calculated
     * @return the score
     * we execute the python file (with the text of the post as a parameter) => it will write the scor to a .txt file
     * then we read from that file (scor1.txt)
     * we keep the scor in the according variabiles
     */
    private String executePython1(AiEntity aiEntity) throws IOException {
        Process p1 = Runtime.getRuntime().exec("python3 py1.py " + aiEntity.getContent());
        Scanner in = new Scanner(new File("scor1.txt"));
        String score1 = in.nextLine();
        return score1;
    }

    /**
     * Private method that executes the second python script using the given entity.
     * @param aiEntity the given entity for which the score needs to be calculated
     * @return the score
     * we execute the python file (with the text of the post as a parameter) => it will write the scor to a .txt file
     * then we read from that file (scor2.txt)
     * we keep the scor in the according variabiles
     */
    private String executePython2(AiEntity aiEntity) throws IOException {
        Process p2 = Runtime.getRuntime().exec("python3 py2.py " + aiEntity.getContent());
        Scanner in = new Scanner(new File("scor2.txt"));
        String score2 = in.nextLine();
        return score2;
    }

    /**
     * private method that merges the 2 scores resulted from the 2 python scripts
     * @param aiEntity the given entity for which the score needs to be calculated
     * @return the merged score
     * the method first calculates the 2 individual scores (result1 and result2) by executing the 2 python scripts with aiEntity as a parameter
     * then it merges the score:
     * true, true => true
     * true, false or false, true => partially false
     * false, false => false
     * partially false, true or true, partially false => true
     * partially false, false or false, partially false => false
     * partially false, partially false => partially false
     */
    private String calculateScore(AiEntity aiEntity) throws IOException {
        String result1 = executePython1(aiEntity);
        String result2 = executePython2(aiEntity);

        if (result1.equals("true") && result2.equals("true")) {
            return "true";
        }

        if (result1.equals("true") && result2.equals("false") || result1.equals("fals" +
                "e") && result2.equals("true")) {
            return "partially false";
        }

        if (result1.equals("false") && result2.equals("false")) {
            return "false";
        }

        if (result1.equals("partially false") && result2.equals("true") || result1.equals("true") && result2.equals("partially false")) {
            return "true";
        }

        if (result1.equals("partially false") && result2.equals("false") || result1.equals("false") && result2.equals("partially false")) {
            return "false";
        }

        return "partially false";
    }
}
