package io.github.ocelot.videotex.api;

import io.github.ocelot.videotex.impl.DSAVideoTexture;
import io.github.ocelot.videotex.impl.LegacyVideoTexture;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.NativeResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

import static org.lwjgl.opengl.GL11C.glDeleteTextures;

/**
 * @author Ocelot
 * @since 1.0.0
 */
public abstract class VideoTexture implements NativeResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(VideoTexture.class);

    private FFmpegFrameGrabber grabber;
    private long startTime;
    private double fps;
    private int frame;

    protected int id;
    protected int width;
    protected int height;

    protected VideoTexture() {
        this.id = 0;
        this.width = 0;
        this.height = 0;
    }

    /**
     * Creates a new {@link VideoTexture} optimized for the platform feature set.
     *
     * @return A new video texture
     */
    public static VideoTexture create() {
        return GL.getCapabilities().GL_ARB_direct_state_access ? new DSAVideoTexture() : new LegacyVideoTexture();
    }

    /**
     * Updates the current state of the texture. Will skip frames to keep the video at the correct time.
     */
    public void update() {
        if (this.grabber == null) {
            return;
        }

        long now = System.currentTimeMillis();
        if (this.startTime == 0) {
            this.startTime = now;
        }

        int expectedFrame = (int) ((now - this.startTime) / 1000.0 * this.fps);
        while (this.frame < expectedFrame - 1) {
            this.frame++;
            try {
                this.grabber.grabImage();
            } catch (Exception e) {
                LOGGER.error("Failed to grab frame, aborting!", e);
                this.stop();
                return;
            }
        }

        if (this.frame < expectedFrame) {
            this.frame = expectedFrame;
            try {
                Frame image = this.grabber.grabImage();
                if (image != null) {
                    this.upload(image);
                }
            } catch (FFmpegFrameGrabber.Exception e) {
                LOGGER.error("Failed to grab frame, aborting!", e);
                this.stop();
            }
        }
    }

    @ApiStatus.OverrideOnly
    protected abstract void upload(Frame frame);

    /**
     * Stops the current video playback and starts playing the specified video data.
     *
     * @param stream A stream pointing to the video to play
     * @throws IOException If any error occurs loading the file
     */
    public void play(InputStream stream) throws IOException {
        this.play(stream, 1.0);
    }

    /**
     * Stops the current video playback and starts playing the specified video data.
     *
     * @param stream A stream pointing to the video to play
     * @throws IOException If any error occurs loading the file
     */
    public void play(InputStream stream, double playbackSpeed) throws IOException {
        try {
            this.stop();
            this.grabber = new FFmpegFrameGrabber(stream);
            this.grabber.start();
            this.fps = playbackSpeed * this.grabber.getVideoFrameRate();
        } catch (FrameGrabber.Exception e) {
            this.grabber = null;
            throw e;
        }
    }

    /**
     * Stops the video from playing. {@link #play(InputStream)} must be called again to play the video.
     */
    public void stop() {
        if (this.grabber != null) {
            try {
                this.grabber.close();
            } catch (FrameGrabber.Exception e) {
                LOGGER.error("Failed to close video", e);
            }
            this.grabber = null;
        }
        this.startTime = 0;
        this.fps = 0;
        this.frame = 0;
    }

    /**
     * Skips ahead the specified number of seconds.
     *
     * @param seconds The time in seconds to skip
     */
    public void skip(double seconds) {
        if (this.grabber != null) {
            int skip = (int) (seconds * this.fps);
            for (int i = 0; i < skip; i++) {
                try {
                    this.grabber.grabImage();
                } catch (Exception e) {
                    LOGGER.error("Failed to grab frame, aborting!", e);
                    this.stop();
                    return;
                }
            }
        }
    }

    @Override
    public void free() {
        if (this.id != 0) {
            glDeleteTextures(this.id);
            this.id = 0;
        }
        this.stop();
        this.width = 0;
        this.height = 0;
    }

    /**
     * @return The OpenGL texture id
     */
    public int getId() {
        return this.id;
    }

    /**
     * @return the width of the video in pixels
     */
    public int getWidth() {
        return this.width;
    }

    /**
     * @return the height of the video in pixels
     */
    public int getHeight() {
        return this.height;
    }
}
