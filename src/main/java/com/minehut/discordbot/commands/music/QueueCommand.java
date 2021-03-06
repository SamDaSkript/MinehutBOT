package com.minehut.discordbot.commands.music;

import com.arsenarsen.lavaplayerbridge.player.Player;
import com.arsenarsen.lavaplayerbridge.player.Track;
import com.minehut.discordbot.MinehutBot;
import com.minehut.discordbot.commands.Command;
import com.minehut.discordbot.util.Bot;
import com.minehut.discordbot.util.Chat;
import com.minehut.discordbot.util.UserClient;
import com.minehut.discordbot.util.exceptions.CommandException;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioTrack;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;

/**
 * Made by the developers of FlareBot.
 * Changed by MatrixTunnel on 1/8/2017.
 */
public class QueueCommand extends Command {

    public QueueCommand() {
        super(CommandType.MUSIC, null, "queue", "songs", "playlist", "songlist", "list");
    }

    @Override
    public boolean onCommand(UserClient sender, Guild guild, TextChannel channel, Message message, String[] args) throws CommandException{
        Chat.removeMessage(message, 5);

        Member member = guild.getMember(sender.getUser());
        Player player = MinehutBot.get().getMusicManager().getPlayer(guild.getId());

        if (!player.getPlaylist().isEmpty()) {
            if (sender.isStaff()) {
                if (args.length == 1 && args[0].equals("clear")) {
                    Chat.sendMessage(member.getAsMention() + " Cleared the current playlist.", channel, 15);
                    player.getPlaylist().clear();
                    return true;
                } else if (args.length == 2 && args[0].equalsIgnoreCase("remove")) {
                    int number;
                    try {
                        number = Integer.parseInt(args[1]);
                    } catch (NumberFormatException e) {
                        Chat.sendMessage(member.getAsMention() + " That is an invalid number!", channel, 5);
                        return true;
                    }

                    Queue<Track> queue = MinehutBot.get().getMusicManager().getPlayer(guild.getId()).getPlaylist();

                    if (number < 1 || number > queue.size()) {
                        Chat.sendMessage(member.getAsMention() + " There is no song with that index. Songs in queue: **" + queue.size() + "**", channel, 5);
                        return true;
                    }

                    List<Track> playlist = new ArrayList<>(queue);
                    playlist.remove(number - 1);
                    queue.clear();
                    queue.addAll(playlist);

                    Chat.sendMessage(member.getAsMention() + " Removed song **#" + number + "** from the queue!", channel, 15);
                    return true;
                }
            }

            List<String> songs = new ArrayList<>();
            int i = 1;
            StringBuilder sb = new StringBuilder();
            Iterator<Track> it = player.getPlaylist().iterator();
            while (it.hasNext() && songs.size() < 25) {
                Track next = it.next();

                String toAppend;
                if (next.getTrack() instanceof YoutubeAudioTrack) { //TODO Redo
                    toAppend = String.format("**%s.** [%s](%s) `[%s]` | <@!%s>\n", i++, next.getTrack().getInfo().title,
                            next.getTrack().getInfo().uri, Bot.millisToTime(next.getTrack().getDuration(), false), next.getMeta().get("requester"));
                } else if (next.getTrack() instanceof SoundCloudAudioTrack) {
                    toAppend = String.format("**%s.** [%s](%s) `[%s]` | <@!%s>\n", i++, next.getTrack().getInfo().title,
                            next.getTrack().getInfo().uri, Bot.millisToTime(next.getTrack().getDuration(), false), next.getMeta().get("requester"));
                } else {
                    toAppend = String.format("**%s.** [%s](%s) `[%s]` | <@!%s>\n", i++, next.getTrack().getInfo().title,
                            next.getTrack().getInfo().uri, Bot.millisToTime(next.getTrack().getDuration(), false), next.getMeta().get("requester"));
                }

                if (sb.length() + toAppend.length() > 1024) {
                    songs.add(sb.toString());
                    sb = new StringBuilder();
                }
                sb.append(toAppend);
            }
            songs.add(sb.toString());
            EmbedBuilder builder = Chat.getEmbed().setTitle("Playlist Queue", null);
            for (String s : songs) {
                builder.addField("\u200e", s, false);
            }

            long totalTime = 0;
            for (Track track : player.getPlaylist()) {
                totalTime = totalTime + track.getTrack().getDuration();
            }
            Chat.sendMessage(builder.addField("Total songs", String.valueOf(player.getPlaylist().size()), true) //TODO Redo this
                    .addField("Total Playlist Time", Bot.millisToTime(totalTime, true), true).build(), channel, 25);
                    //.addField("Paused", player.getPaused() ? ":white_check_mark:" : ":x:", true).build(), channel, 25);
        } else {
            Chat.sendMessage(Chat.getEmbed().setDescription("There are no songs in the queue!").setColor(Chat.CUSTOM_RED).build(), channel, 15);
        }

        return true;
    }

}
