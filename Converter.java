import it.sauronsoftware.jave.AudioAttributes;
import it.sauronsoftware.jave.Encoder;
import it.sauronsoftware.jave.EncoderException;
import it.sauronsoftware.jave.EncodingAttributes;
import java.io.File;
/**
 *
 * @author Oussama
 */
public class Converter {
    
    public void convertMp4ToMp3(File source, File output){
        Encoder forMusic = new Encoder();

        EncodingAttributes specifications = new EncodingAttributes();
        specifications.setFormat("mp3");
        AudioAttributes a = new AudioAttributes();
        a.setVolume(256);//default
        a.setBitRate(320000);
        a.setSamplingRate(44100);
        a.setCodec("mp2");

        specifications.setAudioAttributes(a);
        
        try{
            forMusic.encode(source, output, specifications);
        }
        catch(EncoderException ex){
            
        }
    }
}