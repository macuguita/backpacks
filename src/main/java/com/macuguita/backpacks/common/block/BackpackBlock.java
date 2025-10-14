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

import com.macuguita.backpacks.common.block.entity.BackpackBlockEntity;
import com.macuguita.backpacks.common.components.GuitaBackpacksComponents;
import com.macuguita.backpacks.common.item.BackpackItem;
import com.macuguita.backpacks.common.reg.GBComponents;
import com.macuguita.backpacks.common.reg.GBObjects;
import com.macuguita.backpacks.common.resourcereloader.BackpacksResourceReloadListener;
import com.mojang.serialization.MapCodec;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class BackpackBlock extends BlockWithEntity implements BlockEntityProvider {

	public static final EnumProperty<Direction> FACING = Properties.HORIZONTAL_FACING;

	public BackpackBlock(Settings settings) {
		super(settings);
		this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH));
	}

	@Override
	protected MapCodec<? extends BlockWithEntity> getCodec() {
		return createCodec(BackpackBlock::new);
	}

	@Override
	public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new BackpackBlockEntity(pos, state);
	}

	@Override
	public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
		BlockEntity be = world.getBlockEntity(pos);
		if (be instanceof BackpackBlockEntity backpack) {
			ItemStack stack = new ItemStack(GBObjects.BACKPACK.get());

			stack.set(GBComponents.BACKPACK_MODEL_ID.get(), backpack.getItemModelId());
			stack.set(GBComponents.BACKPACK_UUID.get(), backpack.getUuid());
			ItemEntity itemEntity = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);

			itemEntity.setToDefaultPickupDelay();
			world.spawnEntity(itemEntity);
		}
		return super.onBreak(world, pos, state, player);
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	protected boolean isTransparent(BlockState state) {
		return true;
	}

	@Override
	public float getAmbientOcclusionLightLevel(BlockState state, BlockView world, BlockPos pos) {
		return 1.0F;
	}

	@Override
	public boolean isSideInvisible(BlockState state, BlockState stateFrom, Direction direction) {
		return false;
	}

	@Override
	protected int getComparatorOutput(BlockState state, World world, BlockPos pos, Direction direction) {
		return ScreenHandler.calculateComparatorOutput(getInventory(world, pos, world.getServer()));
	}

	@Override
	protected boolean hasComparatorOutput(BlockState state) {
		return true;
	}

	private @Nullable SimpleInventory getInventory(BlockView world, BlockPos pos, MinecraftServer server) {
		BlockEntity be = world.getBlockEntity(pos);
		if (be instanceof BackpackBlockEntity backpack) {
			UUID uuid = backpack.getUuid();
			if (server != null) {
				return GuitaBackpacksComponents.BACKPACKS_COMPONENT.get(server.getScoreboard()).getInventory(uuid);
			}
		}
		return null;
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
	}

	@Override
	protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		if (world instanceof ServerWorld serverWorld) {
			BlockEntity be = world.getBlockEntity(pos);
			if (be instanceof BackpackBlockEntity backpackBe) {
				UUID uuid = backpackBe.getUuid();
				if (uuid != null) {
					SimpleInventory inventory = getInventory(world, pos, player.getEntityWorld().getServer());
					if (inventory == null) {
						GuitaBackpacksComponents.BACKPACKS_COMPONENT.get(serverWorld.getScoreboard()).addInventory(uuid);
						inventory = getInventory(world, pos, player.getEntityWorld().getServer());
					}
					BackpackItem.openBackpack(player, inventory, -1);
					return ActionResult.SUCCESS_SERVER;
				}
			}
		}
		return ActionResult.SUCCESS;
	}

	@Override
	protected BlockState rotate(BlockState state, BlockRotation rotation) {
		return state.with(FACING, rotation.rotate(state.get(FACING)));
	}

	@Override
	protected BlockState mirror(BlockState state, BlockMirror mirror) {
		return state.rotate(mirror.getRotation(state.get(FACING)));
	}

	private VoxelShape getVoxelShape(BlockView world, BlockPos pos) {
		BlockEntity be = world.getBlockEntity(pos);
		if (be instanceof BackpackBlockEntity backpack && backpack.getItemModelId() != null) {
			Identifier modelId = backpack.getItemModelId();

			VoxelShape shape = VoxelShapes.cuboid(
					BackpacksResourceReloadListener.BACKPACKS.stream()
							.filter(bp -> bp.id().equals(modelId))
							.findFirst()
							.map(BackpacksResourceReloadListener.Backpack::blockCollisionShape)
							.orElse(new Box(0, 0, 0, 1, 1, 1))
			);

			Map<Direction, VoxelShape> shapes = VoxelShapes.createHorizontalFacingShapeMap(shape);

			return shapes.get(world.getBlockState(pos).get(FACING));
		}
		return VoxelShapes.fullCube();
	}

	@Override
	protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return getVoxelShape(world, pos);
	}

	@Override
	protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return getVoxelShape(world, pos);
	}
}
