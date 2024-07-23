package ar.edu.utn.frc.tup.lc.iii.services.impl;

import ar.edu.utn.frc.tup.lc.iii.entities.GameEntity;
import ar.edu.utn.frc.tup.lc.iii.entities.GamePredictionEntity;
import ar.edu.utn.frc.tup.lc.iii.entities.GameResultEntity;
import ar.edu.utn.frc.tup.lc.iii.entities.UserEntity;
import ar.edu.utn.frc.tup.lc.iii.models.Game;
import ar.edu.utn.frc.tup.lc.iii.models.GamePrediction;
import ar.edu.utn.frc.tup.lc.iii.models.GameResult;
import ar.edu.utn.frc.tup.lc.iii.models.User;
import ar.edu.utn.frc.tup.lc.iii.repositories.GamePredictionRepository;
import ar.edu.utn.frc.tup.lc.iii.repositories.GameRepository;
import ar.edu.utn.frc.tup.lc.iii.repositories.GameResultRepository;
import ar.edu.utn.frc.tup.lc.iii.repositories.UserRepository;
import ar.edu.utn.frc.tup.lc.iii.services.GameService;
import ar.edu.utn.frc.tup.lc.iii.services.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class GameServiceImplTest {
    @Autowired
    private GameServiceImpl gameService;

    @MockBean
    private GameRepository gameRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private UserService userService;

    @MockBean
    private GameResultRepository gameResultRepository;

    @MockBean
    private GamePredictionRepository gamePredictionRepository;
    @Qualifier("modelMapper")
    @Autowired
    private ModelMapper modelMapper;

    @Test
    void setGameResultByGameIdTest_GameNull() {
        // TODO Testing
        when(gameRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(HttpClientErrorException.class, () -> gameService.setGameResultByGameId(1L, null,null));
    }

    @Test
    void setGameResultByGameIdTest_GameResultNull() {
        GameEntity game = new GameEntity();
        game.setStadium("stadiumTest");
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        when(gameResultRepository.findByGameId(1L)).thenReturn(null);

        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        GameResult gameResult = gameService.setGameResultByGameId(1L, 2,3);
        assertNotNull(gameResult);
        assertEquals("stadiumTest",gameResult.getGame().getStadium());
        assertEquals(2,gameResult.getLocalGoals());
        assertEquals(3,gameResult.getVisitorGoals());
    }

    @Test
    void setGameResultByGameIdTest_gameResultNotNull() {
        GameEntity game = new GameEntity();
        game.setStadium("stadiumTest");
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        GameResultEntity gameResultEntity = new GameResultEntity();
        gameResultEntity.setGame(game);
        when(gameResultRepository.findByGameId(1L)).thenReturn(gameResultEntity);

        GameResult gameResult = gameService.setGameResultByGameId(1L, 4,9);
        assertNotNull(gameResult);
        assertEquals(4,gameResult.getLocalGoals());
        assertEquals(9,gameResult.getVisitorGoals());
    }

    @Test
    void predictTest_UserNull() {
//        when(userRepository.findById(1L)).thenReturn(Optional.empty());
//        assertThrows(HttpClientErrorException.class,() -> gameService.predict(1L,null,null,null));
          when(userService.getUserById(1L)).thenReturn(null);

          HttpClientErrorException exception = assertThrows(HttpClientErrorException.class,
                  () -> gameService.predict(1L,null,null,null));

          assertEquals("User does not exist", exception.getStatusText());
    }
    @Test
    void predictTest_GameNull() {
        when(userService.getUserById(1L)).thenReturn(new User());

        when(gameRepository.findById(1L)).thenReturn(Optional.empty());
        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () -> gameService.predict(1L, 1L, null, null));
        assertEquals("Game does not exist", exception.getStatusText());    }
    @Test
    void predictTest_GameDateBeforeNow() {
        when(userService.getUserById(1L)).thenReturn(new User());

        GameEntity game = new GameEntity();
        game.setGameDate(LocalDateTime.now().minusDays(1));
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () -> gameService.predict(1L, 1L, null, null));
        assertEquals("The game has already started", exception.getStatusText());
    }
    @Test
    void predictTest_GamePredictionNull() {
        User user = new User();
        user.setEmail("emailTest@test.com");
        user.setPassword("passwordTest");
        when(userService.getUserById(1L)).thenReturn(user);

        GameEntity game = new GameEntity();
        game.setStadium("stadium test");
        game.setGameDate(LocalDateTime.now().plusHours(1));
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

//        when(gameService.getPrediction(1L, 1L)).thenReturn(null);
        when(gamePredictionRepository.findByUserIdAndGameId(1L,1L)).thenReturn(Optional.empty());

        GamePrediction gamePrediction = gameService.predict(1L,1L,5,3);

        assertNotNull(gamePrediction);
        assertEquals("stadium test",gamePrediction.getGame().getStadium());
        assertEquals("emailTest@test.com",gamePrediction.getUser().getEmail());
        assertEquals("passwordTest",gamePrediction.getUser().getPassword());
        assertEquals(5,gamePrediction.getLocalGoals());
        assertEquals(3,gamePrediction.getVisitorGoals());

    }
    @Test
    void predictTest_GamePredictionNotNull() {
        UserEntity user = new UserEntity();
        user.setEmail("emailTest@test.com");
        user.setPassword("passwordTest");
        when(userService.getUserById(1L)).thenReturn(modelMapper.map(user,User.class));

        GameEntity game = new GameEntity();
        game.setStadium("stadium test");
        game.setGameDate(LocalDateTime.now().plusHours(1));
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        GamePredictionEntity gamePredictionEntity = new GamePredictionEntity();
        gamePredictionEntity.setGame(game);
        gamePredictionEntity.setUser(user);

        when(gamePredictionRepository.findByUserIdAndGameId(1L,1L)).thenReturn(Optional.of(gamePredictionEntity));

        GamePrediction gamePrediction = gameService.predict(1L,1L,5,3);
        when(gamePredictionRepository.save(Mockito.any())).thenReturn(gamePrediction);

        assertNotNull(gamePrediction);
        assertEquals("stadium test",gamePrediction.getGame().getStadium());
        assertEquals("emailTest@test.com",gamePrediction.getUser().getEmail());
        assertEquals("passwordTest",gamePrediction.getUser().getPassword());
        assertEquals(5,gamePrediction.getLocalGoals());
        assertEquals(3,gamePrediction.getVisitorGoals());

    }
}