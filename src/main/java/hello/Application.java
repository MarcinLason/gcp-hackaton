package hello;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import com.google.api.core.ApiFuture;
import com.google.cloud.ServiceOptions;
import com.google.cloud.bigquery.storage.v1.*;
import com.google.protobuf.Descriptors;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.time.Instant;


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

  static class WriteCommittedStream {

    final JsonStreamWriter jsonStreamWriter;

    public WriteCommittedStream(String projectId, String datasetName, String tableName) throws IOException, Descriptors.DescriptorValidationException, InterruptedException {

      try (BigQueryWriteClient client = BigQueryWriteClient.create()) {

        WriteStream stream = WriteStream.newBuilder().setType(WriteStream.Type.COMMITTED).build();
        TableName parentTable = TableName.of(projectId, datasetName, tableName);
        CreateWriteStreamRequest createWriteStreamRequest =
                CreateWriteStreamRequest.newBuilder()
                        .setParent(parentTable.toString())
                        .setWriteStream(stream)
                        .build();

        WriteStream writeStream = client.createWriteStream(createWriteStreamRequest);

        jsonStreamWriter = JsonStreamWriter.newBuilder(writeStream.getName(), writeStream.getTableSchema()).build();
      }
    }

    public ApiFuture<AppendRowsResponse> send(Arena arena) {
      Instant now = Instant.now();
      JSONArray jsonArray = new JSONArray();

      arena.state.forEach((url, playerState) -> {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("x", playerState.x);
        jsonObject.put("y", playerState.y);
        jsonObject.put("direction", playerState.direction);
        jsonObject.put("wasHit", playerState.wasHit);
        jsonObject.put("score", playerState.score);
        jsonObject.put("player", url);
        jsonObject.put("timestamp", now.getEpochSecond() * 1000 * 1000);
        jsonArray.put(jsonObject);
      });

      return jsonStreamWriter.append(jsonArray);
    }

  }

  final String projectId = ServiceOptions.getDefaultProjectId();
  final String datasetName = "snowball";
  final String tableName = "events";

  final WriteCommittedStream writeCommittedStream;

  public Application() throws Descriptors.DescriptorValidationException, IOException, InterruptedException {
    writeCommittedStream = new WriteCommittedStream(projectId, datasetName, tableName);
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
    writeCommittedStream.send(arenaUpdate.arena);
    PlayerState state = arenaUpdate.arena.state.get("https://34.117.104.217.sslip.io");
//    arenaUpdate.arena.state.forEach((player, playerState) ->
//            System.out.println(player + " -> " + playerState.direction + " " + playerState.score)
//    );
    int x = state.x;
    int y = state.y;
    String direction = state.direction;
    boolean wasHit = state.wasHit;
    if (wasHit) {
        return "F";
    }
    if (x == 0) {
        return "R";
    }
    if (y == 0) {
        return "R";
    }
    if (x > 39) {
        return "R";
    }
    if (y > 39) {
        return "R";
    }

      boolean matchGeneral = arenaUpdate.arena.state.values().stream().anyMatch(
              playerState -> playerState.x == x
                      && (playerState.y == y));
      if (matchGeneral) {
        return "F";
      }

    if (Objects.equals(direction, "N")) {
      boolean match = arenaUpdate.arena.state.values().stream().anyMatch(
              playerState -> playerState.x == x
                      && (playerState.y - y < 0 && playerState.y - y > -4));
      if (match) {
        return "T";
      } else {
        return "F";
      }
    }
    if (Objects.equals(direction, "E")) {
      boolean match = arenaUpdate.arena.state.values().stream().anyMatch(
              playerState -> playerState.y == y
                      && (playerState.x - x > 0 && playerState.x - x < 4));
      if (match) {
        return "T";
      } else {
        return "F";
      }
    }
    if (Objects.equals(direction, "W")) {
      boolean match = arenaUpdate.arena.state.values().stream().anyMatch(
              playerState -> playerState.y == y
                      && (playerState.x - x < 0 && playerState.x - x < -4));
      if (match) {
        return "T";
      } else {
        return "F";
      }
    }
    if (Objects.equals(direction, "S")) {
      boolean match = arenaUpdate.arena.state.values().stream().anyMatch(
              playerState -> playerState.x == x
                      && (playerState.y - y > 0 && playerState.y - y < 4));
      if (match) {
        return "T";
      } else {
        return "F";
      }
    }
    return "T";
  }
}

