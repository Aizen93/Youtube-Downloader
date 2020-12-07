import com.github.kiulian.downloader.OnYoutubeDownloadListener;
import com.github.kiulian.downloader.YoutubeDownloader;
import com.github.kiulian.downloader.YoutubeException;
import com.github.kiulian.downloader.model.YoutubeVideo;
import com.github.kiulian.downloader.model.formats.AudioVideoFormat;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import javafx.animation.PauseTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * FXML Controller class
 *
 * @author Oussama
 */
public class MenuPrincipaleFXMLController implements Initializable {    
    @FXML AnchorPane mainAnchor;
    @FXML Button telecharger;
    @FXML TextField link; 
    @FXML ProgressBar progressBar;
    @FXML Text progresstext, bureau, info, info1, info2, info3, info4, info5, info6, info7, info8;
    @FXML MenuButton lang;
    @FXML ToggleButton audio, video;
    @FXML ToggleGroup format = new ToggleGroup();
    public BooleanProperty downloadFinished;
    private File videoFile;
    private String language;

    public MenuPrincipaleFXMLController() {
        this.language = "FR";
        downloadFinished = new SimpleBooleanProperty(false);
    }
    
    /**
     * 
     * Parses the youtube link to extract the video ID
     * there are 3 types of youtube links
     * @param link
     * @return 
     */
    private String getVideoID(String link){
        String result;
        if(link.contains("https://www.youtube.com/watch?v=")){//for https://www.youtube.com/watch?v=abc12345
            String[] res = link.split("\\?v=");
            result = res[1];
            if(result.contains("&")){
                result = result.substring(0, result.indexOf("&"));
            }
        }else if(link.contains("https://www.youtube.com/watch?time_continue=")){//for https://www.youtube.com/watch?time_continue=1&v=Gi-n_k&feature=emb_logo
            String s = link.substring(link.indexOf("v=") + 2);
            result = s.substring(0, s.indexOf("&"));
        }else{//for https://youtu.be/R9_P_Tx4
            result = link.split("\\/")[3];
        }
        return result;
    }
    
    @FXML
    private void langENG(){
        info.setText("1. Right click on the video");
        info1.setText("2. select \"Copy video URL\"");
        info2.setText("3. Right click the link's text field designated by the arrow");
        info3.setText("4. select Past");
        info4.setText("Link's textField");
        info5.setText("5. Click on download when you have pasted the link in the link's text field");
        info6.setText("6. Select the format you need, audio (mp3) or video (mp4)");
        info7.setText("Organize the desktop");
        info8.setText("Delete all videos");
        telecharger.setText("Download");
        link.setPromptText("Past the link you just copied HERE...");
        lang.setText("English");
        this.language = "ENG";
    }
    
    @FXML
    private void langFR(){
        info.setText("1. Cliquez droit sur la vidéo");
        info1.setText("2. Selectionez \"Copier l'URL de la vidéo\"");
        info2.setText("3. Cliquez droit sur la barre de lien désigner par la fléche");
        info3.setText("4. Selectionez coller");
        info4.setText("Barre de lien");
        info5.setText("5. Cliquez sur telecharger quand vous aurez coller le lien dans la barre de lien.");
        info6.setText("6. Selectionez le format souhaité, Audio (mp3) ou Vidéo (mp4)");
        info7.setText("Ranger le bureau");
        info8.setText("Supprimer les vidéos");
        telecharger.setText("Télécharger");
        link.setPromptText("Colle le lien que tu viens de copier ICI...");
        lang.setText("Français");
        this.language = "FR";
    }
    
    private void onDownloadingM(int progress){
        progresstext.setVisible(true);
        progresstext.setFill(Color.BLACK);
        progressBar.setProgress((double)progress/100);
        if(language.equals("FR")) progresstext.setText("En cours... " + progress + "%");
        else if(language.equals("ENG")) progresstext.setText("Loading... " + progress + "%");
        
    }
    
    private void onFinishedM(){
        if(language.equals("FR")) progresstext.setText("Téléchargement fini !");
        else if(language.equals("ENG")) progresstext.setText("Download finished !");
        progresstext.setFill(Color.GREEN);
        telecharger.setDisable(false);
        link.clear();
    }
    
    private void onErrorM(){
        if(language.equals("FR")) progresstext.setText("Erreur: Impossible de télécharger la vidéo");
        else if(language.equals("ENG")) progresstext.setText("Error: Failed to downloading video");
        progresstext.setFill(Color.GREEN);
        telecharger.setDisable(false);
    }
    
    @FXML
    private void download(){
        try {
            if(link.getText() != null && !link.getText().trim().isEmpty()){
                info.setFill(Color.BLACK);
                info1.setFill(Color.BLACK);
                info2.setFill(Color.BLACK);
                info3.setFill(Color.BLACK);
                telecharger.setDisable(true);
                // init downloader
                YoutubeDownloader downloader = new YoutubeDownloader();

                downloader.addCipherFunctionPattern(2, "\\b([a-zA-Z0-9$]{2})\\s*=\\s*function\\(\\s*a\\s*\\)\\s*\\{\\s*a\\s*=\\s*a\\.split\\(\\s*\"\"\\s*\\)");
                // extractor features
                downloader.setParserRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.121 Safari/537.36");
                downloader.setParserRetryOnFailure(1);

                // parsing data
                String videoId = getVideoID(link.getText());
                YoutubeVideo ytvideo = downloader.getVideo(videoId);
                
                File outputDir = new File(System.getProperty("user.home")+"\\Desktop");
                
                List<AudioVideoFormat> videoWithAudioFormats = ytvideo.videoWithAudioFormats();

                ytvideo.downloadAsync(videoWithAudioFormats.get(videoWithAudioFormats.size()-1), outputDir, new OnYoutubeDownloadListener() {
                    @Override
                    public void onDownloading(int progress) {
                        onDownloadingM(progress);
                    }

                    @Override
                    public void onFinished(File file) {
                        videoFile = file;
                        downloadFinished.setValue(true);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        onErrorM();
                    }
                });
                
            }else{
                info.setFill(Color.RED);
                info1.setFill(Color.RED);
                info2.setFill(Color.RED);
                info3.setFill(Color.RED);
            }
        }catch (YoutubeException | IOException | IndexOutOfBoundsException | NullPointerException e){
            progresstext.setVisible(true);
            if(language.equals("FR")) progresstext.setText("Erreur: Impossible de télécharger la vidéo");
            else if(language.equals("ENG")) progresstext.setText("Error: Failed to downloading video");
            progresstext.setFill(Color.RED);
            telecharger.setDisable(false);      
            
        }
    }
    
