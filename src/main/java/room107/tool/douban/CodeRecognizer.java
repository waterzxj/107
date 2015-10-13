package room107.tool.douban;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

/**
 * @author WangXiao
 */
public class CodeRecognizer {

    private File tmpDir;

    private String tesseractPath;

    public CodeRecognizer(String tesseractPath) {
        this(null, tesseractPath);
    }

    public CodeRecognizer(String tmpDirPath, String tesseractPath) {
        Validate.notNull(tesseractPath);
        this.tesseractPath = tesseractPath;
        if (tmpDirPath == null) {
            tmpDir = FileUtils.getTempDirectory();
        } else {
            tmpDir = new File(tmpDirPath);
            if (!tmpDir.exists()) {
                tmpDir.mkdirs();
            }
        }
    }

    /**
     * 
     * @param imageUri
     *            file path or URL
     */
    public synchronized String recognize(String imageUri) throws Exception {
        Validate.notNull(imageUri, "null imagePath");
        /*
         * draft
         */
        // parse
        CodeParser parser;
        try {
            parser = new CodeParser(new URL(imageUri));
        } catch (Exception e) {
            parser = new CodeParser(imageUri);
        }
        BufferedImage image = parser.parse();
        Validate.notNull(image, "null image");
        // save
        String tmpName = String.valueOf(System.currentTimeMillis());
        imageUri = ImageUtils.write(tmpDir.getAbsolutePath(), tmpName, image);
        // call
        String outFilePath = "o" + System.currentTimeMillis();
        String cmd = tesseractPath + " \"" + imageUri + "\" " + outFilePath
                + " -l eng -psm 8";
        // System.out.println(cmd);
        try {
            CmdUtils.exec(cmd, 2000);
        } finally {
            new File(imageUri).delete();
        }
        // read file
        File out = new File(outFilePath + ".txt");
        int retry = 3;
        StringBuilder b = new StringBuilder();
        while (retry > 0) {
            try {
                LineIterator iterator = FileUtils.lineIterator(out);
                while (iterator.hasNext()) {
                    String s = StringUtils.trimToNull(iterator.next());
                    if (s != null) {
                        b.append(s);
                    }
                }
                iterator.close();
                break;
            } catch (FileNotFoundException e) {
                retry--;
                Thread.sleep(100);
            }
        }
        out.delete();
        if (b.length() == 0) {
            return null;
        }
        /*
         * correct
         */
        String result = correct(b.toString());
        /*
         * guest from dictionary
         */
        return result;
    }

    private String correct(String s) {
        s = StringUtils.trimToNull(s);
        if (s == null) {
            return null;
        }
        s = s.toLowerCase();
        int l = s.length();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < l; i++) {
            char c = s.charAt(i);
            switch (c) {
            // letter
            case 'v':
                if (i == l - 1) {
                    c = 'y';
                }
                break;
            case 'é':
                c = 'e';
                break;
            // digit
            case '0':
                c = 'o';
                break;
            case '3':
                c = 'a';
                break;
            case '5':
                c = 's';
                break;
            // other
            case '[':
                c = 'c';
                break;
            case '|':
            case '/':
            case '\\':
                c = 'l'; // or i, r
                break;
            }
            builder.append(c);
        }
        return builder.toString();
    }

    public static void main(String[] args) throws Exception {
        CodeRecognizer recognizer = new CodeRecognizer(
                "C:/Users/Brainshawn/Desktop/tesseract-ocr-3.02-win32-portable/Tesseract-OCR/tesseract.exe");
        String dir = "douban/";
        // String[] files = { "awake","canvas" };
        // String[] files = { "addition", "church", "decision", "least",
        // "sharp",
        // "smoke" };
        // String[] files = { "animal" };
        String[] files = new File(dir).list();
        for (String f : files) {
            System.out.println(f);
            if (!f.contains(".")) {
                f += ".jpg";
            }
            System.out.println(recognizer.recognize(dir + f));
        }
    }
}
