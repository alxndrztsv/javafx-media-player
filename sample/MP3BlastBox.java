package sample;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;

// Positioning.
import javafx.geometry.Insets;
import javafx.geometry.Pos;

// Import labels, buttons, etc...
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;

// Import file managers.
import java.io.File;

public class MP3BlastBox extends Application {

    // Declare field variables.
    // Buttons.
    Button addButton, removeButton, removeAllButton,
            playButton, pauseButton, stopButton;
    // Labels.
    Label availableTracksLabel, selectedTracksLabel,
            volumeLabel, statusLabel;
    // ListView.
    ListView<String> availableTracksList, selectedTracksList;
    // Sliders.
    Slider volumeSlider, statusSlider;
    // MediaPlayer.
    Media media;
    MediaPlayer mediaPlayer;
    // Path to the music.
    String pathToMusic = "C:/Users/pc/IdeaProjects/MP3BlastBox/music";
    // Selected song.
    String selectedSong = "";
    String playingSong = "";
    // Duration of the song.
    Duration currentDuration, totalDuration, startDuration;

    // Create a constructor.
    public MP3BlastBox() {
        // Initialize fields.
        // Buttons.
        addButton = new Button("Add >");
        removeButton = new Button("< Remove");
        removeAllButton = new Button("<< Remove All");
        playButton = new Button("Play");
        pauseButton = new Button("Pause");
        stopButton = new Button("Stop");
        // Set the min width for buttons.
        addButton.setMinWidth(150);
        removeButton.setMinWidth(150);
        removeAllButton.setMinWidth(150);
        playButton.setMinWidth(150);
        pauseButton.setMinWidth(150);
        stopButton.setMinWidth(150);
        // Labels.
        availableTracksLabel = new Label("Available Tracks:");
        selectedTracksLabel = new Label("Selected Tracks:");
        volumeLabel = new Label("Volume:");
        statusLabel = new Label("Status:");
        // ListView.
        availableTracksList = new ListView<>();
        selectedTracksList = new ListView<>();
        // Sliders.
        volumeSlider = new Slider();
        statusSlider = new Slider();
        // Set slider values.
        volumeSlider.setValue(50);

        // Set the binding.
        addButton.disableProperty().bind(Bindings.isEmpty(availableTracksList.getSelectionModel().getSelectedItems()));
        removeButton.disableProperty().bind(Bindings.isEmpty(selectedTracksList.getSelectionModel().getSelectedItems()));
        removeAllButton.disableProperty().bind(Bindings.isEmpty(selectedTracksList.getItems()));

        // Firstly, buttons buttons are not clickable.
        playButton.setDisable(true);
        pauseButton.setDisable(true);
        stopButton.setDisable(true);
        volumeSlider.setDisable(true);
        statusSlider.setDisable(true);
    }

    // Method gets all music file names from the folder.
    private void populateTracksList(String path) {
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();
        try {
            for (File file : listOfFiles) availableTracksList.getItems().add(file.getName());
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }
    }

    // Method to add song to selectedTracksList.
    protected void addSong() {
        String selectedSongToAdd = availableTracksList.getSelectionModel().getSelectedItem();
        // If the song is actually selected.
        if (selectedSongToAdd != null && !selectedTracksList.getItems().contains(selectedSongToAdd)) {
            selectedTracksList.getItems().add(selectedSongToAdd);
        }
    }

    // Method to start playing the song.
    private void playSong() {

        // Set the playing song.
        playingSong = selectedSong;

        // Disable playButton.
        playButton.setDisable(true);

        // The rest buttons work.
        pauseButton.setDisable(false);
        stopButton.setDisable(false);
        volumeSlider.setDisable(false);
        statusSlider.setDisable(false);

        // Stop mediaPlayer if something is been played.
        if (mediaPlayer != null) {
            if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                mediaPlayer.stop();
            }

            // Check if the same song is about to play.
            if (selectedSong.equals(playingSong)) {
                // Playing after it's been paused.
                if (mediaPlayer.getStatus() == MediaPlayer.Status.PAUSED) {
                    mediaPlayer.seek(startDuration);
                    mediaPlayer.play();
                    return;
                }
                // Playing after it's been stopped.
                if (mediaPlayer.getStatus() == MediaPlayer.Status.STOPPED) {
                    mediaPlayer.play();
                    return;
                }
            }
        }
        // Initialize MediaPlayer.
        media = new Media("file:///" + pathToMusic + "/" + selectedSong);
        mediaPlayer = new MediaPlayer(media);

