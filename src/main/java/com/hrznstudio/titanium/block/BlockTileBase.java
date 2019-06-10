/*
 * This file is part of Titanium
 * Copyright (C) 2019, Horizon Studio <contact@hrznstudio.com>.
 *
 * This code is licensed under GNU Lesser General Public License v3.0, the full license text can be found in LICENSE.txt
 */

package com.hrznstudio.titanium.block;

import com.hrznstudio.titanium.api.IFactory;
import com.hrznstudio.titanium.block.tile.TileBase;
import com.hrznstudio.titanium.module.api.RegistryManager;
import com.hrznstudio.titanium.nbthandler.NBTManager;
import com.hrznstudio.titanium.util.TileUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Optional;

public abstract class BlockTileBase<T extends TileBase> extends BlockBase implements ITileEntityProvider {
    private final Class<T> tileClass;
    private TileEntityType tileEntityType;

    public BlockTileBase(String name, Properties properties, Class<T> tileClass) {
        super(name, properties);
        this.tileClass = tileClass;
    }

    @Override
    public void addAlternatives(RegistryManager registry) {
        super.addAlternatives(registry);
        NBTManager.getInstance().scanTileClassForAnnotations(tileClass);
        tileEntityType = TileEntityType.register(getRegistryName().toString(), TileEntityType.Builder.func_223042_a(getTileEntityFactory()::create));//TODO Missing Forge Access transformers?
        registry.content(TileEntityType.class, tileEntityType);
    }

    public abstract IFactory<T> getTileEntityFactory();

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean p_220069_6_) {
        getTile(worldIn, pos).ifPresent(tile -> tile.onNeighborChanged(blockIn, fromPos));
    }

    @Override
    public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult ray) {
        return getTile(worldIn, pos).map(tile -> tile.onActivated(player, hand, ray.getFace(), ray.getHitVec().x, ray.getHitVec().y, ray.getHitVec().z)).orElseGet(() -> super.onBlockActivated(state, worldIn, pos, player, hand, ray));
    }


    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return getTileEntityFactory().create();
    }

    public Optional<T> getTile(IBlockReader access, BlockPos pos) {
        return TileUtil.getTileEntity(access, pos, tileClass);
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(IBlockReader iBlockReader) {
        return getTileEntityFactory().create();
    }

    public TileEntityType getTileEntityType() {
        return tileEntityType;
    }
}