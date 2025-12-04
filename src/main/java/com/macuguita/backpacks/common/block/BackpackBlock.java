/*
 * Copyright (c) 2025 macuguita
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
 * OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.macuguita.backpacks.common.block;

import java.util.Map;
import java.util.UUID;

import com.macuguita.backpacks.common.attachments.BackpacksAttachedData;
import com.macuguita.backpacks.common.attachments.GBAttachmentTypes;
import com.macuguita.backpacks.common.block.entity.BackpackBlockEntity;
import com.macuguita.backpacks.common.item.BackpackItem;
import com.macuguita.backpacks.common.reg.GBComponents;
import com.macuguita.backpacks.common.reg.GBObjects;
import com.macuguita.backpacks.common.resourcereloader.BackpacksResourceReloadListener;
import com.mojang.serialization.MapCodec;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BackpackBlock extends BaseEntityBlock implements EntityBlock {

	public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

	public BackpackBlock(Properties settings) {
		super(settings);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec() {
		return simpleCodec(BackpackBlock::new);
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new BackpackBlockEntity(pos, state);
	}

	@Override
	public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
		BlockEntity be = level.getBlockEntity(pos);
		if (be instanceof BackpackBlockEntity backpack) {
			ItemStack stack = new ItemStack(GBObjects.BACKPACK.get());

			stack.set(GBComponents.BACKPACK_MODEL_ID.get(), backpack.getItemModelId());
			stack.set(GBComponents.BACKPACK_UUID.get(), backpack.getUuid());
			ItemEntity itemEntity = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);

			itemEntity.setDefaultPickUpDelay();
			level.addFreshEntity(itemEntity);
		}
		return super.playerWillDestroy(level, pos, state, player);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	protected boolean propagatesSkylightDown(BlockState state) {
		return true;
	}

	@Override
	public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
		return 1.0F;
	}

	@Override
	public boolean skipRendering(BlockState state, BlockState stateFrom, Direction direction) {
		return false;
	}

	@Override
	protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
		return AbstractContainerMenu.getRedstoneSignalFromContainer(getInventory(level, pos, level.getServer()));
	}

	@Override
	protected boolean hasAnalogOutputSignal(BlockState state) {
		return true;
	}

	private @Nullable SimpleContainer getInventory(Level level, BlockPos pos, MinecraftServer server) {
		BlockEntity be = level.getBlockEntity(pos);
		if (be instanceof BackpackBlockEntity backpack) {
			UUID uuid = backpack.getUuid();
			return level.getAttachedOrCreate(GBAttachmentTypes.BACKPACKS_ATTACHMENT_TYPE, () -> BackpacksAttachedData.DEFAULT).getInventory(uuid);
		}
		return null;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		return this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (level instanceof ServerLevel serverLevel) {
			BlockEntity be = serverLevel.getBlockEntity(pos);
			if (be instanceof BackpackBlockEntity backpackBe) {
				UUID uuid = backpackBe.getUuid();
				SimpleContainer inventory = getInventory(serverLevel, pos, player.level().getServer());
				if (inventory == null) {
					BackpacksAttachedData attachedData = serverLevel.getAttachedOrCreate(GBAttachmentTypes.BACKPACKS_ATTACHMENT_TYPE, () -> BackpacksAttachedData.DEFAULT);
					serverLevel.setAttached(GBAttachmentTypes.BACKPACKS_ATTACHMENT_TYPE, attachedData.addInventory(uuid));
					inventory = getInventory(serverLevel, pos, player.level().getServer());
				}
				BackpackItem.openBackpack(player, inventory, -1);
				return InteractionResult.SUCCESS_SERVER;
			}
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	protected BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	protected BlockState mirror(BlockState state, Mirror mirror) {
		return state.rotate(mirror.getRotation(state.getValue(FACING)));
	}

	private VoxelShape getVoxelShape(BlockGetter level, BlockPos pos) {
		BlockEntity be = level.getBlockEntity(pos);
		if (be instanceof BackpackBlockEntity backpack && backpack.getItemModelId() != null) {
			ResourceLocation modelId = backpack.getItemModelId();

			VoxelShape shape = Shapes.create(
					BackpacksResourceReloadListener.BACKPACKS.stream()
							.filter(bp -> bp.id().equals(modelId))
							.findFirst()
							.map(BackpacksResourceReloadListener.Backpack::blockCollisionShape)
							.orElse(new AABB(0, 0, 0, 1, 1, 1))
			);

			Map<Direction, VoxelShape> shapes = Shapes.rotateHorizontal(shape);

			return shapes.get(level.getBlockState(pos).getValue(FACING));
		}
		return Shapes.block();
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return getVoxelShape(level, pos);
	}

	@Override
	protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return getVoxelShape(level, pos);
	}
}
