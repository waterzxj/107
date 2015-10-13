package room107.tool.douban;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author WangXiao
 */
public class ImageUtils {

    @Setter
    static int blackDelta = 100;

    static boolean isWhite(BufferedImage image, int x, int y) {
        return isWhite(image, x, y, blackDelta);
    }

    static boolean isWhite(BufferedImage image, int x, int y, int delta) {
        if (x < 0 || y < 0 || x >= image.getWidth() || y >= image.getHeight()) {
            return false;
        }
        Color color = new Color(image.getRGB(x, y));
        return color.getRed() + color.getGreen() + color.getBlue() > delta;
    }

    static boolean isBlack(BufferedImage image, int x, int y) {
        return isBlack(image, x, y, blackDelta);
    }

    static boolean isBlack(BufferedImage image, int x, int y, int delta) {
        if (x < 0 || y < 0 || x >= image.getWidth() || y >= image.getHeight()) {
            return false;
        }
        Color color = new Color(image.getRGB(x, y));
        int mean = Math.abs(color.getRed() - color.getGreen())
                + Math.abs(color.getRed() - color.getBlue())
                + Math.abs(color.getBlue() - color.getGreen());
        if (mean >= 50) {
            return false;
        }
        return color.getRed() + color.getGreen() + color.getBlue() <= delta;
    }

