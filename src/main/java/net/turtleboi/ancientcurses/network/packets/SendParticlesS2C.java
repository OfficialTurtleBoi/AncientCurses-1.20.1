package net.turtleboi.ancientcurses.network.packets;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.turtleboi.ancientcurses.particle.ModParticles;

import java.util.function.Supplier;

public class SendParticlesS2C {
    private final double x, y, z;

    public SendParticlesS2C(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public SendParticlesS2C(FriendlyByteBuf buf) {
        this.x = buf.readDouble();
        this.y = buf.readDouble();
        this.z = buf.readDouble();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            //System.out.println(Component.literal("Level: " + Minecraft.getInstance().level)); //debug code
            if (Minecraft.getInstance().level != null) {
                for (int i = 0; i < 8; i++) {
                    double xPos = x + (Minecraft.getInstance().level.random.nextDouble() - 0.5);
                    double yPos = y + (Minecraft.getInstance().level.random.nextDouble() - 0.5);
                    double zPos = z + (Minecraft.getInstance().level.random.nextDouble() - 0.5);
                    double xSpeed = (Minecraft.getInstance().level.random.nextDouble() - 0.5) * 0.02;
                    double ySpeed = (Minecraft.getInstance().level.random.nextDouble() - 0.5) * 0.25;
                    double zSpeed = (Minecraft.getInstance().level.random.nextDouble() - 0.5) * 0.02;
                    Minecraft.getInstance().level.addParticle(ModParticles.HEAL_PARTICLES.get(), xPos, yPos, zPos, xSpeed, ySpeed, zSpeed);
                    //System.out.println(Component.literal("Sending particles to the client at: " + x + ", " + y + ", " + z)); //debug code
                }
            }
        });
        context.setPacketHandled(true);
        return true;
    }
}
