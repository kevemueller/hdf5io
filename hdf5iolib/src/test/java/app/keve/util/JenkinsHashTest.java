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
package app.keve.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import app.keve.hdf5io.util.JenkinsHash;

public final class JenkinsHashTest {
    public static final int A55A5AA5 = 0xa55a5aa5;

    @Test
    public void test0() {
        final IntBuffer k0 = IntBuffer.allocate(0);
        assertEquals(0xdeadbeef, JenkinsHash.hashWord(k0, 0));
        assertEquals(0x84081994, JenkinsHash.hashWord(k0, A55A5AA5));
    }

    @Test
    public void test1() {
        final IntBuffer k1 = IntBuffer.allocate(1).put(A55A5AA5).flip();
        assertEquals(0x31a14298, JenkinsHash.hashWord(k1, 0));
        k1.rewind();
        assertEquals(0xa9a063c1, JenkinsHash.hashWord(k1, A55A5AA5));
    }

    @Test
    public void test4() {
        final IntBuffer k4 = IntBuffer.allocate(4).put(A55A5AA5).put(A55A5AA5).put(0).put(A55A5AA5).flip();
        assertEquals(0xea584351, JenkinsHash.hashWord(k4, 0));
        k4.rewind();
        assertEquals(0x2dd2d4b4, JenkinsHash.hashWord(k4, A55A5AA5));
    }

    @Test
    public void test5() {
        final IntBuffer k5 = IntBuffer.allocate(5).put(A55A5AA5).put(A55A5AA5).put(0).put(A55A5AA5).put(A55A5AA5)
                .flip();
        assertEquals(0x63eb4fab, JenkinsHash.hashWord(k5, 0));
        k5.rewind();
        assertEquals(0x93575004, JenkinsHash.hashWord(k5, A55A5AA5));
    }

    @Test
    public void test6() {
        final IntBuffer k6 = IntBuffer.allocate(6).put(A55A5AA5).put(A55A5AA5).put(A55A5AA5).put(0).put(A55A5AA5)
                .put(A55A5AA5).flip();
        assertEquals(0xd655e88d, JenkinsHash.hashWord(k6, 0));
        k6.rewind();
        assertEquals(0x208bca19, JenkinsHash.hashWord(k6, A55A5AA5));
    }

    @Test
    public void test7() {
        final IntBuffer k7 = IntBuffer.allocate(7).put(A55A5AA5).put(A55A5AA5).put(A55A5AA5).put(0).put(A55A5AA5).put(0)
                .put(A55A5AA5).flip();
        assertEquals(0x52bca703, JenkinsHash.hashWord(k7, 0));
        k7.rewind();
        assertEquals(0x16d4037d, JenkinsHash.hashWord(k7, A55A5AA5));
    }

    @Test
    public void test8() {
        final IntBuffer k8 = IntBuffer.allocate(8).put(0).put(A55A5AA5).put(A55A5AA5).put(A55A5AA5).put(0).put(A55A5AA5)
                .put(0).put(A55A5AA5).flip();
        assertEquals(0x05b76d07, JenkinsHash.hashWord(k8, 0));
        k8.rewind();
        assertEquals(0x65ab5069, JenkinsHash.hashWord(k8, A55A5AA5));
    }

    @Test
    public void testb12() {
        final ByteBuffer b12 = ByteBuffer.wrap(new byte[] {(byte) 136, 22, (byte) 196, (byte) 255, (byte) 157,
                (byte) 178, 8, 3, 19, (byte) 165, (byte) 220, 7}).order(ByteOrder.LITTLE_ENDIAN);
        final IntBuffer k = b12.order(ByteOrder.LITTLE_ENDIAN).asIntBuffer();
        assertEquals(0x1c7ca037, JenkinsHash.hashWord(k, 0));
        k.rewind();
        assertEquals(0xcf72688f, JenkinsHash.hashWord(k, A55A5AA5));
    }

    @Test
    public void testb12a() {
        final ByteBuffer b12 = ByteBuffer.wrap(new byte[] {(byte) 136, 22, (byte) 196, (byte) 255, (byte) 157,
                (byte) 178, 8, 3, 19, (byte) 165, (byte) 220, 7}).order(ByteOrder.LITTLE_ENDIAN);
        assertEquals(0x1c7ca037, JenkinsHash.hash(b12, 0));
        b12.rewind();
        assertEquals(0xcf72688f, JenkinsHash.hash(b12, A55A5AA5));
    }

    @Test
    public void testb12b() {
        final ByteBuffer b12 = ByteBuffer.wrap(new byte[] {(byte) 136, 22, (byte) 196, (byte) 255, (byte) 157,
                (byte) 178, 8, 3, 19, (byte) 165, (byte) 220, 7}).order(ByteOrder.LITTLE_ENDIAN);
        assertEquals(0x1c7ca037, JenkinsHash.hashBytes(b12, 0));
        b12.rewind();
        assertEquals(0xcf72688f, JenkinsHash.hashBytes(b12, A55A5AA5));
    }

    @ParameterizedTest
    @ValueSource(strings = {"myData.scalar", "dataSmall.contiguous", "dataSmall.chunked", "dataSmall.compact",
            "dataSmall.deflate", "dataLarge.contiguous"})
    public void hash(final String value) {
        final ByteBuffer buf = StandardCharsets.US_ASCII.encode(value);
        final int hash = JenkinsHash.hash(buf.order(ByteOrder.LITTLE_ENDIAN), 0);
        System.err.println("" + value + "->" + hash);
    }

}
