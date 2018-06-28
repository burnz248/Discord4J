/*
 * This file is part of Discord4J.
 *
 * Discord4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Discord4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */
package discord4j.core.object.entity;

import discord4j.core.ServiceMediator;
import discord4j.core.event.domain.VoiceServerUpdateEvent;
import discord4j.core.event.domain.VoiceStateUpdateEvent;
import discord4j.core.object.data.stored.VoiceChannelBean;
import discord4j.core.spec.VoiceChannelEditSpec;
import discord4j.core.util.EntityUtil;
import discord4j.gateway.json.GatewayPayload;
import discord4j.gateway.json.VoiceStateUpdate;
import discord4j.core.VoiceConnectionController;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;

/** A Discord voice channel. */
public final class VoiceChannel extends BaseGuildChannel {

    /**
     * Constructs an {@code VoiceChannel} with an associated ServiceMediator and Discord data.
     *
     * @param serviceMediator The ServiceMediator associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    public VoiceChannel(final ServiceMediator serviceMediator, final VoiceChannelBean data) {
        super(serviceMediator, data);
    }

    /**
     * Gets the bitrate (in bits) for this voice channel.
     *
     * @return Gets the bitrate (in bits) for this voice channel.
     */
    public int getBitrate() {
        return getData().getBitrate();
    }

    @Override
    protected VoiceChannelBean getData() {
        return (VoiceChannelBean) super.getData();
    }

    /**
     * Gets the user limit of this voice channel.
     *
     * @return The user limit of this voice channel.
     */
    public int getUserLimit() {
        return getData().getUserLimit();
    }

    /**
     * Requests to edit a voice channel.
     *
     * @param spec A {@link Consumer} that provides a "blank" {@link VoiceChannelEditSpec} to be operated on. If some
     * properties need to be retrieved via blocking operations (such as retrieval from a database), then it is
     * recommended to build the spec externally and call {@link #edit(VoiceChannelEditSpec)}.
     *
     * @return A {@link Mono} where, upon successful completion, emits the edited {@link VoiceChannel}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public Mono<VoiceChannel> edit(final Consumer<VoiceChannelEditSpec> spec) {
        final VoiceChannelEditSpec mutatedSpec = new VoiceChannelEditSpec();
        spec.accept(mutatedSpec);
        return edit(mutatedSpec);
    }

    /**
     * Requests to edit a voice channel.
     *
     * @param spec A configured {@link VoiceChannelEditSpec} to perform the request on.
     * @return A {@link Mono} where, upon successful completion, emits the edited {@link VoiceChannel}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public Mono<VoiceChannel> edit(final VoiceChannelEditSpec spec) {
        return getServiceMediator().getRestClient().getChannelService()
                .modifyChannel(getId().asLong(), spec.asRequest())
                .map(EntityUtil::getChannelBean)
                .map(bean -> EntityUtil.getChannel(getServiceMediator(), bean))
                .cast(VoiceChannel.class);
    }

    public Mono<VoiceConnectionController> join() {
        ServiceMediator serviceMediator = getServiceMediator();
        long guildId = getGuildId().asLong();
        long channelId = getId().asLong();
        long selfId = serviceMediator.getStateHolder().getSelfId().get();

        Mono<Void> sendVoiceStateUpdate = Mono.fromRunnable(() -> {
            VoiceStateUpdate voiceStateUpdate = new VoiceStateUpdate(guildId, channelId, false, false); // fixme
            serviceMediator.getGatewayClient().sender().next(GatewayPayload.voiceStateUpdate(voiceStateUpdate));
        });

        Mono<VoiceStateUpdateEvent> waitForVoiceStateUpdate = getClient().getEventDispatcher()
                .on(VoiceStateUpdateEvent.class)
                .filter(vsu -> {
                    long vsuUser = vsu.getCurrent().getUserId().asLong();
                    long vsuGuild = vsu.getCurrent().getGuildId().asLong();

                    return vsuUser == selfId && vsuGuild == guildId; // this update is for the bot user in this guild
                })
                .next();

        Mono<VoiceServerUpdateEvent> waitForVoiceServerUpdate = getClient().getEventDispatcher()
                .on(VoiceServerUpdateEvent.class)
                .filter(vsu -> vsu.getGuildId().asLong() == guildId)
                .next();

        return sendVoiceStateUpdate
                .then(Mono.zip(waitForVoiceStateUpdate, waitForVoiceServerUpdate))
                .map(t -> {
                    String endpoint = t.getT2().getEndpoint().replace(":80", ""); // discord sends the wrong port...
                    String session = t.getT1().getCurrent().getSessionId();
                    String token = t.getT2().getToken();

                    return serviceMediator.getVoiceClient().newConnection(endpoint, guildId, selfId, session, token);
                })
                .map(voiceConnection -> new VoiceConnectionController(voiceConnection, getServiceMediator(), guildId));
    }
}