        // Listen to the player to be ready.
        mediaPlayer.setOnReady(() -> {
            totalDuration = media.getDuration();

            // Set the volume to 50%.
            mediaPlayer.setVolume(0.5);

            mediaPlayer.play();
        });

        // Update the time Slider when the song is been played.
        mediaPlayer.currentTimeProperty().addListener(ae -> showStatus(mediaPlayer.getStatus()));
    }

    // Method to pause the song.
    private void pauseSong() {
            // Pause mediaPlayer.
            mediaPlayer.pause();

            // Disable pauseButton.
            pauseButton.setDisable(true);

            // Enable playButton.
            playButton.setDisable(false);

            // Show the status when it's changed.
//            mediaPlayer.statusProperty().addListener(actionEvent -> showStatus(mediaPlayer.getStatus()));
            mediaPlayer.setOnPaused(() -> showStatus(mediaPlayer.getStatus()));

            // Here managing time.
            statusSlider.valueProperty().addListener(actionEvent -> {
                System.out.println("Value");
                startDuration = new Duration(totalDuration.toMillis() * statusSlider.getValue() / 100);
                System.out.println(totalDuration.toSeconds());
                System.out.println(statusSlider.getValue());
                System.out.println(startDuration.toSeconds());
            });
    }

    // To stop the song.
    private void stopSong() {
            // Stop mediaPlayer.
            mediaPlayer.stop();

            // Disable stopButton.
            stopButton.setDisable(true);

            // Enable playButton.
            playButton.setDisable(false);

            // Set to the beginning.
            statusSlider.setValue(0);

            // Show the status when it's changed.
//            mediaPlayer.statusProperty().addListener(actionEvent -> showStatus(mediaPlayer.getStatus()));
            mediaPlayer.setOnStopped(() -> showStatus(mediaPlayer.getStatus()));
    }

    // To remove the song.
    protected void removeSong() {
        selectedTracksList.getItems().remove(selectedSong);
        selectedTracksList.getSelectionModel().clearSelection();
        selectedSong = null;
    }

    // To remove all songs.
    protected void removeAllSongs() {
        // If mediaPlayer is occupied.
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.setOnStopped(() -> showStatus(mediaPlayer.getStatus()));
        }
        selectedTracksList.getItems().clear();
        selectedTracksList.getSelectionModel().clearSelection();
        selectedSong = null;
    }

    // To show the information about status.
    protected void showStatus(MediaPlayer.Status status) {
        currentDuration = mediaPlayer.getCurrentTime();
        statusLabel.setText(String.format("Status: %s: %02d:%02d", status,
                (int) currentDuration.toMinutes(),
                (int) (currentDuration.toSeconds() % 60)));
        statusSlider.setValue(currentDuration.toSeconds() * 100 / mediaPlayer.getTotalDuration().toSeconds());
    }

    @Override
    public void init() {
        // Get songs from the folder.
        populateTracksList(pathToMusic);

        // Add a song to selectedTracksList.
        addButton.setOnAction(actionEvent -> addSong());

        // Remove the song.
        removeButton.setOnAction(actionEvent -> removeSong());

        // Remove all songs.
        removeAllButton.setOnAction(actionEvent -> removeAllSongs());

        // Volume adjustment.
        volumeSlider.valueProperty().addListener(actionEvent -> {
            mediaPlayer.setVolume(volumeSlider.getValue() / 100.0);
            volumeLabel.setText("Volume: " + (int) volumeSlider.getValue() + "%");
        });

        // When the selectedTracksList is empty,
        // disable buttons and delete selectedSong.
        selectedTracksList.itemsProperty().addListener(ae -> {
            if (selectedTracksList.getItems().isEmpty()) {
                selectedSong = null;
                playButton.setDisable(true);
                pauseButton.setDisable(true);
                stopButton.setDisable(true);
                volumeSlider.setDisable(true);
                statusSlider.setDisable(true);
            }
        });

        // Play the song.
        playButton.setOnAction(actionEvent -> playSong());

        // Pause the song.
        pauseButton.setOnAction(actionEvent -> pauseSong());

        // Stop the song.
        stopButton.setOnAction(actionEvent -> stopSong());

        // If the song is selected.
        selectedTracksList.getSelectionModel().selectedItemProperty().addListener(ae -> {

            // Save selected song.
            selectedSong = selectedTracksList.getSelectionModel().getSelectedItem();

            // Check if selectedSong is not null.
                if (selectedSong != null) {

                // Condition to play song.
                if (mediaPlayer == null ||
                        mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING ||
                        mediaPlayer.getStatus() == MediaPlayer.Status.PAUSED ||
                        mediaPlayer.getStatus() == MediaPlayer.Status.STOPPED) {

                    // Enable playButton.
                    playButton.setDisable(false);
                }

                if (mediaPlayer != null) {

                    // Condition to stop player.
                    if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING ||
                            mediaPlayer != null && mediaPlayer.getStatus() == MediaPlayer.Status.PAUSED) {
                        // Enable stop button.
                        stopButton.setDisable(false);
                    }

                    // Condition to pause song.
                    if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                        // Enable pauseButton.
                        pauseButton.setDisable(false);
                    }

                    // Show status.
                    if (mediaPlayer != null) showStatus(mediaPlayer.getStatus());
                }

                // Disable playButton if the same song.
                if (selectedSong.equals(playingSong)) playButton.setDisable(true);
            }

            // Disable buttons if there are no songs to play.
            if (selectedTracksList.getItems().isEmpty()) {
                selectedSong = null;
                playButton.setDisable(true);
                pauseButton.setDisable(true);
                stopButton.setDisable(true);
                volumeSlider.setDisable(true);
                statusSlider.setDisable(true);
            }
        });
    }

    @Override
    public void start(Stage primaryStage) {
        // Set the title.
        primaryStage.setTitle("MP3BlastBox BlastBox");

        // Set width and height.
        primaryStage.setWidth(700);
        primaryStage.setHeight(500);

        // Layouts.
        // Create children layouts.
        VBox vbButtons = new VBox();

        // Create a parent layout.
        GridPane gpMain = new GridPane();

        // VBox layouts.
        // vbButtons.
        // Add elements to vbButtons.
        vbButtons.getChildren().addAll(addButton, removeButton, removeAllButton,
                playButton, pauseButton, stopButton, volumeLabel, volumeSlider);

        // Set the position.
        vbButtons.setAlignment(Pos.TOP_CENTER);

        // Set spacing.
        vbButtons.setSpacing(5);

        // Set minimum width of vbButtons
        vbButtons.setMinWidth(180);

        // GridPane layouts.
        // gpMain.
        // Add elements to gpMain.
        gpMain.add(availableTracksLabel, 0, 0);
        gpMain.add(selectedTracksLabel, 2, 0);
        gpMain.add(availableTracksList, 0, 1);
        gpMain.add(vbButtons, 1, 1);
        gpMain.add(selectedTracksList, 2, 1);
        gpMain.add(statusLabel, 2, 2);
        gpMain.add(statusSlider, 2, 3);

        // Set gaps.
        gpMain.setVgap(10);
        gpMain.setHgap(10);

        // Set padding.
        gpMain.setPadding(new Insets(10));

        // Create a scene and set it.
        Scene scene = new Scene(gpMain);
        primaryStage.setScene(scene);

        // Add styles to scene.
        scene.getStylesheets().add(getClass().getResource("styles.css").toString());

        // Add icon.
        primaryStage.getIcons().add(new Image(MP3BlastBox.class.getResourceAsStream("icon.png")));

        // Show the stage.
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
