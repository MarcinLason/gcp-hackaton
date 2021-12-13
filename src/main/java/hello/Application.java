package hello;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

@SpringBootApplication
@RestController
public class Application {

  static class Self {
    public String href;
  }

  static class Links {
    public Self self;
  }

  static class PlayerState {
    public Integer x;
    public Integer y;
    public String direction;
    public Boolean wasHit;
    public Integer score;
  }

  static class Arena {
    public List<Integer> dims;
    public Map<String, PlayerState> state;
  }

  static class ArenaUpdate {
    public Links _links;
    public Arena arena;
  }

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  @InitBinder
  public void initBinder(WebDataBinder binder) {
    binder.initDirectFieldAccess();
  }

  @GetMapping("/")
  public String index() {
    return "Let the battle begin!";
  }

  @PostMapping("/**")
  public String index(@RequestBody ArenaUpdate arenaUpdate) {
//    PlayerState state = arenaUpdate.arena.state.get("https://java-springboot-amhe6rxwpa-uc.a.run.app/");
//    int x = state.x;
//    int y = state.y;
//    String direction = state.direction;
//    boolean wasHit = state.wasHit;
//    if (wasHit) {
//        return "F";
//    }
//    if (x == 0) {
//        return "R";
//    }
//    if (y == 0) {
//        return "R";
//    }
//    if (x > 39) {
//        return "R";
//    }
//    if (y > 39) {
//        return "R";
//    }
    return "T";
  }
}

