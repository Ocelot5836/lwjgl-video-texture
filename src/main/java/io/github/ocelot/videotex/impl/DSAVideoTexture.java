package io.github.ocelot.videotex.impl;

import io.github.ocelot.videotex.api.VideoTexture;
import org.bytedeco.javacpp.indexer.Indexer;
import org.bytedeco.javacv.Frame;
import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.system.MemoryUtil;

import static org.bytedeco.javacv.Frame.*;
import static org.lwjgl.opengl.ARBDirectStateAccess.*;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL12C.*;

@ApiStatus.Internal
public class DSAVideoTexture extends VideoTexture {

    @Override
    protected void upload(int width, int height, int format, int dataType, long address) {
        if (this.id == 0) {
            this.id = glCreateTextures(GL_TEXTURE_2D);
            glTextureStorage2D(this.id, 1, GL_RGB8, this.width, this.height);
            glTextureParameteri(this.id, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTextureParameteri(this.id, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTextureParameteri(this.id, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTextureParameteri(this.id, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            glTextureParameteri(this.id, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
        }
        glTextureSubImage2D(this.id, 0, 0, 0, this.width, this.height, format, dataType, address);
    }
}
