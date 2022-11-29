package cs107;

import java.util.ArrayList;

/**
 * "Quite Ok Image" Encoder
 *
 * @author Hamza REMMAL (hamza.remmal@epfl.ch)
 * @version 1.2
 * @apiNote Second task of the 2022 Mini Project
 * @since 1.0
 */
public final class QOIEncoder {

    /**
     * DO NOT CHANGE THIS, MORE ON THAT IN WEEK 7.
     */
    private QOIEncoder() {
    }

    // ==================================================================================
    // ============================ QUITE OK IMAGE HEADER ===============================
    // ==================================================================================

    /**
     * Generate a "Quite Ok Image" header using the following parameters
     *
     * @param image (Helper.Image) - Image to use
     * @return (byte[]) - Corresponding "Quite Ok Image" Header
     * @throws AssertionError if the colorspace or the number of channels is corrupted or if the image is null.
     *                        (See the "Quite Ok Image" Specification or the handouts of the project for more information)
     */
    public static byte[] qoiHeader(Helper.Image image) {
        assert image != null;
        assert image.channels() == 3 || image.channels() == 4;
        assert image.color_space() == 0 || image.color_space() == 1;

        int[][] data = image.data();
        byte[] largeur = ArrayUtils.fromInt(data[0].length);
        byte[] hauteur = ArrayUtils.fromInt(data.length);
        int can = image.channels();
        byte[] cannaux = ArrayUtils.fromInt(can);
        byte[] cancan = ArrayUtils.extract(cannaux, 3, 1);
        int col = image.color_space();
        byte[] color = ArrayUtils.fromInt(col);
        byte[] colcol = ArrayUtils.extract(color, 3, 1);
        byte[] header = ArrayUtils.concat(QOISpecification.QOI_MAGIC, largeur, hauteur, cancan, colcol);

        return header;

    }

    // ==================================================================================
    // ============================ ATOMIC ENCODING METHODS =============================
    // ==================================================================================

    /**
     * Encode the given pixel using the QOI_OP_RGB schema
     *
     * @param pixel (byte[]) - The Pixel to encode
     * @return (byte[]) - Encoding of the pixel using the QOI_OP_RGB schema
     * @throws AssertionError if the pixel's length is not 4
     */
    public static byte[] qoiOpRGB(byte[] pixel) {
        assert pixel.length == 4;

        byte[] tag = {(byte) QOISpecification.QOI_OP_RGB_TAG};
        byte[] red = ArrayUtils.extract(pixel, 0, 1);
        byte[] green = ArrayUtils.extract(pixel, 1, 1);
        byte[] blue = ArrayUtils.extract(pixel, 2, 1);

        byte[] rgb = ArrayUtils.concat(tag, red, green, blue);

        return rgb;
    }

    /**
     * Encode the given pixel using the QOI_OP_RGBA schema
     *
     * @param pixel (byte[]) - The pixel to encode
     * @return (byte[]) Encoding of the pixel using the QOI_OP_RGBA schema
     * @throws AssertionError if the pixel's length is not 4
     */
    public static byte[] qoiOpRGBA(byte[] pixel) {
        assert pixel.length == 4;

        byte[] tag = {(byte) QOISpecification.QOI_OP_RGBA_TAG};
        byte[] red = ArrayUtils.extract(pixel, 0, 1);
        byte[] green = ArrayUtils.extract(pixel, 1, 1);
        byte[] blue = ArrayUtils.extract(pixel, 2, 1);
        byte[] alpha = ArrayUtils.extract(pixel, 3, 1);

        byte[] rgba = ArrayUtils.concat(tag, red, green, blue, alpha);

        return rgba;
    }

    /**
     * Encode the index using the QOI_OP_INDEX schema
     *
     * @param index (byte) - Index of the pixel
     * @return (byte[]) - Encoding of the index using the QOI_OP_INDEX schema
     * @throws AssertionError if the index is outside the range of all possible indices
     */
    public static byte[] qoiOpIndex(byte index) {
        assert (int) index >= 0;
        assert (int) index <= 63;
        byte OpIndex = (byte) (index | (QOISpecification.QOI_OP_INDEX_TAG));
        byte[] encoding = ArrayUtils.wrap(OpIndex);

        return encoding;
    }


