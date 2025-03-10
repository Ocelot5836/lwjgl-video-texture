package io.github.ocelot.videotex.impl;

import io.github.ocelot.videotex.api.VideoTexture;
import org.jetbrains.annotations.ApiStatus;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL12C.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL12C.GL_TEXTURE_WRAP_R;

@ApiStatus.Internal
public class LegacyVideoTexture extends VideoTexture {

    @Override
    protected void upload(int width, int height, int format, int dataType, long address) {
        if (this.id == 0) {
            this.id = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, this.id);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB8, this.width, this.height, 0, format, dataType, 0L);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
        } else {
            glBindTexture(GL_TEXTURE_2D, this.id);
        }
        glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, this.width, this.height, format, dataType, address);
    }
}
