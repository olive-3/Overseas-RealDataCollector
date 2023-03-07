package stock.overseas;

import org.json.simple.parser.ParseException;
import stock.overseas.directory.DirectoryService;
import stock.overseas.gui.MyGUI;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MainApp {

    public static void main(String[] args) {

        MyGUI myGUI = MyGUI.getInstance();
        DirectoryService directoryService = new DirectoryService();
        List<String> trKeyList = new ArrayList<>();

        try {
            directoryService.checkJsonFileExist();
            directoryService.checkJsonFileForm();
            trKeyList = directoryService.getTrKeyList();
            directoryService.checkDirectoryExist(trKeyList);
        } catch (FileNotFoundException e) {
            myGUI.exceptionHandling(LocalDateTime.now(), "RealDataCollector.json 파일이 존재하지 않습니다.");
        } catch (IOException | ParseException e) {
            myGUI.exceptionHandling(LocalDateTime.now(), "RealDataCollector.json 파일 파싱 중 오류 발생");
        }

        directoryService.checkDirectoryExist(trKeyList);
        directoryService.makeFiles(trKeyList);

    }
}
