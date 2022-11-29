package cs107;

import static cs107.Helper.Image;
import static cs107.Helper.generateImage;

/**
 * "Quite Ok Image" Decoder
 *
 * @author Hamza REMMAL (hamza.remmal@epfl.ch)
 * @version 1.2
 * @apiNote Third task of the 2022 Mini Project
 * @since 1.0
 */
public final class QOIDecoder {

    /**
     * DO NOT CHANGE THIS, MORE ON THAT IN WEEK 7.
     */
    private QOIDecoder() {
    }

    // ==================================================================================
    // =========================== QUITE OK IMAGE HEADER ================================
    // ==================================================================================

    /**
     * Extract useful information from the "Quite Ok Image" header
     *
     * @param header (byte[]) - A "Quite Ok Image" header
     * @return (int[]) - Array such as its content is {width, height, channels, color space}
     * @throws AssertionError See handouts section 6.1
     */
    public static int[] decodeHeader(byte[] header) {
        assert header != null;
        assert header.length == QOISpecification.HEADER_SIZE;
        byte[] magicTest = ArrayUtils.extract(header, 0, 4);
        for (int i = 0; i < 4; ++i) {
            assert magicTest[i] == QOISpecification.QOI_MAGIC[i];
        }

        byte[] largeur = ArrayUtils.extract(header, 4, 4);
        int LARGEUR = ArrayUtils.toInt(largeur);
        byte[] hauteur = ArrayUtils.extract(header, 8, 4);
        int HAUTEUR = ArrayUtils.toInt(hauteur);
        byte[] cannaux = ArrayUtils.extract(header, 12, 1);
        int CANNAUX = cannaux[0];

        assert CANNAUX == QOISpecification.RGB || CANNAUX == QOISpecification.RGBA;

        byte[] color = ArrayUtils.extract(header, 13, 1);
        int COLOR = color[0];

        assert COLOR == QOISpecification.sRGB || COLOR == QOISpecification.ALL;

        int[] decode = {LARGEUR, HAUTEUR, CANNAUX, COLOR};

        return decode;
    }


    // ==================================================================================
    // =========================== ATOMIC DECODING METHODS ==============================
    // ==================================================================================

    /**
     * Store the pixel in the buffer and return the number of consumed bytes
     *
     * @param buffer   (byte[][]) - Buffer where to store the pixel
     * @param input    (byte[]) - Stream of bytes to read from
     * @param alpha    (byte) - Alpha component of the pixel
     * @param position (int) - Index in the buffer
     * @param idx      (int) - Index in the input
     * @return (int) - The number of consumed bytes
     * @throws AssertionError See handouts section 6.2.1
     */
    public static int decodeQoiOpRGB(byte[][] buffer, byte[] input, byte alpha, int position, int idx) {
        assert buffer != null;
        assert input != null;
        assert (position >= 0) && (position < buffer.length);
        assert idx >= 0 && idx < input.length;
        assert input.length - idx >= 3;

        buffer[position][3] = alpha;
        for (int i = 0; i < 3; ++i) {
            buffer[position][i] = input[i + idx];

        }

        byte[] tabalpha = ArrayUtils.wrap(alpha);

        return 3;

    }

    /**
     * Store the pixel in the buffer and return the number of consumed bytes
     *
     * @param buffer   (byte[][]) - Buffer where to store the pixel
     * @param input    (byte[]) - Stream of bytes to read from
     * @param position (int) - Index in the buffer
     * @param idx      (int) - Index in the input
     * @return (int) - The number of consumed bytes
     * @throws AssertionError See handouts section 6.2.2
     */
    public static int decodeQoiOpRGBA(byte[][] buffer, byte[] input, int position, int idx) {
        assert buffer != null;
        assert input != null;
        assert (position >= 0) && (position < buffer.length);
        assert idx >= 0 && idx <= input.length;
        assert input.length - idx >= 4;

        buffer[position] = ArrayUtils.extract(input, idx, 4);


        return 4;
    }

    /**
     * Create a new pixel following the "QOI_OP_DIFF" schema.
     *
     * @param previousPixel (byte[]) - The previous pixel
     * @param chunk         (byte) - A "QOI_OP_DIFF" data chunk
     * @return (byte[]) - The newly created pixel
     * @throws AssertionError See handouts section 6.2.4
     */
    public static byte[] decodeQoiOpDiff(byte[] previousPixel, byte chunk) {
        assert previousPixel != null;
        assert previousPixel.length == 4;
        assert (chunk >>> 6) == (QOISpecification.QOI_OP_DIFF_TAG >>> 6);


        byte r = (byte) (((chunk >> 4)) & 0b11);
        byte g = (byte) (((chunk >> 2)) & 0b11);
        byte b = (byte) (((chunk)) & 0b11);

        byte[] dr = ArrayUtils.wrap((byte) (previousPixel[0] + r - 2));
        byte[] dg = ArrayUtils.wrap((byte) (previousPixel[1] + g - 2));
        byte[] db = ArrayUtils.wrap((byte) (previousPixel[2] + b - 2));
        byte[] a = ArrayUtils.wrap(previousPixel[3]);

        byte[] current_pixel = ArrayUtils.concat(dr, dg, db, a);

        return current_pixel;

    }

