/*
    This file is part of Peers, a java SIP softphone.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
    
    Copyright 2010, 2011 Oleg Kulikov, Yohann Martineau
*/

package net.sourceforge.peers.media;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.lang.invoke.MethodHandles;
import java.util.concurrent.CountDownLatch;

public class PcmaEncoder extends Encoder {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final static int cClip = 32635;
    private static byte aLawCompressTable[] = new byte[]{
            1, 1, 2, 2, 3, 3, 3, 3,
            4, 4, 4, 4, 4, 4, 4, 4,
            5, 5, 5, 5, 5, 5, 5, 5,
            5, 5, 5, 5, 5, 5, 5, 5,
            6, 6, 6, 6, 6, 6, 6, 6,
            6, 6, 6, 6, 6, 6, 6, 6,
            6, 6, 6, 6, 6, 6, 6, 6,
            6, 6, 6, 6, 6, 6, 6, 6,
            7, 7, 7, 7, 7, 7, 7, 7,
            7, 7, 7, 7, 7, 7, 7, 7,
            7, 7, 7, 7, 7, 7, 7, 7,
            7, 7, 7, 7, 7, 7, 7, 7,
            7, 7, 7, 7, 7, 7, 7, 7,
            7, 7, 7, 7, 7, 7, 7, 7,
            7, 7, 7, 7, 7, 7, 7, 7,
            7, 7, 7, 7, 7, 7, 7, 7
    };

    public PcmaEncoder() {
    }

    @Override
    public byte[] process(byte[] media, int size) {
        byte[] compressed = new byte[size / 2];

        int j = 0;
        for (int i = 0; i < compressed.length; i++) {
            short sample = (short) (((media[j++] & 0xff) | (media[j++]) << 8));
            compressed[i] = linearToALawSample(sample);
        }
        return compressed;
    }

    /**
     * Compress 16bit value to 8bit value
     *
     * @param sample 16-bit sample
     * @return compressed 8-bit value.
     */
    private byte linearToALawSample(short sample) {
        int sign;
        int exponent;
        int mantissa;
        int s;

        sign = ((~sample) >> 8) & 0x80;
        if (!(sign == 0x80)) {
            sample = (short) -sample;
        }
        if (sample > cClip) {
            sample = cClip;
        }
        if (sample >= 256) {
            exponent = (int) aLawCompressTable[(sample >> 8) & 0x7F];
            mantissa = (sample >> (exponent + 3)) & 0x0F;
            s = (exponent << 4) | mantissa;
        } else {
            s = sample >> 4;
        }
        s ^= (sign ^ 0x55);
        return (byte) s;
    }

}
