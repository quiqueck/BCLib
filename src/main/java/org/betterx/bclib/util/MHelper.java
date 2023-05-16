package org.betterx.bclib.util;

import de.ambertation.wunderlib.math.Float3;

import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;

import org.joml.Vector3f;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class MHelper {
    public static final Vector3f YP = Float3.Y_AXIS.toVector3();

    static class ThreadLocalRandomSource implements RandomSource {
        ThreadLocalRandomSource(long seed) {

        }

        @Override
        public RandomSource fork() {
            return this;
        }

        @Override
        public PositionalRandomFactory forkPositional() {
            return null;
        }

        @Override
        public void setSeed(long l) {
            ThreadLocalRandom.current().setSeed(l);
        }

        @Override
        public int nextInt() {
            return ThreadLocalRandom.current().nextInt();
        }

        @Override
        public int nextInt(int i) {
            return ThreadLocalRandom.current().nextInt(i);
        }

        @Override
        public long nextLong() {
            return ThreadLocalRandom.current().nextLong();
        }

        @Override
        public boolean nextBoolean() {
            return ThreadLocalRandom.current().nextBoolean();
        }

        @Override
        public float nextFloat() {
            return ThreadLocalRandom.current().nextFloat();
        }

        @Override
        public double nextDouble() {
            return ThreadLocalRandom.current().nextDouble();
        }

        @Override
        public double nextGaussian() {
            return ThreadLocalRandom.current().nextGaussian();
        }
    }

    private static final Vec3i[] RANDOM_OFFSETS = new Vec3i[3 * 3 * 3 - 1];
    private static final float RAD_TO_DEG = 57.295779513082320876798154814105F;
    public static final float PHI = (float) (Math.PI * (3 - Math.sqrt(5)));
    public static final float PI2 = (float) (Math.PI * 2);
    public static final Random RANDOM = new Random();
    public static final RandomSource RANDOM_SOURCE = new ThreadLocalRandomSource(RANDOM.nextLong());

    public static int randRange(int min, int max, RandomSource random) {
        return min + random.nextInt(max - min + 1);
    }

    public static double randRange(double min, double max, RandomSource random) {
        return min + random.nextDouble() * (max - min);
    }

    public static float randRange(float min, float max, RandomSource random) {
        return min + random.nextFloat() * (max - min);
    }

    public static byte setBit(byte source, int pos, boolean value) {
        return value ? setBitTrue(source, pos) : setBitFalse(source, pos);
    }

    public static byte setBitTrue(byte source, int pos) {
        source |= 1 << pos;
        return source;
    }

    public static byte setBitFalse(byte source, int pos) {
        source &= ~(1 << pos);
        return source;
    }

    public static boolean getBit(byte source, int pos) {
        return ((source >> pos) & 1) == 1;
    }

    public static float wrap(float x, float side) {
        return x - floor(x / side) * side;
    }

    public static int floor(double x) {
        return x < 0 ? (int) (x - 1) : (int) x;
    }

    public static int min(int a, int b) {
        return a < b ? a : b;
    }

    public static int min(int a, int b, int c) {
        return min(a, min(b, c));
    }

    public static int max(int a, int b) {
        return a > b ? a : b;
    }

    public static float min(float a, float b) {
        return a < b ? a : b;
    }

    public static float max(float a, float b) {
        return a > b ? a : b;
    }

    public static float max(float a, float b, float c) {
        return max(a, max(b, c));
    }

    public static int max(int a, int b, int c) {
        return max(a, max(b, c));
    }

    public static boolean isEven(int num) {
        return (num & 1) == 0;
    }

    public static float lengthSqr(float x, float y, float z) {
        return x * x + y * y + z * z;
    }

    public static double lengthSqr(double x, double y, double z) {
        return x * x + y * y + z * z;
    }

    public static float length(float x, float y, float z) {
        return (float) Math.sqrt(lengthSqr(x, y, z));
    }

    public static double length(double x, double y, double z) {
        return Math.sqrt(lengthSqr(x, y, z));
    }

    public static float lengthSqr(float x, float y) {
        return x * x + y * y;
    }

    public static double lengthSqr(double x, double y) {
        return x * x + y * y;
    }

    public static float length(float x, float y) {
        return (float) Math.sqrt(lengthSqr(x, y));
    }

    public static double length(double x, double y) {
        return Math.sqrt(lengthSqr(x, y));
    }

    public static float dot(float x1, float y1, float z1, float x2, float y2, float z2) {
        return x1 * x2 + y1 * y2 + z1 * z2;
    }

    public static float dot(float x1, float y1, float x2, float y2) {
        return x1 * x2 + y1 * y2;
    }

    public static int getRandom(int x, int z) {
        int h = x * 374761393 + z * 668265263;
        h = (h ^ (h >> 13)) * 1274126177;
        return h ^ (h >> 16);
    }

    public static int getSeed(int seed, int x, int y) {
        int h = seed + x * 374761393 + y * 668265263;
        h = (h ^ (h >> 13)) * 1274126177;
        return h ^ (h >> 16);
    }

    public static int getSeed(int seed, int x, int y, int z) {
        int h = seed + x * 374761393 + y * 668265263 + z;
        h = (h ^ (h >> 13)) * 1274126177;
        return h ^ (h >> 16);
    }

    public static <T> void shuffle(T[] array, RandomSource random) {
        for (int i = 0; i < array.length; i++) {
            int i2 = random.nextInt(array.length);
            T element = array[i];
            array[i] = array[i2];
            array[i2] = element;
        }
    }

    public static int sqr(int i) {
        return i * i;
    }

    public static float sqr(float f) {
        return f * f;
    }

    public static double sqr(double d) {
        return d * d;
    }

    public static final float radiansToDegrees(float value) {
        return value * RAD_TO_DEG;
    }

    public static final float degreesToRadians(float value) {
        return value / RAD_TO_DEG;
    }

    public static Vector3f cross(Vector3f vec1, Vector3f vec2) {
        float cx = vec1.y * vec2.z - vec1.z * vec2.y;
        float cy = vec1.z * vec2.x - vec1.x * vec2.z;
        float cz = vec1.x * vec2.y - vec1.y * vec2.x;
        return new Vector3f(cx, cy, cz);
    }

    public static Vector3f normalize(Vector3f vec) {
        float length = lengthSqr(vec.x, vec.y, vec.z);
        if (length > 0) {
            length = (float) Math.sqrt(length);
            float x = vec.x / length;
            float y = vec.y / length;
            float z = vec.z / length;
            vec.set(x, y, z);
        }
        return vec;
    }

    public static float angle(Vector3f vec1, Vector3f vec2) {
        float dot = vec1.x * vec2.x + vec1.y * vec2.y + vec1.z * vec2.z;
        float length1 = lengthSqr(vec1.x, vec1.y, vec1.z);
        float length2 = lengthSqr(vec2.x, vec2.y, vec2.z);
        return (float) Math.acos(dot / Math.sqrt(length1 * length2));
    }

    public static Vector3f randomHorizontal(RandomSource random) {
        float angleY = randRange(0, PI2, random);
        float vx = (float) Math.sin(angleY);
        float vz = (float) Math.cos(angleY);
        return new Vector3f(vx, 0, vz);
    }

    public static Vec3i[] getOffsets(RandomSource random) {
        shuffle(RANDOM_OFFSETS, random);
        return RANDOM_OFFSETS;
    }

    static {
        int index = 0;
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x != 0 || y != 0 || z != 0) {
                        RANDOM_OFFSETS[index++] = new Vec3i(x, y, z);
                    }
                }
            }
        }
    }
}