    /**
     * Encode the difference between 2 pixels using the QOI_OP_DIFF schema
     *
     * @param diff (byte[]) - The difference between 2 pixels
     * @return (byte[]) - Encoding of the given difference
     * @throws AssertionError if diff doesn't respect the constraints or diff's length is not 3
     *                        (See the handout for the constraints)
     */
    public static byte[] qoiOpDiff(byte[] diff) {
        assert (diff.length == 3);
        assert (diff != null);
        for (int i = 0; i < diff.length; ++i) {
            assert ((-3 < diff[i]) && (diff[i] < 2));
        }
        byte r = (byte) ((diff[0] + 2) << 4);
        byte g = (byte) ((diff[1] + 2) << 2);
        byte b = (byte) ((diff[2] + 2));

        byte diff1 = (byte) ((r | g | b) | QOISpecification.QOI_OP_DIFF_TAG);

        return ArrayUtils.wrap(diff1);
    }

    /**
     * Encode the difference between 2 pixels using the QOI_OP_LUMA schema
     *
     * @param diff (byte[]) - The difference between 2 pixels
     * @return (byte[]) - Encoding of the given difference
     * @throws AssertionError if diff doesn't respect the constraints
     *                        or diff's length is not 3
     *                        (See the handout for the constraints)
     */
    public static byte[] qoiOpLuma(byte[] diff) {
        assert (diff.length == 3);
        assert (diff != null);
        assert ((-33 < diff[1]) && (diff[1] < 32));
        assert (-9 < (diff[0] - diff[1]) && (diff[0] - diff[1]) < 8);
        assert (-9 < (diff[2] - diff[1]) && (diff[2] - diff[1]) < 8);

        byte dr = diff[0];
        byte dg = diff[1];
        byte db = diff[2];

        byte diffg = (byte) (((dg) + 32) | QOISpecification.QOI_OP_LUMA_TAG);
        byte diffrg = (byte) ((dr - dg) + 8 << 4);
        byte diffbg = (byte) ((db - dg) + 8);
        byte diffrgb = (byte) (diffrg | diffbg);
        byte[] difference = {diffg, diffrgb};

        return difference;
    }

    /**
     * Encode the number of similar pixels using the QOI_OP_RUN schema
     *
     * @param count (byte) - Number of similar pixels
     * @return (byte[]) - Encoding of count
     * @throws AssertionError if count is not between 0 (exclusive) and 63 (exclusive)
     */
    public static byte[] qoiOpRun(byte count) {
        assert ((int) (count) >= 1);
        assert ((int) (count) <= 62);

        byte newcount = (byte) ((int) count - 1); // décalage de -1
        byte bytecount = (byte) (newcount | QOISpecification.QOI_OP_RUN_TAG);
        byte[] tabcount = ArrayUtils.wrap(bytecount);

        return tabcount;
    }

    // ==================================================================================
    // ============================== GLOBAL ENCODING METHODS  ==========================
    // ==================================================================================

