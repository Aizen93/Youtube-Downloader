import com.github.kiulian.downloader.OnYoutubeDownloadListener;
import com.github.kiulian.downloader.YoutubeDownloader;
import com.github.kiulian.downloader.YoutubeException;
import com.github.kiulian.downloader.model.VideoDetails;
import com.github.kiulian.downloader.model.YoutubeVideo;
import com.github.kiulian.downloader.model.formats.AudioVideoFormat;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

/**
 * FXML Controller class
 *
 * @author Oussama
 */
public class MenuPrincipaleFXMLController implements Initializable {    
    @FXML
    Button telecharger;
    @FXML
    TextField link; 
    @FXML
    ProgressBar progressBar;
    @FXML
    Text progresstext, info, info1, info2, info3, info4, info5;
    @FXML
    MenuButton lang;
    String language;

    public MenuPrincipaleFXMLController() {
        this.language = "FR";
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
        }else if(link.contains("https://www.youtube.com/watch?time_continue=")){//for https://www.youtube.com/watch?time_continue=1&v=Gi-n_k&feature=emb_logo
            String s = link.substring(link.indexOf("v=") + 2);
            result = s.substring(0, s.indexOf("&"));
        }else{//for https://youtu.be/R9_P_Tx4
            result = link.split("\\/")[3];
        }
        return result;
    }
    
    @FXML
    private void langENG(ActionEvent event){
        info.setText("1. Right click on the video");
        info1.setText("2. select \"Copy video URL\"");
        info2.setText("3. Right click the link's text field designated by the arrow");
        info3.setText("4. select Past");
        info4.setText("Link's textField");
        info5.setText("5. Click on download when you have pasted the link in the link's text field");
        telecharger.setText("Download");
        link.setPromptText("Past the link you just copied HERE...");
        lang.setText("English");
        this.language = "ENG";
    }
    
    @FXML
    private void langFR(ActionEvent event){
        info.setText("1. Tu clique droit sur la vidéo");
        info1.setText("2. tu sélèctione \"Copier l'URL de la vidéo\"");
        info2.setText("3. Tu clique droit sur la barre de lien désigner par la fléche");
        info3.setText("4.Tu sélèctione coller");
        info4.setText("Barre de lien");
        info5.setText("5. Tu clique sur telecharger quand tu aura coller le lien dans la barre de lien.");
        telecharger.setText("Télécharger");
        link.setPromptText("Colle le lien que tu viens de copier ICI...");
        lang.setText("Français");
        this.language = "FR";
    }
    
    @FXML
    private void download(ActionEvent event){
        try {
            if(link.getText() != null && !link.getText().trim().isEmpty()){
                telecharger.setDisable(true);
                // init downloader
                YoutubeDownloader downloader = new YoutubeDownloader();

                downloader.addCipherFunctionPattern(2, "\\b([a-zA-Z0-9$]{2})\\s*=\\s*function\\(\\s*a\\s*\\)\\s*\\{\\s*a\\s*=\\s*a\\.split\\(\\s*\"\"\\s*\\)");
                // extractor features
                downloader.setParserRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.121 Safari/537.36");
                downloader.setParserRetryOnFailure(1);

                // parsing data
                String videoId = getVideoID(link.getText());
                YoutubeVideo video = downloader.getVideo(videoId);

                // video details
                VideoDetails details = video.details();

                // get videos with audio
                List<AudioVideoFormat> videoWithAudioFormats = video.videoWithAudioFormats();

                // filtering only video formats
                /*List<VideoFormat> videoFormats = video.findVideoWithQuality(VideoQuality.large);
                videoFormats.forEach(it -> {
                    System.out.println(it.videoQuality() + " : " + it.url());
                });*/

                // itags can be found here - https://gist.github.com/sidneys/7095afe4da4ae58694d128b1034e01e2
                /*Format formatByItag = video.findFormatByItag(136); 
                if (formatByItag != null) {
                    System.out.println(formatByItag.url());
                }*/

                File outputDir = new File(System.getProperty("user.home")+"/Desktop");

                // async downloading with callback
                video.downloadAsync(videoWithAudioFormats.get(videoWithAudioFormats.size()-1), outputDir, new OnYoutubeDownloadListener() {
                    @Override
                    public void onDownloading(int progress) {
                        progressBar.setVisible(true);
                        progresstext.setVisible(true);
                        progresstext.setFill(Color.BLACK);
                        progressBar.setProgress((double)progress/100);
                        if(language.equals("FR")) progresstext.setText("En cours... " + progress + "%");
                        else if(language.equals("ENG")) progresstext.setText("Loading... " + progress + "%");
                        info.setFill(Color.BLACK);
                        info1.setFill(Color.BLACK);
                        info2.setFill(Color.BLACK);
                        info3.setFill(Color.BLACK);
                    }

                    @Override
                    public void onFinished(File file) {
                        if(language.equals("FR")) progresstext.setText("Téléchargement fini !");
                        else if(language.equals("ENG")) progresstext.setText("Download finished !");
                        progresstext.setFill(Color.GREEN);
                        telecharger.setDisable(false);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        if(language.equals("FR")) progresstext.setText("Erreur: Impossible de télécharger la vidéo");
                        else if(language.equals("ENG")) progresstext.setText("Error: Downloading video impossible");
                        progresstext.setFill(Color.GREEN);
                        telecharger.setDisable(false);
                    }
                });
            }else{
                info.setFill(Color.RED);
                info1.setFill(Color.RED);
                info2.setFill(Color.RED);
                info3.setFill(Color.RED);
            }
        }catch (YoutubeException | IOException | IndexOutOfBoundsException e){
            progresstext.setVisible(true);
            if(language.equals("FR")) progresstext.setText("Erreur: Impossible de télécharger la vidéo");
            else if(language.equals("ENG")) progresstext.setText("Error: Downloading video impossible");
            progresstext.setFill(Color.RED);
            telecharger.setDisable(false);
        }
    }
    
    /**
     * Initializes the controller class.
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Nothing to do here
    }    
    
}
