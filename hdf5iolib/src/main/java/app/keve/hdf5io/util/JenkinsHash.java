/*
 * Copyright 2020 Keve Müller
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package app.keve.hdf5io.util;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public final class JenkinsHash {
    private JenkinsHash() {

    }

    @Unsigned
    public static int hash(final ByteBuffer buf, final @Unsigned int initVal) {
        if (0 == buf.remaining() % 4) {
            return hashWord(buf.asIntBuffer(), initVal);
        }
        return hashBytes(buf, initVal);
    }

    @Unsigned
    public static int hashBytes(final ByteBuffer buf, final int initVal) {
        /* Set up the internal state */
        int a = 0xdeadbeef + buf.remaining() + initVal;
        int b = a;
        int c = a;

        int bRemaining = buf.remaining();
        final IntBuffer k = buf.asIntBuffer();
        /*------------------------------------------------- handle most of the key */
        while (bRemaining > 12) {
            a += k.get();
            b += k.get();
            c += k.get();

//            mix(a, b, c);
            a -= c;
            a ^= rot(c, 4);
            c += b;
            b -= a;
            b ^= rot(a, 6);
            a += c;
            c -= b;
            c ^= rot(b, 8);
            b += a;
            a -= c;
            a ^= rot(c, 16);
            c += b;
            b -= a;
            b ^= rot(a, 19);
            a += c;
            c -= b;
            c ^= rot(b, 4);
            b += a;

            bRemaining -= 3 * 4;
        }

//        System.err.println("kRemain" + k.remaining());
//        System.err.println("pos" + pos);
        // continue with the ByteBuffer for the last <=12 bytes
        buf.position(buf.position() + buf.remaining() - bRemaining);
//        System.err.println("bRemain" + buf.remaining());
        switch (buf.remaining()) {
        case 0:
            return c;
        case 1:
            a += buf.get();
            break;
        case 2:
            a += buf.get();
            a += (buf.get() & 0xFF) << 8;
            break;
        case 3:
            a += buf.get();
            a += (buf.get() & 0xFF) << 8;
            a += (buf.get() & 0xFF) << 16;
            break;
        case 4:
            a += buf.getInt();
            break;
        case 5:
            a += buf.getInt();
            b += buf.get();
            break;
        case 6:
            a += buf.getInt();
            b += buf.get();
            b += (buf.get() & 0xFF) << 8;
            break;
        case 7:
            a += buf.getInt();
            b += buf.get();
            b += (buf.get() & 0xFF) << 8;
            b += (buf.get() & 0xFF) << 16;
            break;
        case 8:
            a += buf.getInt();
            b += buf.getInt();
            break;
        case 9:
            a += buf.getInt();
            b += buf.getInt();
            c += buf.get();
            break;
        case 10:
            a += buf.getInt();
            b += buf.getInt();
            c += buf.get();
            c += (buf.get() & 0xFF) << 8;
            break;
        case 11:
            a += buf.getInt();
            b += buf.getInt();
            c += buf.get();
            c += (buf.get() & 0xFF) << 8;
            c += (buf.get() & 0xFF) << 16;
            break;
        case 12:
            a += buf.getInt();
            b += buf.getInt();
            c += buf.getInt();
            break;
        default:
            throw new IllegalArgumentException("Too many bytes " + buf.remaining());
        }

        assert 0 == buf.remaining();

        // final(a,b,c)
        c ^= b;
        c -= rot(b, 14);
        a ^= c;
        a -= rot(c, 11);
        b ^= a;
        b -= rot(a, 25);
        c ^= b;
        c -= rot(b, 16);
        a ^= c;
        a -= rot(c, 4);
        b ^= a;
        b -= rot(a, 14);
        c ^= b;
        c -= rot(b, 24);

        /*------------------------------------------------------ report the result */
        return c;
    }

    /**
     * Perform Jenkins hash on words.
     * 
     * @param k       the key, an array of uint32_t values
     * @param initVal the previous hash, or an arbitrary value
     * @return the hash
     */
    @Unsigned
    public static int hashWord(final IntBuffer k, final @Unsigned int initVal) {
        /*
         * -------------------------------------------------------------------- This
         * works on all machines. To be useful, it requires -- that the key be an array
         * of uint32_t's, and -- that the length be the number of uint32_t's in the key
         * 
         * The function hashword() is identical to hashlittle() on little-endian
         * machines, and identical to hashbig() on big-endian machines, except that the
         * length has to be measured in uint32_ts rather than in bytes. hashlittle() is
         * more complicated than hashword() only because hashlittle() has to dance
         * around fitting the key bytes into registers.
         * --------------------------------------------------------------------
         */
        /* Set up the internal state */
        int a = 0xdeadbeef + k.remaining() * 4 + initVal;
        int b = a;
        int c = a;
        /*------------------------------------------------- handle most of the key */
        while (k.remaining() > 3) {
            a += k.get();
            b += k.get();
            c += k.get();

//            mix(a, b, c);
            a -= c;
            a ^= rot(c, 4);
            c += b;
            b -= a;
            b ^= rot(a, 6);
            a += c;
            c -= b;
            c ^= rot(b, 8);
            b += a;
            a -= c;
            a ^= rot(c, 16);
            c += b;
            b -= a;
            b ^= rot(a, 19);
            a += c;
            c -= b;
            c ^= rot(b, 4);
            b += a;
        }

        /*------------------------------------------- handle the last 3 uint32_t's */
        switch (k.remaining()) {
        case 0: /* case 0: nothing left to add */
            return c;
        case 1:
            a += k.get();
            break;
        case 2:
            a += k.get();
            b += k.get();
            break;
        case 3:
            a += k.get();
            b += k.get();
            c += k.get();
            break;
        default:
            throw new IllegalArgumentException();
        }

        // final(a,b,c)
        c ^= b;
        c -= rot(b, 14);
        a ^= c;
        a -= rot(c, 11);
        b ^= a;
        b -= rot(a, 25);
        c ^= b;
        c -= rot(b, 16);
        a ^= c;
        a -= rot(c, 4);
        b ^= a;
        b -= rot(a, 14);
        c ^= b;
        c -= rot(b, 24);

        /*------------------------------------------------------ report the result */
        return c;
    }

    /*
     * This function is here for reference purposes. It is inline expanded at every
     * place of use.
     *
     * @param a
     * 
     * @param b
     * 
     * @param c
     */
