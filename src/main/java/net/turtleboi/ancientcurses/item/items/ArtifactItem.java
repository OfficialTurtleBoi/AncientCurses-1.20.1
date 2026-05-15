package net.turtleboi.ancientcurses.item.items;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

public abstract class ArtifactItem extends Item {
    protected ArtifactItem(Properties properties) {
        super(properties);
    }

    public boolean tryActivateArtifactAbility(ServerPlayer player, ItemStack stack, boolean crouching) {
        return false;
    }

    public static boolean tryActivate(ServerPlayer player) {
        boolean crouching = player.isCrouching();
        Set<ItemStack> visited = Collections.newSetFromMap(new IdentityHashMap<>());

        ItemStack mainHand = player.getMainHandItem();
        if (tryActivate(player, mainHand, crouching, visited)) {
            return true;
        }

        ItemStack offhand = player.getOffhandItem();
        if (tryActivate(player, offhand, crouching, visited)) {
            return true;
        }

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.ARMOR) {
                if (tryActivate(player, player.getItemBySlot(slot), crouching, visited)) {
                    return true;
                }
            }
        }

        if (ModList.get().isLoaded("curios")) {
            if (CuriosApi.getCuriosInventory(player).map(curiosInventory -> {
                String[] handlers = new String[] {"head", "necklace", "body", "belt", "charm", "hands"};
                for (String handlerName : handlers) {
                    if (curiosInventory.getStacksHandler(handlerName).map(handler -> {
                        for (int i = 0; i < handler.getStacks().getSlots(); i++) {
                            if (tryActivate(player, handler.getStacks().getStackInSlot(i), crouching, visited)) {
                                return true;
                            }
                        }
                        return false;
                    }).orElse(false)) {
                        return true;
                    }
                }
                return false;
            }).orElse(false)) {
                return true;
            }
        }

        for (ItemStack stack : player.getInventory().items) {
            if (tryActivate(player, stack, crouching, visited)) {
                return true;
            }
        }

        return false;
    }

    private static boolean tryActivate(ServerPlayer player, ItemStack stack, boolean crouching, Set<ItemStack> visited) {
        if (stack.isEmpty() || !visited.add(stack) || !(stack.getItem() instanceof ArtifactItem artifactItem)) {
            return false;
        }
        return artifactItem.tryActivateArtifactAbility(player, stack, crouching);
    }
}
