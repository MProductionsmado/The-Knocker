package net.mcreator.theknocker.procedures;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.InteractionHand;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Mth;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.CommandSource;

import net.mcreator.theknocker.init.TheKnockerModEntities;
import net.mcreator.theknocker.entity.KnockerstalklookedEntity;
import net.mcreator.theknocker.entity.KnockerEntity;

import java.util.Map;
import java.util.Comparator;

public class SpawnknockerProcedure {
    public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
        if (entity == null)
            return;

        Entity knocker = null;
        double random = 0;
        random = Math.round(Mth.nextDouble(RandomSource.create(), 1, 2));

        if (random == 1) {
            if (world instanceof ServerLevel _level) {
                Entity entityToSpawn = new KnockerstalklookedEntity(TheKnockerModEntities.KNOCKERSTALKLOOKED.get(), _level);
                entityToSpawn.moveTo(x, (y - 100), z, world.getRandom().nextFloat() * 360F, 0);
                if (entityToSpawn instanceof Mob _mobToSpawn)
                    _mobToSpawn.finalizeSpawn(_level, world.getCurrentDifficultyAt(entityToSpawn.blockPosition()), MobSpawnType.MOB_SUMMONED, null, null);
                world.addFreshEntity(entityToSpawn);
            }
            knocker = world.getEntitiesOfClass(KnockerstalklookedEntity.class, AABB.ofSize(new Vec3(x, (y - 100), z), 4, 4, 4), e -> true).stream()
                .sorted(Comparator.comparingDouble(_ent -> _ent.distanceToSqr(x, (y - 100), z)))
                .findFirst()
                .orElse(null);

            if (knocker != null) {  // null check
                if (knocker instanceof LivingEntity _entity) {
                    ItemStack _setstack = new ItemStack(Blocks.SOUL_TORCH);
                    _setstack.setCount(1);
                    _entity.setItemInHand(InteractionHand.MAIN_HAND, _setstack);
                    if (_entity instanceof Player _player)
                        _player.getInventory().setChanged();
                }
                BlockPos _bp = new BlockPos(knocker.getX(), knocker.getY() + 1, knocker.getZ());
                BlockState _bs = Blocks.LIGHT.defaultBlockState();
                BlockState _bso = world.getBlockState(_bp);
                for (Map.Entry<Property<?>, Comparable<?>> entry : _bso.getValues().entrySet()) {
                    Property _property = _bs.getBlock().getStateDefinition().getProperty(entry.getKey().getName());
                    if (_property != null && _bs.getValue(_property) != null) {
                        try {
                            _bs = _bs.setValue(_property, entry.getValue());
                        } catch (Exception e) {
                            // idk just if any exceptions are needed
                        }
                    }
                }
                world.setBlock(_bp, _bs, 3);

                if (knocker instanceof KnockerstalklookedEntity _datEntSetL) {
                    _datEntSetL.getEntityData().set(KnockerstalklookedEntity.DATA_torch_event, true);
                }
            }
        } else {
            if (world instanceof ServerLevel _level) {
                Entity entityToSpawn = new KnockerEntity(TheKnockerModEntities.KNOCKER.get(), _level);
                entityToSpawn.moveTo(x, (y - 100), z, world.getRandom().nextFloat() * 360F, 0);
                if (entityToSpawn instanceof Mob _mobToSpawn)
                    _mobToSpawn.finalizeSpawn(_level, world.getCurrentDifficultyAt(entityToSpawn.blockPosition()), MobSpawnType.MOB_SUMMONED, null, null);
                world.addFreshEntity(entityToSpawn);
            }
            knocker = world.getEntitiesOfClass(KnockerEntity.class, AABB.ofSize(new Vec3(x, (y - 100), z), 4, 4, 4), e -> true).stream()
                .sorted(Comparator.comparingDouble(_ent -> _ent.distanceToSqr(x, (y - 100), z)))
                .findFirst()
                .orElse(null);

            if (knocker != null) {  // null check
                if (!entity.level.isClientSide() && entity.getServer() != null) {
                    entity.getServer().getCommands().performPrefixedCommand(
                        new CommandSourceStack(CommandSource.NULL, entity.position(), entity.getRotationVector(),
                        entity.level instanceof ServerLevel ? (ServerLevel) entity.level : null, 4,
                        entity.getName().getString(), entity.getDisplayName(), entity.level.getServer(), entity),
                        "spreadplayers ~ ~ 0 50 false @e[type=the_knocker:knocker,limit=1]");
                }
            }
        }
    }
}
