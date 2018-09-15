//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package shaoyuan.spiro.spf;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class WaveFileWriter {
    private RandomAccessFile fileWriter;
    private int totalNumberOfBytes;
    private boolean isClosed = false;
    private File file;

    public WaveFileWriter(File file, int rate, short numOfChannels, short samplesBitSize) throws IOException {
        this.file = file;
        this.fileWriter = new RandomAccessFile(file, "rw");
        this.fileWriter.setLength(0L);
        this.fileWriter.writeBytes("RIFF");
        this.fileWriter.writeInt(0);
        this.fileWriter.writeBytes("WAVE");
        this.fileWriter.writeBytes("fmt ");
        this.fileWriter.writeInt(Integer.reverseBytes(16));
        this.fileWriter.writeShort(Short.reverseBytes((short)1));
        this.fileWriter.writeShort(Short.reverseBytes(numOfChannels));
        this.fileWriter.writeInt(Integer.reverseBytes(rate));
        this.fileWriter.writeInt(Integer.reverseBytes(rate * samplesBitSize * 8 * numOfChannels));
        this.fileWriter.writeShort(Short.reverseBytes((short)(numOfChannels * samplesBitSize * 8)));
        this.fileWriter.writeShort(Short.reverseBytes(samplesBitSize));
        this.fileWriter.writeBytes("data");
        this.fileWriter.writeInt(0);
    }

    public void write(byte[] buffer) throws IOException {
        if (!this.isClosed) {
            this.fileWriter.write(buffer);
            this.totalNumberOfBytes += buffer.length;
        }
    }

    public void close() throws IOException {
        if (!this.isClosed) {
            this.fileWriter.seek(4L);
            this.fileWriter.writeInt(Integer.reverseBytes(36 + this.totalNumberOfBytes));
            this.fileWriter.seek(40L);
            this.fileWriter.writeInt(Integer.reverseBytes(this.totalNumberOfBytes));
            this.fileWriter.close();
            this.isClosed = true;
        }
    }

    public boolean isClosed() {
        return this.isClosed;
    }

    public File getFile() {
        return this.file;
    }
}
