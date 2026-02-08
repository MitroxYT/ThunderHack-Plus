package thunder.hack.features.modules.combat;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import thunder.hack.events.impl.EventSync;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.misc.FakePlayer;
import thunder.hack.features.modules.render.NameTags;
import thunder.hack.setting.Setting;
import thunder.hack.utility.Timer;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public final class AntiBot extends Module {
    public static ArrayList<PlayerEntity> bots = new ArrayList<>();
    private HashMap<Integer,Integer> tpCount = new HashMap<>();
    public Setting<Boolean> remove = new Setting<>("Remove", false);
    public Setting<Boolean> onlyAura = new Setting<>("OnlyAura", true);
    private final Setting<Mode> mode = new Setting<>("Mode", Mode.UUIDCheck);
    public Setting<Integer> checkticks = new Setting<>("checkTicks", 3, 0, 10, v -> mode.getValue() == Mode.MotionCheck);
    private final Timer clearTimer = new Timer();
    private int ticks = 0;

    public AntiBot() {
        super("AntiBot", Category.COMBAT);
    }

    @EventHandler
    public void onSync(EventSync e) {
        if (!onlyAura.getValue()) mc.world.getPlayers().forEach(this::markAsBot);
        else if (Aura.target instanceof PlayerEntity ent) this.markAsBot(ent);

        if (remove.getValue())
            bots.forEach(b -> {
                    try {
                        mc.world.removeEntity(b.getId(), Entity.RemovalReason.KILLED);
                    } catch (Exception ignored) {
                    }
            });

        if (clearTimer.passedMs(10000)) {
            bots.clear();
            ticks = 0;
            clearTimer.reset();
            tpCount.clear();
        }
    }
    @EventHandler
    public void onReceive(PacketEvent.Receive event) {
        if (mode.getValue() == Mode.LvmAC) {
            if (event.getPacket() instanceof EntitiesDestroyS2CPacket entitiesDestroyS2CPacket) {
                    for (int id: entitiesDestroyS2CPacket.getEntityIds()) {
                        if (tpCount.containsKey(id)) {
                            tpCount.clear();
                        }
                    }
            }
            if (event.getPacket() instanceof EntityPositionS2CPacket packet) {
                if (!bots.isEmpty()) {
                    for (PlayerEntity entity : bots) {
                        if (entity.getId() == packet.getId()) return;
                    }
                }
                int tp = tpCount.getOrDefault(packet.getId(), 0);
                if (tp > 9) {
                    try {
                        this.addBot((PlayerEntity) mc.world.getEntityById(packet.getId()));
                    } catch (NullPointerException ignored) {
                    }
                }
                tpCount.put(packet.getId(), tp + 1);
            }
        }
    }

    private void markAsBot(PlayerEntity ent) {
        if (bots.contains(ent))
            return;

        switch (mode.getValue()) {
            case UUIDCheck -> {
                if (!ent.getUuid().equals(UUID.nameUUIDFromBytes(("OfflinePlayer:" + ent.getName().getString()).getBytes(StandardCharsets.UTF_8))) && ent instanceof OtherClientPlayerEntity
                        && (FakePlayer.fakePlayer == null || ent.getId() != FakePlayer.fakePlayer.getId())
                        && !ent.getName().getString().contains("-")) {
                    this.addBot(ent);
                }
            }
            case MotionCheck -> {
                double diffX = ent.getX() - ent.prevX;
                double diffZ = ent.getZ() - ent.prevZ;
                
                if ((diffX * diffX) + (diffZ * diffZ) > 0.5D) {
                    if (ticks >= checkticks.getValue())
                        this.addBot(ent);
                    ticks++;
                }
            }
            case ZeroPing -> {
                if (NameTags.getEntityPing(ent) <= 0)
                    this.addBot(ent);
            }
        }
    }

    private void addBot(PlayerEntity entity) {
        if (entity == null) {
            this.sendMessage(entity.getId() + " is a bot!");
            bots.add(entity);
            return;
        }
        this.sendMessage(entity.getName().getString() + " is a bot!");
        bots.add(entity);
    }

    @Override
    public String getDisplayInfo() {
        return String.valueOf(bots.size());
    }

    public enum Mode {
        UUIDCheck, MotionCheck, ZeroPing, LvmAC
    }
}
