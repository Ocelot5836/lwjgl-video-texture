package io.github.ocelot.videotex.impl;

import io.github.ocelot.videotex.api.VideoTexture;
import org.bytedeco.javacpp.indexer.Indexer;
import org.bytedeco.javacv.Frame;
import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.system.MemoryUtil;

import static org.bytedeco.javacv.Frame.*;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL12C.*;

@ApiStatus.Internal
public class LegacyVideoTexture extends VideoTexture {

    @Override
    public void upload(Frame frame) {
        if (frame.type != Type.VIDEO) {
            return;
        }

        if (frame.imageWidth != this.width || frame.imageHeight != this.height) {
            glDeleteTextures(this.id);
            this.id = 0;
        }
        if (this.id == 0) {
            this.id = glGenTextures();
            this.width = frame.imageWidth;
            this.height = frame.imageHeight;
            glBindTexture(GL_TEXTURE_2D, this.id);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB8, this.width, this.height, 0, GL_RGB, GL_UNSIGNED_BYTE, 0L);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
        } else {
            glBindTexture(GL_TEXTURE_2D, this.id);
        }

        Indexer indexer = frame.createIndexer();
        long address = MemoryUtil.memAddress(indexer.buffer());
        int format = switch (frame.imageDepth) {
            case DEPTH_BYTE -> GL_BYTE;
            case DEPTH_UBYTE -> GL_UNSIGNED_BYTE;
            case DEPTH_SHORT -> GL_SHORT;
            case DEPTH_USHORT -> GL_UNSIGNED_SHORT;
            case DEPTH_INT -> GL_INT;
            case DEPTH_FLOAT -> GL_FLOAT;
            case DEPTH_DOUBLE -> GL_DOUBLE;
            default -> throw new IllegalStateException("Unexpected value: " + frame.imageDepth);
        };
        glPixelStorei(GL_UNPACK_ROW_LENGTH, (int) indexer.stride(0) / 3);
        glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, this.width, this.height, GL_BGR, format, address + frame.imageStride);
    }
}
