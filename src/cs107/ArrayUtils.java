package cs107;

/**
 * Utility class to manipulate arrays.
 *
 * @author Hamza REMMAL (hamza.remmal@epfl.ch)
 * @version 1.2
 * @apiNote First Task of the 2022 Mini Project
 * @since 1.0
 */
public final class ArrayUtils {

    /**
     * DO NOT CHANGE THIS, MORE ON THAT IN WEEK 7.
     */
    private ArrayUtils() {
    }

    // ==================================================================================
    // =========================== ARRAY EQUALITY METHODS ===============================
    // ==================================================================================

    /**
     * Check if the content of both arrays is the same
     *
     * @param a1 (byte[]) - First array
     * @param a2 (byte[]) - Second array
     * @return (boolean) - true if both arrays have the same content (or both null), false otherwise
     * @throws AssertionError if one of the parameters is null
     */
    public static boolean equals(byte[] a1, byte[] a2) {
        if ((a1 == null) && (a2 == null)) {
            return true;
        }
        if ((a1.length != a2.length)) {
            return false;
        }
        for (int j = 0; j < a1.length; ++j) {
            if (a1[j] != a2[j]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if the content of both arrays is the same
     *
     * @param a1 (byte[][]) - First array
     * @param a2 (byte[][]) - Second array
     * @return (boolean) - true if both arrays have the same content (or both null), false otherwise
     * @throws AssertionError if one of the parameters is null
     */
    public static boolean equals(byte[][] a1, byte[][] a2) {

        if ((a1 == null) && (a2 == null)) {
            return true;
        }
        if (a1.length != a2.length) {
            return false;
        }
        for (int j = 0; j < a1.length; ++j) {
            for (int i = 0; i < a1.length; ++j) {
                if (a1[i][j] != a2[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }

    // ==================================================================================
    // ============================ ARRAY WRAPPING METHODS ==============================
    // ==================================================================================

    /**
     * Wrap the given value in an array
     *
     * @param value (byte) - value to wrap
     * @return (byte[]) - array with one element (value)
     */
    public static byte[] wrap(byte value) {
        byte[] wrapped = {value};

        return wrapped;
    }

    // ==================================================================================
    // ========================== INTEGER MANIPULATION METHODS ==========================
    // ==================================================================================

    /**
     * Create an Integer using the given array. The input needs to be considered
     * as "Big Endian"
     * (See handout for the definition of "Big Endian")
     *
     * @param bytes (byte[]) - Array of 4 bytes
     * @return (int) - Integer representation of the array
     * @throws AssertionError if the input is null or the input's length is different from 4
     */
    public static int toInt(byte[] bytes) {
        assert bytes != null;
        assert bytes.length == 4;

        int index = bytes[0];
        for (int i = 1; i < bytes.length; ++i) {
            index = index << 8;
            index = (index | bytes[i] & 0b11111111);
        }
        return index;

    }

    /**
     * Separate the Integer (word) to 4 bytes. The Memory layout of this integer is "Big Endian"
     * (See handout for the definition of "Big Endian")
     *
     * @param value (int) - The integer
     * @return (byte[]) - Big Endian representation of the integer
     */
    public static byte[] fromInt(int value) {
        byte[] b = new byte[4];

        b[0] = (byte) ((value >>> 24));
        b[1] = (byte) ((value >>> 16));
        b[2] = (byte) ((value >>> 8));
        b[3] = (byte) (value);

        return b;
    }


    // ==================================================================================
    // ========================== ARRAY CONCATENATION METHODS ===========================
    // ==================================================================================

    /**
     * Concatenate a given sequence of bytes and stores them in an array
     *
     * @param bytes (byte ...) - Sequence of bytes to store in the array
     * @return (byte[]) - Array representation of the sequence
     * @throws AssertionError if the input is null
     */
    public static byte[] concat(byte... bytes) {
        assert bytes != null;

        byte[] tab = new byte[bytes.length];

        for (int i = 0; i < bytes.length; ++i) {
            tab[i] = bytes[i];
        }
        return tab;
    }

    /**
     * Concatenate a given sequence of arrays into one array
     *
     * @param tabs (byte[] ...) - Sequence of arrays
     * @return (byte[]) - Array representation of the sequence
     * @throws AssertionError if the input is null
     *                        or one of the inner arrays of input is null.
     */
    public static byte[] concat(byte[]... tabs) {
        assert tabs != null;
        int taille = tabs.length;
        int u = 0;
        int taille2 = 0;
        for (int i = 0; i < taille; ++i) {
            taille2 += tabs[i].length;
        }
        byte[] tab = new byte[taille2];
        for (int j = 0; j < tabs.length; ++j) {
            for (int h = 0; h < tabs[j].length; ++h) {
                tab[u] = tabs[j][h];
                u++;
            }
        }
        return tab;
    }

    // ==================================================================================
    // =========================== ARRAY EXTRACTION METHODS =============================
    // ==================================================================================

    /**
     * Extract an array from another array
     *
     * @param input  (byte[]) - Array to extract from
     * @param start  (int) - Index in the input array to start the extract from
     * @param length (int) - The number of bytes to extract
     * @return (byte[]) - The extracted array
     * @throws AssertionError if the input is null or start and length are invalid.
     *                        start + length should also be smaller than the input's length
     */
    public static byte[] extract(byte[] input, int start, int length) {
        assert input != null;
        assert (start >= 0) && (start < input.length);
        assert 0 <= length;
        assert start + length <= input.length;

        byte[] extracted = new byte[length];
        int j = 0;
        for (int i = start; i < length + start; ++i) {
            extracted[j] = input[i];
            j++;
        }
        return extracted;
    }


    /**
     * Create a partition of the input array.
     * (See handout for more information on how this method works)
     *
     * @param input (byte[]) - The original array
     * @param sizes (int ...) - Sizes of the partitions
     * @return (byte[][]) - Array of input's partitions.
     * The order of the partition is the same as the order in sizes
     * @throws AssertionError if one of the parameters is null
     *                        or the sum of the elements in sizes is different from the input's length
     */
    public static byte[][] partition(byte[] input, int... sizes) {
        assert ((input != null) && (sizes != null));
        int b = 0;
        for (int j = 0; j < sizes.length; ++j) {
            b = b + sizes[j];
        }
        assert ((input).length == b);


        byte[][] partition = new byte[sizes.length][];
        int s = 0;
        for (int i = 0; i < sizes.length; ++i) {
            if (i == 0) {
                s = 0;
            } else {
                s += sizes[i - 1];
            }
            partition[i] = extract(input, s, (sizes[i]));

        }
        return partition;
    }

    // ==================================================================================
    // ============================== ARRAY FORMATTING METHODS ==========================
    // ==================================================================================

    /**
     * Format a 2-dim integer array
     * where each dimension is a direction in the image to
     * a 2-dim byte array where the first dimension is the pixel
     * and the second dimension is the channel.
     * See handouts for more information on the format.
     *
     * @param input (int[][]) - image data
     * @return (byte [][]) - formatted image data
     * @throws AssertionError if the input is null
     *                        or one of the inner arrays of input is null
     */
    public static byte[][] imageToChannels(int[][] input) {
        assert input != null;
        for (int i = 0; i < input.length; ++i) {
            assert input[i] != null;
            assert input[0].length == input[i].length;
        }
        byte[][] convert = new byte[input.length * input[0].length][4];
        int var = 0;
        for (int i = 0; i < input.length; ++i) {
            for (int j = 0; j < input[i].length; ++j) {
                byte[] ligne = fromInt(input[i][j]);
                convert[var][0] = ligne[1];
                convert[var][1] = ligne[2];
                convert[var][2] = ligne[3];
                convert[var][3] = ligne[0];
                var = var + 1;
            }
        }
        return convert;
    }

    /**
     * Format a 2-dim byte array where the first dimension is the pixel
     * and the second is the channel to a 2-dim int array where the first
     * dimension is the height and the second is the width
     *
     * @param input  (byte[][]) : linear representation of the image
     * @param height (int) - Height of the resulting image
     * @param width  (int) - Width of the resulting image
     * @return (int[][]) - the image data
     * @throws AssertionError if the input is null
     *                        or one of the inner arrays of input is null
     *                        or input's length differs from width * height
     *                        or height is invalid
     *                        or width is invalid
     */
    public static int[][] channelsToImage(byte[][] input, int height, int width) {
        assert input.length == height * width;
        assert input != null;
        for (int i = 0; i < input.length; ++i) {
            assert input[i] != null;
            assert input[i].length == 4;
        }
        int[][] convert = new int[height][width];

        byte[][] tabargb = new byte[input.length][4];
        for (int i = 0; i < tabargb.length; ++i) {
            tabargb[i][0] = input[i][3];
            tabargb[i][3] = input[i][2];
            tabargb[i][2] = input[i][1];
            tabargb[i][1] = input[i][0];
        }

        int index = 0;
        for (int i = 0; i < convert.length; ++i) {
            for (int j = 0; j < convert[i].length; ++j) {
                convert[i][j] = toInt(tabargb[index]);
                index++;
            }
        }
        return convert;
    }
}
