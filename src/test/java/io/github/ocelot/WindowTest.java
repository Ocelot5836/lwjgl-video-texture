package io.github.ocelot;

import io.github.ocelot.videotex.api.VideoTexture;
import io.github.ocelot.window.Window;
import io.github.ocelot.window.WindowEventListener;
import io.github.ocelot.window.WindowManager;
import io.github.ocelot.window.input.KeyMods;
import org.bytedeco.javacv.FrameGrabber;
import org.lwjgl.opengl.GL;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL20C.*;
import static org.lwjgl.opengl.GL30C.*;

public class WindowTest {

    public static void main(String[] args) throws IOException, InterruptedException {
        try (WindowManager windowManager = new WindowManager()) {
            Window window = windowManager.create("Test", 800, 600, false);

            GL.createCapabilities();
            glfwMakeContextCurrent(window.getHandle());

            int vao = glGenVertexArrays();
            int program = glCreateProgram();
            int vertexShader = glCreateShader(GL_VERTEX_SHADER);
            int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);

            try (VideoTexture texture = VideoTexture.create()) {
                byte[] data;
                try (InputStream stream = WindowTest.class.getResourceAsStream("/recording.mov")) {
                    data = stream.readAllBytes();
                }

                glShaderSource(vertexShader, """
                        #version 330 core
                        
                        out vec2 texCoord;
                        
                        void main() {
                            vec2 uv = vec2(gl_VertexID & 1, gl_VertexID & 2);
                            gl_Position = vec4(uv * vec2(3.0) - vec2(1.0), 0.0, 1.0);
                            texCoord = uv * vec2(1.5);
                        }
                        """);
                glShaderSource(fragmentShader, """
                        #version 330 core
                        
                        uniform sampler2D Sampler;
                        
                        in vec2 texCoord;
                        
                        out vec4 outColor;
                        
                        void main() {
                            outColor = texture(Sampler, vec2(texCoord.x, 1.0 - texCoord.y));
                        }
                        """);
                glCompileShader(vertexShader);
                glCompileShader(fragmentShader);

                glAttachShader(program, vertexShader);
                glAttachShader(program, fragmentShader);
                glLinkProgram(program);

                glBindVertexArray(vao);
                glUseProgram(program);

                glClearColor(0.0F, 0.0F, 0.0F, 0.0F);

                texture.play(new ByteArrayInputStream(data));
                window.addListener(new WindowEventListener() {
                    @Override
                    public void keyPressed(Window window, int key, int scanCode, KeyMods mods) {
                        if (key == GLFW_KEY_R) {
                            try {
                                texture.play(new ByteArrayInputStream(data), mods.control() ? 8 : 1);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else if (key == GLFW_KEY_Q) {
                            texture.stop();
                        } else if (key == GLFW_KEY_A) {
                            texture.skip(1);
                        }
                    }
                });
                while (!window.isClosed()) {
                    windowManager.update();
                    glViewport(0, 0, window.getFramebufferWidth(), window.getFramebufferHeight());

                    texture.update();
                    glBindTexture(GL_TEXTURE_2D, texture.getId());

                    if (texture.isDone()) {
                        texture.play(new ByteArrayInputStream(data), 2.0);
                    }

                    glDrawArrays(GL_TRIANGLES, 0, 3);
                }
            } catch (FrameGrabber.Exception e) {
                throw new RuntimeException(e);
            } finally {
                glDeleteVertexArrays(vao);
                glDeleteProgram(program);
                glDeleteShader(vertexShader);
                glDeleteShader(fragmentShader);
            }
        }
    }
}