    static void fill(BufferedImage image, int color) {
        int w = image.getWidth(), h = image.getHeight();
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                image.setRGB(i, j, color);
            }
        }
    }

    static BufferedImage clone(BufferedImage image) {
        BufferedImage result = new BufferedImage(image.getWidth(),
                image.getHeight(), image.getType());
        result.setData(image.getData());
        return result;
    }

    static int getDepth(BufferedImage image, int x, int y, Set<Point> accessed) {
        return getDepth(image, x, y, accessed, 0);
    }

    static int getDepth(BufferedImage image, int x, int y, Set<Point> accessed,
            int stackDepth) {
        if (++stackDepth > 2000) {
            return 0;
        }
        int w = image.getWidth(), h = image.getHeight();
        if (x < 0 || y < 0 || x >= w || y >= h || isWhite(image, x, y))
            return 0;
        Point p = new Point(x, y);
        if (accessed.contains(p)) {
            return 0;
        }
        accessed.add(p);
        return 1 + getDepth(image, x - 1, y, accessed, stackDepth)
                + getDepth(image, x, y - 1, accessed, stackDepth)
                + getDepth(image, x + 1, y, accessed, stackDepth)
                + getDepth(image, x, y + 1, accessed, stackDepth)
                + getDepth(image, x + 1, y - 1, accessed, stackDepth)
                + getDepth(image, x - 1, y - 1, accessed, stackDepth)
                + getDepth(image, x - 1, y + 1, accessed, stackDepth)
                + getDepth(image, x + 1, y + 1, accessed, stackDepth);
    }

    static BufferedImage cutMargin(BufferedImage image, boolean[][] hits,
            boolean byHorizontal, boolean byVertial) {
        int[][] mergedHits = getMergedHits(image, hits);
        boolean[][] b = toBooleans(mergedHits);
        int hStart = 0, hEnd = image.getWidth() - 1;
        if (byVertial) {
            IntPair pair = getEdge(b, 0, true);
            if (pair != null) {
                hStart = pair.x;
                hEnd = pair.y;
            }
        }
        int vStart = 0, vEnd = image.getHeight() - 1;
        if (byHorizontal) {
            IntPair pair = getEdge(b, 1, true);
            if (pair != null) {
                vStart = pair.x;
                vEnd = pair.y;
            }
        }
        return image.getSubimage(hStart, vStart, hEnd - hStart + 1, vEnd
                - vStart + 1);
    }

    static boolean[][] getHits(BufferedImage image) {
        int w = image.getWidth(), h = image.getHeight();
        boolean[][] result = new boolean[h][w];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                if (ImageUtils.isBlack(image, x, y)) {
                    result[y][x] = true;
                }
            }
        }
        return result;
    }

    // /**
    // *
    // * @param hits
    // * in and out
    // * @param image
    // * out
    // */
    // static void completeLines(boolean[][] hits, BufferedImage image) {
    // // int w = hits[0].length, h = hits.length;
    // // for (int x = 0; x < w; x++) {
    // // for (int y = 0; y < h; y++) {
    // // if (ImageUtils.isBlack(image, x, y)) {
    // // }
    // // }
    // // }
    // final int minPartHits = 5, maxGap = 3;
    // for (int i = 0; i < hits.length; i++) {
    // List<IntPair> ranges = getRanges(hits, i, true, 0);
    // IntPair pre = null;
    // for (IntPair intPair : ranges) {
    // if (pre!=null) {
    // if (pre.getRange()>=minPartHits && intPair.getRange()>=minPartHits &&
    // pre.y ) {
    //
    // }
    // }
    // pre = intPair;
    // }
    // }
    // }

    /**
     * @return colHits, rowHits
     */
    private static int[][] getMergedHits(BufferedImage image, boolean[][] hits) {
        int w = hits[0].length, h = hits.length;
        int[] colHits = new int[w], rowHits = new int[h];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                if (hits[y][x]) {
                    colHits[x]++;
                    rowHits[y]++;
                }
            }
        }
        return new int[][] { colHits, rowHits };
    }

    /**
     * @return max range
     */
    static IntPair getEdge(boolean[][] hits, int which, boolean byRow) {
        List<IntPair> ranges = getRanges(hits, which, byRow, 10);
        int max = -1;
        IntPair result = null;
        for (IntPair intPair : ranges) {
            if (intPair.getRange() > max) {
                max = intPair.getRange();
                result = intPair;
            }
        }
        return result;
    }

    static List<IntPair> getRanges(boolean[][] hits, int which, boolean byRow,
            int mergeDistance) {
        List<IntPair> result = new ArrayList<IntPair>();
        int start = -1, n = byRow ? hits[which].length : hits.length;
        for (int i = 0; i < n; i++) {
            boolean hit = byRow ? hits[which][i] : hits[i][which];
            if (hit) {
                if (start < 0) {
                    start = i;
                }
            } else {
                if (start >= 0) {
                    if (i > 0) {
                        if (result.isEmpty()) {
                            result.add(new IntPair(start, i - 1));
                        } else {
                            IntPair pre = result.get(result.size() - 1);
                            if (start - pre.y - 1 <= mergeDistance) { // merge
                                pre.y = i - 1;
                            } else {
                                result.add(new IntPair(start, i - 1));
                            }
                        }
                    }
                    start = -1;
                }
            }
        }
        if (start >= 0) {
            int i = n;
            if (result.isEmpty()) {
                result.add(new IntPair(start, i - 1));
            } else {
                IntPair pre = result.get(result.size() - 1);
                if (start - pre.y - 1 <= mergeDistance) { // merge
                    pre.y = i - 1;
                } else {
                    result.add(new IntPair(start, i - 1));
                }
            }
        }
        return result;
    }

    /**
     * @return written image path
     */
    static String write(String dirName, String fileName, BufferedImage image)
            throws IOException {
        File dir = new File(dirName);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        fileName = fileName.endsWith(".jpg") ? fileName : (fileName + ".jpg");
        File file = new File(dir, fileName);
        ImageIO.write(image, "jpg", file);
        return file.getAbsolutePath();
    }

    private static boolean[][] toBooleans(int[][] a) {
        boolean[][] result = new boolean[a.length][];
        for (int i = 0; i < a.length; i++) {
            result[i] = new boolean[a[i].length];
            for (int j = 0; j < a[i].length; j++) {
                result[i][j] = a[i][j] > 0;
            }
        }
        return result;
    }

    @NoArgsConstructor
    @RequiredArgsConstructor
    @ToString
    public static class IntPair {
        @NonNull
        int x, y;

        int mergeCount;

        Object data;

        public void unionMerge(IntPair p) {
            x = Math.min(x, p.x);
            y = Math.max(y, p.y);
            mergeCount++;
        }

        public int getRange() {
            return Math.abs(x - y);
        }

        public int getOverlap(IntPair p) {
            if (y + 1 == p.x || p.y + 1 == x) {
                return 0;
            }
            int delta = Math.min(y, p.y) - Math.max(x, p.x);
            return delta;
        }

        public int getMean() {
            return (x + y) / 2;
        }

        @SuppressWarnings("unchecked")
        public <T> T getData() {
            return (T) data;
        }
    }

}