    /**
     * Encode the given image using the "Quite Ok Image" Protocol
     * (See handout for more information about the "Quite Ok Image" protocol)
     *
     * @param image (byte[][]) - Formatted image to encode
     * @return (byte[]) - "Quite Ok Image" representation of the image
     */
    public static byte[] encodeData(byte[][] image) {
        assert image != null;
        for (int i = 0; i < image.length; ++i) {
            assert image[i].length == 4;
            assert image[i] != null;
        }

        byte[] pixelPrecedent = QOISpecification.START_PIXEL;
        byte[][] tabHach = new byte[64][4];
        ArrayList<byte[]> encode = new ArrayList<>();
        int compteur = 0;

        for (int i = 0; i < image.length; ++i) {
            if (ArrayUtils.equals(image[i], pixelPrecedent)) {
                ++compteur;
                if (((compteur == 62) || (i == image.length - 1))) {
                    encode.add(qoiOpRun((byte) compteur));
                    compteur = 0;
                }
            } else {
                if ((compteur > 0)) {
                    encode.add(qoiOpRun((byte) compteur));
                    compteur = 0;
                }
                if (ArrayUtils.equals(tabHach[QOISpecification.hash(image[i])], image[i])) {
                    encode.add(qoiOpIndex(QOISpecification.hash(image[i])));

                } else {
                    tabHach[QOISpecification.hash(image[i])] = image[i];

                    if ((image[i][3] == pixelPrecedent[3]) &&
                            (byte) (image[i][0] - pixelPrecedent[0]) < 2 && (byte) (image[i][0] - pixelPrecedent[0]) > -3
                            && (byte) (image[i][1] - pixelPrecedent[1]) < 2 && (byte) (image[i][1] - pixelPrecedent[1]) > -3
                            && (byte) (image[i][2] - pixelPrecedent[2]) < 2 && (byte) (image[i][2] - pixelPrecedent[2]) > -3) {

                        byte dr = (byte) (image[i][0] - pixelPrecedent[0]);
                        byte dg = (byte) (image[i][1] - pixelPrecedent[1]);
                        byte db = (byte) (image[i][2] - pixelPrecedent[2]);
                        byte[] delta = ArrayUtils.concat(dr, dg, db);

                        encode.add(qoiOpDiff(delta));
                    } else {
                        if ((image[i][3] == pixelPrecedent[3]) &&
                                (-33 < ((byte) (image[i][1] - pixelPrecedent[1]))) && ((byte) (image[i][1] - pixelPrecedent[1]) < 32) &&
                                (-9 < ((byte) (image[i][0] - pixelPrecedent[0]) - (byte) (image[i][1] - pixelPrecedent[1])) && (((byte) (image[i][0] - pixelPrecedent[0]) - (byte) (image[i][1] - pixelPrecedent[1])) < 8) &&
                                        (-9 < ((byte) (image[i][2] - pixelPrecedent[2]) - (byte) (image[i][1] - pixelPrecedent[1]))) && ((byte) (image[i][2] - pixelPrecedent[2]) - (byte) (image[i][1] - pixelPrecedent[1])) < 8)) {


                            byte drr = (byte) (image[i][0] - pixelPrecedent[0]);
                            byte dgg = (byte) (image[i][1] - pixelPrecedent[1]);
                            byte dbb = (byte) (image[i][2] - pixelPrecedent[2]);
                            byte[] delta = ArrayUtils.concat(drr, dgg, dbb);
                            encode.add(qoiOpLuma(delta));
                        } else {
                            if (image[i][3] == pixelPrecedent[3]) {
                                encode.add(qoiOpRGB(image[i]));
                            } else {
                                encode.add(qoiOpRGBA(image[i]));
                            }
                        }
                    }
                }
            }
            pixelPrecedent = image[i];
        }
        byte[][] data;
        data = encode.toArray(new byte[0][]);


        return ArrayUtils.concat(data);


    }


    /**
     * Creates the representation in memory of the "Quite Ok Image" file.
     *
     * @param image (Helper.Image) - Image to encode
     * @return (byte[]) - Binary representation of the "Quite Ok File" of the image
     * @throws AssertionError if the image is null
     * @apiNote THE FILE IS NOT CREATED YET, THIS IS JUST ITS REPRESENTATION.
     * TO CREATE THE FILE, YOU'LL NEED TO CALL Helper::write
     */
    public static byte[] qoiFile(Helper.Image image) {
        assert image != null;

        byte[] head = qoiHeader(image);
        int[][] dataPIXEL = image.data();
        byte[][] dataRGBA = ArrayUtils.imageToChannels(dataPIXEL);
        byte[] body = encodeData(dataRGBA);

        return ArrayUtils.concat(head, body, QOISpecification.QOI_EOF);
    }

}