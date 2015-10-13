package room107.tool.douban;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.Validate;

import room107.tool.douban.ImageUtils.IntPair;

/**
 * @author WangXiao
 */
public class CodeParser {

    private String imageName;

    private BufferedImage image;

    public CodeParser(URL imageUrl) throws IOException {
        Validate.notNull(imageUrl);
        imageName = URLEncoder.encode(imageUrl.toString(), "UTF-8");
        URLConnection connection = imageUrl.openConnection();
        connection.setConnectTimeout(1000);
        connection.setReadTimeout(1000);
        connection.setRequestProperty("User-Agent", DoubanUpper.UA);
        connection.setRequestProperty("referer", DoubanUpper.REF);
        image = ImageIO.read(connection.getInputStream());
    }

    public CodeParser(String imagePath) throws IOException {
        Validate.notNull(imagePath);
        File f = new File(imagePath);
        imageName = f.getName();
        image = ImageIO.read(f);
    }

    private void removeBackground() {
        int w = image.getWidth(), h = image.getHeight();
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                if (!ImageUtils.isBlack(image, x, y)) {
                    image.setRGB(x, y, Color.WHITE.getRGB());
                }
            }
        }
    }

    private void removeNoise() {
        int w = image.getWidth(), h = image.getHeight(), minDepth = 20, maxDepth = 500, blockRadius = 8;
        float blockEdgeFillRate = 0.8f;
        Set<Point> globalAccessed = new HashSet<Point>();
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                if (globalAccessed.contains(new Point(x, y))
                        || !ImageUtils.isBlack(image, x, y)) {
                    continue;
                }
                Set<Point> accessed = new HashSet<Point>();
                // by depth
                int depth = ImageUtils.getDepth(image, x, y, accessed);
                if (depth < minDepth || depth >= maxDepth) {
                    for (Point point : accessed) {
                        image.setRGB(point.x, point.y, Color.WHITE.getRGB());
                    }
                } else { // by block
                    for (Point point : accessed) {
                        int radius;
                        for (radius = 1; radius <= blockRadius; radius++) {
                            int rX1 = point.x - radius, rX2 = point.x + radius, rY1 = point.y
                                    - radius, rY2 = point.y + radius;
                            int circleSum = 0, circleBlackSum = 0, k;
                            for (int j = rX1; j <= rX2; j++) {
                                k = rY1;
                                if (j >= 0 && j < w && j >= 0 && k < h) {
                                    circleSum += 1;
                                    circleBlackSum += ImageUtils.isBlack(image,
                                            j, k) ? 1 : 0;
                                }
                                k = rY2;
                                if (j >= 0 && j < w && j >= 0 && k < h) {
                                    circleSum += 1;
                                    circleBlackSum += ImageUtils.isBlack(image,
                                            j, k) ? 1 : 0;
                                }
                            }
                            for (int j = rY1; j <= rY2; j++) {
                                k = rX1;
                                if (j >= 0 && j < w && j >= 0 && k < h) {
                                    circleSum += 1;
                                    circleBlackSum += ImageUtils.isBlack(image,
                                            k, j) ? 1 : 0;
                                }
                                k = rX2;
                                if (j >= 0 && j < w && j >= 0 && k < h) {
                                    circleSum += 1;
                                    circleBlackSum += ImageUtils.isBlack(image,
                                            k, j) ? 1 : 0;
                                }
                            }
                            float fillRate = circleBlackSum
                                    / ((float) circleSum);
                            if (fillRate < blockEdgeFillRate) {
                                break; // not block
                            }
                        }
                        if (radius >= blockRadius) { // in block
                            for (Point p : accessed) {
                                image.setRGB(p.x, p.y, Color.WHITE.getRGB());
                            }
                            break;
                        }
                    }
                }
                globalAccessed.addAll(accessed);
            }
        }
    }

    private void removeMarginNoise(boolean[][] hits) {
        final int minHeight = 10;
        // calculate height for each block
        List<IntPair> segs = new LinkedList<IntPair>();
        for (int x = 0; x < hits[0].length; x++) {
            List<IntPair> ranges = ImageUtils.getRanges(hits, x, false, 0);
            if (!ranges.isEmpty()) {
                for (IntPair intPair : ranges) {
                    intPair.data = new IntPair(x, x);
                    segs.add(intPair);
                }
                unionMerge(segs);
            }
        }
        // filter small
        CollectionUtils.filter(segs, new Predicate() {
            @Override
            public boolean evaluate(Object object) {
                return ((IntPair) object).getRange() >= minHeight;
            }
        });
        // calculate overlap
        final float minOverlap = 0.5f;
        for (IntPair seg : segs) {
            for (IntPair p : segs) {
                if (seg == p) {
                    continue;
                }
                float overlap = seg.getOverlap(p);
                if (overlap > 0 && overlap / seg.getRange() >= minOverlap
                        && overlap / p.getRange() >= minOverlap) {
                    int delta = (((IntPair) seg.data).y - ((IntPair) seg.data).x)
                            + (((IntPair) p.data).y - ((IntPair) p.data).x);
                    seg.mergeCount += delta;
                    p.mergeCount += delta;
                }
            }
        }
        // filter noise block
        final int minMergeCount = 70;
        CollectionUtils.filter(segs, new Predicate() {
            @Override
            public boolean evaluate(Object object) {
                return ((IntPair) object).mergeCount >= minMergeCount;
            }
        });
        // filter noise point
        for (int i = 0; i < hits.length; i++) {
            for (int j = 0; j < hits[i].length; j++) {
                if (hits[i][j]) {
                    boolean preserve = false;
                    for (IntPair seg : segs) {
                        if (seg.x <= i && seg.y >= i) {
                            IntPair p = seg.getData();
                            if (p.x <= j && p.y >= j) {
                                preserve = true;
                                break;
                            }
                        }
                    }
                    if (!preserve) {
                        hits[i][j] = false;
                        image.setRGB(j, i, Color.WHITE.getRGB());
                    }
                }
            }
        }
    }

    private void unionMerge(Collection<IntPair> pairs) {
        boolean merged = false;
        Iterator<IntPair> iterator = pairs.iterator();
        all: while (iterator.hasNext()) {
            IntPair p = iterator.next();
            int offset = ((IntPair) p.data).y;
            for (IntPair p2 : pairs) {
                if (p == p2) {
                    continue;
                }
                int offset2 = ((IntPair) p2.data).y;
                if (Math.abs(offset - offset2) <= 1) {
                    int overlap = p2.getOverlap(p);
                    if (overlap >= 0) {
                        p2.unionMerge(p);
                        ((IntPair) p2.data).x = Math.min(((IntPair) p.data).x,
                                ((IntPair) p2.data).x);
                        ((IntPair) p2.data).y = Math.max(offset, offset2);
                        p2.mergeCount += p.mergeCount;
                        merged = true;
                        iterator.remove();
                        break all;
                    }
                }
            }
        }
        if (merged) {
            unionMerge(pairs);
        }
    }

    private void split(BufferedImage image, List<BufferedImage> result) {
        int w = image.getWidth(), h = image.getHeight();
        Set<Point> accessed = new HashSet<Point>();
        all: for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                if (ImageUtils.getDepth(image, i, j, accessed) > 0) {
                    break all;
                }
            }
        }
        if (accessed.isEmpty()) {
            return;
        }
        // find connected points
        int x1 = Integer.MAX_VALUE, x2 = Integer.MIN_VALUE, y1 = Integer.MAX_VALUE, y2 = Integer.MIN_VALUE;
        for (Point point : accessed) {
            x1 = point.x < x1 ? point.x : x1;
            x2 = point.x > x2 ? point.x : x2;
            y1 = point.y < y1 ? point.y : y1;
            y2 = point.y > y2 ? point.y : y2;
        }
        BufferedImage cut = image.getSubimage(x1, y1, x2 - x1 + 1, y2 - y1 + 1);
        cut = ImageUtils.clone(cut);
        for (Point point : accessed) { // clear
            image.setRGB(point.x, point.y, Color.WHITE.getRGB());
            point.x = point.x - x1;
            point.y = point.y - y1;
        }
        accessed = new HashSet<Point>(accessed); // rehash
        // clear cut
        w = cut.getWidth();
        h = cut.getHeight();
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                if (!accessed.contains(new Point(i, j))) {
                    cut.setRGB(i, j, Color.WHITE.getRGB());
                }
            }
        }
        result.add(cut);
        split(image, result);
    }

    private BufferedImage merge(List<BufferedImage> splits) {
        Validate.notEmpty(splits);
        final int minSize = 12, space = 2;
        final float maxSingleRate = 1f;
        int width = 0, height = 0, imageType = 0;
        Iterator<BufferedImage> iterator = splits.iterator();
        image: while (iterator.hasNext()) {
            BufferedImage image = iterator.next();
            imageType = image.getType();
            // filter noise
            if (image.getWidth() < minSize && image.getHeight() < minSize) {
                iterator.remove();
                continue image;
            }
            boolean[][] hits = ImageUtils.getHits(image);
            int singlePointCount = 0;
            for (int i = 0; i < hits.length; i++) {
                for (int j = 0; j < hits[i].length; j++) {
                    if (hits[i][j]) {
                        int c = 0;
                        c += i - 1 >= 0 ? (hits[i - 1][j] ? 0 : 1) : 1;
                        c += j - 1 >= 0 ? (hits[i][j - 1] ? 0 : 1) : 1;
                        c += i + 1 < hits.length ? (hits[i + 1][j] ? 0 : 1) : 1;
                        c += j + 1 < hits[i].length ? (hits[i][j + 1] ? 0 : 1)
                                : 1;
                        if (c > 3) {
                            singlePointCount++;
                        }
                    }
                }
            }
            float r = singlePointCount
                    / ((float) image.getWidth() * image.getHeight()) * 100;
            if (r > maxSingleRate) {
                iterator.remove();
                continue image;
            }
            if (image.getHeight() > height) {
                height = image.getHeight();
            }
            width += image.getWidth() + space;
        }
        // merge
        BufferedImage result = new BufferedImage(width, height, imageType);
        ImageUtils.fill(result, Color.WHITE.getRGB());
        int xOffset = 0;
        for (BufferedImage image : splits) {
            int w = image.getWidth(), h = image.getHeight();
            int yOffset = height - h;
            for (int i = 0; i < w; i++) {
                for (int j = 0; j < h; j++) {
                    result.setRGB(xOffset + i, yOffset + j, image.getRGB(i, j));
                }
            }
            xOffset += w + space;
        }
        return result;
    }

    public BufferedImage parse() throws Exception {
        /*
         * cut code
         */
        // noise 1: 200
        ImageUtils.setBlackDelta(200);
        removeBackground();
        removeNoise();
        // noise 1: 200
        ImageUtils.setBlackDelta(100);
        removeBackground();
        removeNoise();
        // cut 1
        boolean[][] hits = ImageUtils.getHits(image);
        image = ImageUtils.cutMargin(image, hits, true, true);
        // noise 2
        hits = ImageUtils.getHits(image);
        removeMarginNoise(hits);
        image = ImageUtils.cutMargin(image, hits, true, true);
        /*
         * split
         */
        List<BufferedImage> splitImages = new ArrayList<BufferedImage>();
        split(ImageUtils.clone(image), splitImages);
        /*
         * merge
         */
        image = merge(splitImages);
        /*
         * write
         */
        // String dir = "out";
        // ImageUtils.write(dir, imageName, image);
        // test
        // int i = 0;
        // for (BufferedImage bufferedImage : splitImages) {
        // ImageUtils.write(dir, file.getName() + i++ + ".jpg", bufferedImage);
        // }
        return image;
    }

    public static void main(String[] args) throws Exception {
        String dir = "douban/";
        // String[] files = { "canvas", "awake", "young" };
        // String[] files = { "addition", "church", "decision" ,"least",
        // "sharp",
        // "smoke" };
        String[] files = new File(dir).list();
        // String[] files = { "animal" };
        for (String f : files) {
            System.out.println(f);
            if (!f.contains(".")) {
                f += ".jpg";
            }
            CodeParser parser = new CodeParser(dir + f);
            parser.parse();
        }
    }

}