    /**
     * Create a new pixel following the "QOI_OP_LUMA" schema
     *
     * @param previousPixel (byte[]) - The previous pixel
     * @param data          (byte[]) - A "QOI_OP_LUMA" data chunk
     * @return (byte[]) - The newly created pixel
     * @throws AssertionError See handouts section 6.2.5
     */
    public static byte[] decodeQoiOpLuma(byte[] previousPixel, byte[] data) {
        assert previousPixel.length == 4;
        assert data != null;
        assert previousPixel != null;
        assert data[0] >>> 6 == QOISpecification.QOI_OP_LUMA_TAG >>> 6;


        byte g = (byte) (data[0] & 0b00_11_11_11);
        byte[] currentpixel = new byte[4];

        byte dg = (byte) ((g) - 32);
        byte drg = (byte) ((((data[1]) >>> 4) & 0b00_00_11_11) - 8 + dg);
        byte dbg = (byte) (((data[1]) & 0b00_00_11_11) - 8 + dg);

        currentpixel[0] = (byte) (previousPixel[0] + drg);
        currentpixel[1] = (byte) (previousPixel[1] + dg);
        currentpixel[2] = (byte) (previousPixel[2] + dbg);
        currentpixel[3] = (previousPixel[3]);

        return currentpixel;

    }

    /**
     * Store the given pixel in the buffer multiple times
     *
     * @param buffer   (byte[][]) - Buffer where to store the pixel
     * @param pixel    (byte[]) - The pixel to store
     * @param chunk    (byte) - a QOI_OP_RUN data chunk
     * @param position (int) - Index in buffer to start writing from
     * @return (int) - number of written pixels in buffer
     * @throws AssertionError See handouts section 6.2.6
     */
    public static int decodeQoiOpRun(byte[][] buffer, byte[] pixel, byte chunk, int position) {
        assert buffer != null;
        assert pixel != null;
        assert (position >= 0) && (position < buffer.length);
        assert pixel.length == 4;
        assert position < buffer.length;

        int count = ((chunk & 0b00111111) + 1);
        for (int i = 0; i < count; ++i) {
            buffer[position + i] = pixel;
        }
        return count - 1;

    }

    // ==================================================================================
    // ========================= GLOBAL DECODING METHODS ================================
    // ==================================================================================

    /**
     * Decode the given data using the "Quite Ok Image" Protocol
     *
     * @param data   (byte[]) - Data to decode
     * @param width  (int) - The width of the expected output
     * @param height (int) - The height of the expected output
     * @return (byte[][]) - Decoded "Quite Ok Image"
     * @throws AssertionError See handouts section 6.3
     */
    public static byte[][] decodeData(byte[] data, int width, int height) {
        assert data != null;
        assert width > 0;
        assert height > 0;
        int idx = 0;

        byte[] pixelPrecedent = QOISpecification.START_PIXEL;
        byte[][] image = new byte[width * height][4];
        byte[][] tabHach = new byte[64][4];
        int idxH = 0;

        for (int i = 0; i < data.length; ++i) {
            if ((data[i] >> 6) == (QOISpecification.QOI_OP_RUN_TAG >> 6) &&
                    data[i] != QOISpecification.QOI_OP_RGB_TAG &&
                    data[i] != QOISpecification.QOI_OP_RGBA_TAG) {
                idx = idx + decodeQoiOpRun(image, pixelPrecedent, data[i], idx);
            } else if ((data[i] >>> 6) == (QOISpecification.QOI_OP_DIFF_TAG >>> 6)) {
                image[idx] = decodeQoiOpDiff(pixelPrecedent, data[i]);
            } else if ((data[i] >>> 6) == QOISpecification.QOI_OP_INDEX_TAG >>> 6) {
                int index = data[i] & 0b00_11_11_11;
                image[idx] = tabHach[index];
            } else if ((data[i] >>> 6) == (QOISpecification.QOI_OP_LUMA_TAG >>> 6)) {
                byte n1 = data[i];
                byte n2 = data[i + 1];
                byte[] chunk3 = ArrayUtils.concat(n1, n2);
                image[idx] = decodeQoiOpLuma(pixelPrecedent, chunk3);
                ++i;
            } else if ((data[i]) == (QOISpecification.QOI_OP_RGB_TAG)) {
                i = i + decodeQoiOpRGB(image, data, pixelPrecedent[3], idx, i + 1);
            } else if ((data[i]) == (QOISpecification.QOI_OP_RGBA_TAG)) {
                i = i + decodeQoiOpRGBA(image, data, idx, i + 1);
            }
            idxH = QOISpecification.hash(image[idx]);
            tabHach[idxH] = image[idx];

            pixelPrecedent = image[idx];
            ++idx;
        }

        return image;
    }

    /**
     * Decode a file using the "Quite Ok Image" Protocol
     *
     * @param content (byte[]) - Content of the file to decode
     * @return (Image) - Decoded image
     * @throws AssertionError if content is null
     */
    public static Image decodeQoiFile(byte[] content) {
        assert content != null;
        assert ArrayUtils.equals(ArrayUtils.extract(content, content.length - 8, 8), QOISpecification.QOI_EOF);

        byte[] data = ArrayUtils.extract(content, QOISpecification.HEADER_SIZE, content.length - 22);

        byte[] widthB = ArrayUtils.extract(content, 4, 4);
        int width = ArrayUtils.toInt(widthB);

        byte[] heigthB = ArrayUtils.extract(content, 8, 4);
        int height = ArrayUtils.toInt(heigthB);

        byte[][] dataRGBA = decodeData(data, height, width);
        int[][] dataPIXEL = ArrayUtils.channelsToImage(dataRGBA, height, width);
        Image image = generateImage(dataPIXEL, (byte) content[12], (byte) content[13]);

        return image;

    }
}