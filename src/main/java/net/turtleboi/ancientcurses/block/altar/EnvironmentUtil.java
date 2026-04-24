package net.turtleboi.ancientcurses.block.altar;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.turtleboi.ancientcurses.block.ModBlocks;
import net.turtleboi.ancientcurses.block.entity.CursedAltarBlockEntity;
import net.turtleboi.ancientcurses.particle.ModParticleTypes;

import java.util.List;

public final class EnvironmentUtil {
    public static final List<BlockPos> SOUL_TORCH_OFFSETS = BlockPos.betweenClosedStream(-2, -2, -2, 2, 2, 2)
            .filter(offset -> Math.abs(offset.getX()) == 2 || Math.abs(offset.getZ()) == 2)
            .map(BlockPos::immutable)
            .toList();

    private EnvironmentUtil() {
    }

    public static void animateSoulLights(Level level, BlockPos altarPos, RandomSource random) {
        if (!level.isClientSide()) {
            return;
        }

        for (BlockPos offset : SOUL_TORCH_OFFSETS) {
            BlockPos torchPos = altarPos.offset(offset);
            BlockState torchState = level.getBlockState(torchPos);
            if (random.nextInt(16) != 0 || !isSoulLight(level, torchPos)) {
                continue;
            }

            double particleX;
            double particleY;
            double particleZ;

            if (torchState.is(Blocks.SOUL_WALL_TORCH)) {
                Direction direction = torchState.getValue(WallTorchBlock.FACING);
                Direction oppositeDirection = direction.getOpposite();
                particleX = torchPos.getX() + 0.5D + 0.27D * oppositeDirection.getStepX();
                particleY = torchPos.getY() + 0.7D;
                particleZ = torchPos.getZ() + 0.5D + 0.27D * oppositeDirection.getStepZ();
            } else {
                particleX = torchPos.getX() + 0.5D;
                particleY = torchPos.getY() + 0.7D;
                particleZ = torchPos.getZ() + 0.5D;
            }

            double altarX = altarPos.getX() + 0.5D;
            double altarY = altarPos.getY() + 1.0D;
            double altarZ = altarPos.getZ() + 0.5D;

            level.addParticle(
                    ParticleTypes.SCULK_SOUL,
                    particleX,
                    particleY,
                    particleZ,
                    (altarX - particleX) * 0.05,
                    (altarY - particleY) * 0.05,
                    (altarZ - particleZ) * 0.05
            );
        }
    }

    public static boolean isSoulLight(Level level, BlockPos pos) {
        return level.getBlockState(pos).is(ModBlocks.SCONCED_SOUL_TORCH.get())
                || level.getBlockState(pos).is(ModBlocks.SCONCED_WALL_SOUL_TORCH.get())
                || level.getBlockState(pos).is(Blocks.SOUL_LANTERN);
    }

    public static void convertSoulLights(CursedAltarBlockEntity altarEntity, Level level) {
        BlockPos altarPos = altarEntity.getBlockPos();

        for (BlockPos offset : SOUL_TORCH_OFFSETS) {
            BlockPos lightPos = altarPos.offset(offset);
            if (!isSoulLight(level, lightPos)) {
                continue;
            }

            BlockState lightState = level.getBlockState(lightPos);
            BlockState finalLightState;

            if (lightState.getBlock() instanceof WallTorchBlock) {
                Direction direction = lightState.getValue(WallTorchBlock.FACING);
                finalLightState = ModBlocks.SCONCED_WALL_CURSED_TORCH.get().defaultBlockState()
                        .setValue(WallTorchBlock.FACING, direction);
            } else if (lightState.getBlock() instanceof TorchBlock) {
                finalLightState = ModBlocks.SCONCED_CURSED_TORCH.get().defaultBlockState();
            } else if (lightState.getBlock() instanceof LanternBlock) {
                finalLightState = ModBlocks.CURSED_LANTERN.get().defaultBlockState();
            } else {
                continue;
            }

            level.setBlock(lightPos, finalLightState, 3);

            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        ModParticleTypes.CURSED_FLAME_PARTICLE.get(),
                        lightPos.getX() + 0.5,
                        lightPos.getY() + 0.7,
                        lightPos.getZ() + 0.5,
                        20,
                        0.05,
                        0.05,
                        0.05,
                        0.01
                );

                serverLevel.playSound(
                        null,
                        lightPos.getX() + 0.5,
                        lightPos.getY() + 0.7,
                        lightPos.getZ() + 0.5,
                        SoundEvents.GHAST_SHOOT,
                        SoundSource.HOSTILE,
                        0.25f,
                        0.5f
                );
            }
        }
    }

    public static int countSoulTorchesAround(BlockGetter world, BlockPos centerPos) {
        int soulTorchAmount = 0;
        for (int x = -2; x <= 2; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -2; z <= 2; z++) {
                    BlockPos offsetPos = centerPos.offset(x, y, z);
                    if (world.getBlockState(offsetPos).is(ModBlocks.SCONCED_SOUL_TORCH.get())) {
                        soulTorchAmount++;
                    }
                }
            }
        }
        return soulTorchAmount;
    }
}