    private void onDownloadFinished(){
        if(audio.isSelected()){
            Converter convert = new Converter();

            String[] nameFile = videoFile.getAbsolutePath().split("\\\\");
            String[] removeExtension = nameFile[nameFile.length-1].split("\\.");

            File destinationOfficial = new File(System.getProperty("user.home")+"\\Desktop"+"\\"+removeExtension[0]+".mp3");

            convert.convertMp4ToMp3(videoFile, destinationOfficial);
        }

        onFinishedM();
    }
    
    private String findAvailableName(String name, String type){
        String[] copy = name.split("\\.");
        File copyfile = new File(System.getProperty("user.home")+"/Desktop/"+ type +"/"+name);
        int increase = 1;
        while(copyfile.exists()){
            name = copy[0] + "(copy"+ increase +")." + copy[1];
            copyfile = new File(System.getProperty("user.home")+"/Desktop/"+ type +"/"+name);
            increase++;
        }
        return name;
    }
    
    @FXML
    private void cleanDesktop(){
        try {
            DirectoryStream<Path> dir = Files.newDirectoryStream(Paths.get(System.getProperty("user.home")+"/Desktop"));
            Path videoDestDir = Paths.get(System.getProperty("user.home")+"/Desktop/Video");
            Path audioDestDir = Paths.get(System.getProperty("user.home")+"/Desktop/Audio");
            
            Files.createDirectories(videoDestDir);
            Files.createDirectories(audioDestDir);
            
            for (Path file : dir) {
                String name = file.getFileName().toString();
                
                if (!Files.isDirectory(file)) {
                    if(name.contains(".mp4")){
                        try {
                            Files.move(file, videoDestDir.resolve(name));
                        }catch(FileAlreadyExistsException ee){
                            name = findAvailableName(name, "Video");
                            Files.move(file, videoDestDir.resolve(name));
                        }
                    }else if(name.contains(".mp3")){
                        try{
                            Files.move(file, audioDestDir.resolve(name));
                        }catch(FileAlreadyExistsException ee){
                            name = findAvailableName(name, "Audio");
                            Files.move(file, audioDestDir.resolve(name));
                        }
                    }
                }
            }
            if(language.equals("FR"))bureau.setText("Le bureau a bien été ranger !");
            else if(language.equals("ENG")) bureau.setText("The desktop is clean !");
            notifMessage();
        } catch (IOException ex) {
            if(language.equals("FR"))bureau.setText("Une erreur est survenu !");
            else if(language.equals("ENG")) bureau.setText("An error occured !");
            bureau.setFill(Color.RED);
            bureau.setVisible(true);
        }
        
    }
    
    private void notifMessage(){
        bureau.setFill(Color.web("#00e817"));
        bureau.setVisible(true);
        Timer timer = new Timer();
        TimerTask task = new TimerTask(){
            @Override
            public void run(){
                bureau.setVisible(false);
                timer.cancel();
            }
        };
        timer.schedule(task,3000l);
    }
    
    @FXML
    private void deleteVideos(){
        try{
            File videodir = new File(System.getProperty("user.home")+"/Desktop/Video");
            File audiodir = new File(System.getProperty("user.home")+"/Desktop/Audio");
            for(File file: videodir.listFiles()) {
                if (!file.isDirectory()) 
                file.delete();
            }
            for(File file: audiodir.listFiles()) {
                if (!file.isDirectory()) 
                file.delete();
            }
            if(language.equals("FR"))bureau.setText("Les vidéos ont bien été supprimés !");
            else if(language.equals("ENG")) bureau.setText("The videos have been deleted !");
            notifMessage();
        }catch(Exception e){
        }
    }
    
    /**
     * Initializes the controller class.
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        PauseTransition delay = new PauseTransition(Duration.millis(1000));
        delay.setOnFinished(event -> {
            audio.setStyle("-fx-opacity: 0.4;");
            video.setStyle("-fx-opacity: 1.0;");
            audio.setDisable(false);
            video.setDisable(true);
            format.selectedToggleProperty().addListener((ObservableValue<? extends Toggle> ov, Toggle toggle, Toggle new_toggle) -> {    
                if(((ToggleButton)format.getSelectedToggle()).equals(audio)){
                    audio.setStyle("-fx-opacity: 1.0;");
                    video.setStyle("-fx-opacity: 0.4;");
                    audio.setDisable(true);
                    video.setDisable(false);
                }else if(((ToggleButton)format.getSelectedToggle()).equals(video)){
                    audio.setStyle("-fx-opacity: 0.4;");
                    video.setStyle("-fx-opacity: 1.0;");
                    audio.setDisable(false);
                    video.setDisable(true);
                }
            });

            downloadFinished.addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                if(newValue == true){
                    onDownloadFinished();
                    downloadFinished.setValue(false);
                }
            });
        } );
        delay.play();
        
        
        
        
    }    
    
}
