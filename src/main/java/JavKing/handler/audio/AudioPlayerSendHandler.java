package JavKing.handler.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import net.dv8tion.jda.api.audio.AudioSendHandler;

import java.nio.ByteBuffer;

/**
 * This is a wrapper around AudioPlayer which makes it behave as an AudioSendHandler for JDA. As JDA calls canProvide
 * before every call to provide20MsAudio(), we pull the frame in canProvide() and use the frame we already pulled in
 * provide20MsAudio().
 */
public class AudioPlayerSendHandler implements AudioSendHandler {
    private final AudioPlayer audioPlayer;
    private final ByteBuffer buffer;
//    private final MutableAudioFrame frame;

    private AudioFrame lastFrame;

    /**
     * @param audioPlayer Audio player to wrap.
     */
    public AudioPlayerSendHandler(AudioPlayer audioPlayer) {
        this.audioPlayer = audioPlayer;
        this.buffer = ByteBuffer.allocate(920 * 2 * 2);
//        this.frame = new MutableAudioFrame();
//        this.frame.setBuffer(buffer);
    }

    @Override
    public boolean canProvide() {
        // returns true if audio was provided
        if (lastFrame == null) lastFrame = audioPlayer.provide();
        System.out.println("lastFrame: " + lastFrame);
        return lastFrame != null;
//        return audioPlayer.provide(frame);
    }

    @Override
    public ByteBuffer provide20MsAudio() {
        // flip to make it a read buffer
//        ((Buffer) buffer).flip();
//        return buffer;
//        buffer.wait(5_000);
        if (lastFrame == null) lastFrame = audioPlayer.provide();
        byte[] data = lastFrame != null ? lastFrame.getData() : null;
        lastFrame = null;
        System.out.println("data: " + data);
        return data == null ? null : ByteBuffer.wrap(data);
    }

    @Override
    public boolean isOpus() {
        return true;
    }
}