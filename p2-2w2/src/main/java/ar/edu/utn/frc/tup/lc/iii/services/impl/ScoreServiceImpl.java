package ar.edu.utn.frc.tup.lc.iii.services.impl;

import ar.edu.utn.frc.tup.lc.iii.entities.*;
import ar.edu.utn.frc.tup.lc.iii.models.GamePrediction;
import ar.edu.utn.frc.tup.lc.iii.models.GameResult;
import ar.edu.utn.frc.tup.lc.iii.models.Score;
import ar.edu.utn.frc.tup.lc.iii.repositories.ScoreDetailRepository;
import ar.edu.utn.frc.tup.lc.iii.repositories.ScoreRepository;
import ar.edu.utn.frc.tup.lc.iii.services.ScoreService;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class ScoreServiceImpl implements ScoreService {

    @Autowired
    private ScoreRepository scoreRepository;

    @Autowired
    private ScoreDetailRepository scoreDetailRepository;

    @Autowired
    private ModelMapper modelMapper;
    @Qualifier("scoreService")

    /**
     * Calculate the score of a user based on the prediction and the result of a game.
     * If the user guessed the result of the game, he gets 1 point.
     * If the user guessed the result and the goals of the local and visitor teams, he gets 3 points.
     * If the user didn't guess the result of the game, he gets 0 points.
     *
     * @param gamePrediction the prediction of the user.
     * @param gameResult the result of the game.
     */
    @Transactional
    @Override
    public void calculateScore(GamePrediction gamePrediction, GameResult gameResult) {
        // TODO 2 - Cálculo de puntajes: Esta parte del flujo calcula para cada predicción su puntaje.
        //  Además deja un registro del detalle del puntaje en la tabla score_detail.

        // 1. Determine the points of the user based on the prediction and the result of the game.
        // 1.a. Initialize a variable points with 0.
        // 1.b. If the result of the prediction is equal to the result of the game, the user gets 1 point.
        // 1.c. If the user guessed the result and the goals of the local and visitor teams, he gets 3 extra points.
        // 2. Get the score of the user by user id.
        // 3. If the user has a score, get the score detail of the user by score id and prediction game id.
        // 3.a. If the user has a score detail, reduce the points of the user score by the points of the score detail.
        //      Set the new points of the score detail with the points calculated in step 1 and save the score detail.
        // 3.b. If the user has not a score detail, create a new score detail with the points calculated in step 1 and save it.
        // 4. If the user has not a score, create a new score with the user, the points calculated in step 1 and save it.
        // 4.a. Create the score detail with the score, the prediction, the result and the points calculated in step 1 and save it.
        int points = 0;
        if(gamePrediction.getResult().equals(gameResult.getResult())){
            points ++ ;
            if(gamePrediction.getLocalGoals().equals(gameResult.getLocalGoals()) && gamePrediction.getVisitorGoals().equals(gameResult.getVisitorGoals())){
                points += 3;
            }
        }
        ScoreEntity scoreEntity = scoreRepository.findByUserId(gamePrediction.getUser().getId()).orElse(null);

        if(scoreEntity != null) {

            ScoreDetailEntity scoreDetailEntity  = scoreDetailRepository.findByScoreIdAndPredictionGameId(scoreEntity.getId(), gamePrediction.getId()).orElse(null);
            if(scoreDetailEntity != null) {
                scoreEntity.setPoints(scoreEntity.getPoints() - scoreDetailEntity.getPoints());
                scoreDetailEntity.setPoints(scoreDetailEntity.getPoints() + points);
                scoreDetailRepository.save(scoreDetailEntity);
            } else {
                scoreDetailEntity = new ScoreDetailEntity();
                scoreDetailEntity.setScore(scoreEntity);
                scoreDetailEntity.setPrediction(modelMapper.map(gamePrediction, GamePredictionEntity.class));
                scoreDetailEntity.setPoints(points);
                scoreDetailRepository.save(scoreDetailEntity);
            }

            scoreEntity.setPoints(scoreEntity.getPoints() + points);
            scoreRepository.save(scoreEntity);
        } else {
            scoreEntity = new ScoreEntity();
            scoreEntity.setUser(modelMapper.map(gamePrediction.getUser(), UserEntity.class));
            scoreEntity.setPoints(points);
            scoreRepository.save(scoreEntity);

            ScoreDetailEntity scoreDetailEntity = new ScoreDetailEntity();
            scoreDetailEntity.setScore(scoreEntity);
            scoreDetailEntity.setPrediction(modelMapper.map(gamePrediction, GamePredictionEntity.class));
            scoreDetailEntity.setResult(modelMapper.map(gameResult,GameResultEntity.class));
            scoreDetailEntity.setPoints(points);
            scoreDetailRepository.save(scoreDetailEntity);
        }

    }

    /**
     * Get the scores of all users ordered by points, user last name and name.
     *
     * @return the scores of all users.
     */
    @Override
    public List<Score> getScores() {
        List<ScoreEntity> scoreEntities = scoreRepository.findAllOrdered();
        return modelMapper.map(scoreEntities, new TypeToken<List<Score>>() {}.getType());
    }
}