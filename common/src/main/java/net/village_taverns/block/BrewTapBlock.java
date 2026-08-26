package net.village_taverns.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.village_taverns.TavernsMod;
import org.jetbrains.annotations.Nullable;

public class BrewTapBlock extends Block {
    public static final String NAME = "barrel";
    public static final Identifier ID = Identifier.fromNamespaceAndPath(TavernsMod.ID, NAME);

    public BrewTapBlock(BlockBehaviour.Properties settings) {
        super(settings);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    // The "Workbench for Bartender Villagers." hint used to live in `Block#appendTooltip`, which no
    // longer exists in 1.21.11 — it moved onto the block's item (see `TavernBlockItem`).

    // MARK: Facing

    // 1.21.11: `DirectionProperty` is gone, `Properties.HORIZONTAL_FACING` is an `EnumProperty<Direction>`.
    private static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    // MARK: Partial transparency

    // 1.21.11: `isTranslucent(state, world, pos)` → `isTransparent(state)`.
    @Override
    protected boolean propagatesSkylightDown(BlockState state) {
        return true;
    }

    // MARK: Shape

    public static final VoxelShape SHAPE = Block.box(0, 0, 2, 16, 14, 14);
    public static final VoxelShape SHAPE_R = Block.box(2, 0, 0, 14, 14, 16);

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case NORTH, SOUTH -> SHAPE_R;
            default -> SHAPE;
        };
    }
}