//    private static void mix(int a, int b, int c) {
    /*
     * -----------------------------------------------------------------------------
     * -- mix -- mix 3 32-bit values reversibly.
     * 
     * This is reversible, so any information in (a,b,c) before mix() is still in
     * (a,b,c) after mix().
     * 
     * If four pairs of (a,b,c) inputs are run through mix(), or through mix() in
     * reverse, there are at least 32 bits of the output that are sometimes the same
     * for one pair and different for another pair. This was tested for: pairs that
     * differed by one bit, by two bits, in any combination of top bits of (a,b,c),
     * or in any combination of bottom bits of (a,b,c). "differ" is defined as +, -,
     * ^, or ~^. For + and -, I transformed the output delta to a Gray code
     * (a^(a>>1)) so a string of 1's (as is commonly produced by subtraction) look
     * like a single 1-bit difference. the base values were pseudorandom, all zero
     * but one bit set, or all zero plus a counter that starts at zero.
     * 
     * Some k values for my "a-=c; a^=rot(c,k); c+=b;" arrangement that satisfy this
     * are 4 6 8 16 19 4 9 15 3 18 27 15 14 9 3 7 17 3 Well, "9 15 3 18 27 15"
     * didn't quite get 32 bits diffing for "differ" defined as + with a one-bit
     * base and a two-bit delta. I used
     * http://burtleburtle.net/bob/hash/avalanche.html to choose the operations,
     * constants, and arrangements of the variables.
     * 
     * This does not achieve avalanche. There are input bits of (a,b,c) that fail to
     * affect some output bits of (a,b,c), especially of a. The most thoroughly
     * mixed value is c, but it doesn't really even achieve avalanche in c.
     * 
     * This allows some parallelism. Read-after-writes are good at doubling the
     * number of bits affected, so the goal of mixing pulls in the opposite
     * direction as the goal of parallelism. I did what I could. Rotates seem to
     * cost as much as shifts on every machine I could lay my hands on, and rotates
     * are much kinder to the top and bottom bits, so I used rotates.
     * -----------------------------------------------------------------------------
     * --
     */
//        a -= c;
//        a ^= rot(c, 4);
//        c += b;
//        b -= a;
//        b ^= rot(a, 6);
//        a += c;
//        c -= b;
//        c ^= rot(b, 8);
//        b += a;
//        a -= c;
//        a ^= rot(c, 16);
//        c += b;
//        b -= a;
//        b ^= rot(a, 19);
//        a += c;
//        c -= b;
//        c ^= rot(b, 4);
//        b += a;
//    }

    /**
     * This function is here for reference purposes. Original name final.
     */
//  private static void finalMix(int a, int b, int c) {
    /*
     * -----------------------------------------------------------------------------
     * -- final -- final mixing of 3 32-bit values (a,b,c) into c
     * 
     * Pairs of (a,b,c) values differing in only a few bits will usually produce
     * values of c that look totally different. This was tested for pairs that
     * differed by one bit, by two bits, in any combination of top bits of (a,b,c),
     * or in any combination of bottom bits of (a,b,c). "differ" is defined as +, -,
     * ^, or ~^. For + and -, I transformed the output delta to a Gray code
     * (a^(a>>1)) so a string of 1's (as is commonly produced by subtraction) look
     * like a single 1-bit difference. the base values were pseudorandom, all zero
     * but one bit set, or all zero plus a counter that starts at zero.
     * 
     * These constants passed: 14 11 25 16 4 14 24 12 14 25 16 4 14 24 and these
     * came close: 4 8 15 26 3 22 24 10 8 15 26 3 22 24 11 8 15 26 3 22 24
     * -----------------------------------------------------------------------------
     * --
     */
//        c ^= b;
//        c -= rot(b, 14);
//        a ^= c;
//        a -= rot(c, 11);
//        b ^= a;
//        b -= rot(a, 25);
//        c ^= b;
//        c -= rot(b, 16);
//        a ^= c;
//        a -= rot(c, 4);
//        b ^= a;
//        b -= rot(a, 14);
//        c ^= b;
//        c -= rot(b, 24);
//    }

    private static int rot(final int x, final int distance) {
        return x << distance | x >>> 32 - distance;
    }
}
