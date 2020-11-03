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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import app.keve.hdf5io.util.VMappedFile;

public final class VmapTest {

    @Test
    public void testVoffset() throws IOException {
        final Path path = Path.of("testVoffset.bin");

        try (VMappedFile vmappedFile = VMappedFile.of()) {
            vmappedFile.addMapping(128, 512, Integer.MAX_VALUE, path, StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.TRUNCATE_EXISTING);

            final ByteBuffer src = ByteBuffer.allocate(4096);
            int i = Integer.MAX_VALUE;
            while (src.hasRemaining()) {
                src.put((byte) i--);
            }
            src.flip();
            src.position(64).limit(1024);

            final long[] res = vmappedFile.append(src);
            System.out.println(vmappedFile);
            System.out.println(Arrays.toString(res));
            assertArrayEquals(new long[] {128 /* vOffset at which it was written */, 960 /* length */}, res);

            // 128 = 64 from initial offset + 64 into the written part
            final ByteBuffer srcPart = src.duplicate().position(128).limit(128 + 256);
            System.err.println(srcPart);
            final byte[] srcBytes = new byte[256];
            srcPart.duplicate().get(srcBytes);

            // 192 = 64 into written part (192=128+64)
            // 576 into file (576 = 512 + (192-128)
            final ByteBuffer buf = vmappedFile.at(192, 256);
            final byte[] fileBytes = new byte[256];
            buf.duplicate().get(fileBytes);

            assertArrayEquals(srcBytes, fileBytes);
            assertEquals(-1, srcPart.mismatch(buf));

            assertEquals(960, vmappedFile.size());
            assertEquals(128/* vOffset */ + 960 /* size */, vmappedFile.maxVOffset());
        }
        assertEquals(512/* offsetIntoFile */ + 960 /* size */, Files.size(path));
    }
}
