package net.village_taverns.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.village_taverns.TavernsMod;
import org.jetbrains.annotations.Nullable;

public class BrewTapBlock extends Block {
    public static final String NAME = "barrel";
    public static final Identifier ID = Identifier.of(TavernsMod.ID, NAME);

    public BrewTapBlock(AbstractBlock.Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(FACING, Direction.NORTH));
    }

    // The "Workbench for Bartender Villagers." hint used to live in `Block#appendTooltip`, which no
    // longer exists in 1.21.11 — it moved onto the block's item (see `TavernBlockItem`).

    // MARK: Facing

    // 1.21.11: `DirectionProperty` is gone, `Properties.HORIZONTAL_FACING` is an `EnumProperty<Direction>`.
    private static final EnumProperty<Direction> FACING = Properties.HORIZONTAL_FACING;

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    // MARK: Partial transparency

    // 1.21.11: `isTranslucent(state, world, pos)` → `isTransparent(state)`.
    @Override
    protected boolean isTransparent(BlockState state) {
        return true;
    }

    // MARK: Shape

    public static final VoxelShape SHAPE = Block.createCuboidShape(0, 0, 2, 16, 14, 14);
    public static final VoxelShape SHAPE_R = Block.createCuboidShape(2, 0, 0, 14, 14, 16);

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return switch (state.get(FACING)) {
            case NORTH, SOUTH -> SHAPE_R;
            default -> SHAPE;
        };
    }
}
